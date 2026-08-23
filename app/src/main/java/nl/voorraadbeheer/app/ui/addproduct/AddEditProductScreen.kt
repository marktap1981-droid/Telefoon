package nl.voorraadbeheer.app.ui.addproduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import nl.voorraadbeheer.app.R
import nl.voorraadbeheer.app.util.toDisplayString

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    onSaved: () -> Unit,
    viewModel: AddEditProductViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    var locationMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val titleRes = if (uiState.isEditing && !uiState.isRestockingExisting) {
        R.string.product_edit_title
    } else {
        R.string.product_add_title
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(titleRes)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLookingUp) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.scan_looking_up), modifier = Modifier.padding(start = 8.dp))
                }
            } else if (uiState.lookupFailed) {
                Text(stringResource(R.string.scan_not_found), color = MaterialTheme.colorScheme.error)
            } else if (uiState.isRestockingExisting) {
                Text(stringResource(R.string.product_restocking_hint), color = MaterialTheme.colorScheme.primary)
            }

            if (uiState.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.product_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = viewModel::updateBrand,
                label = { Text(stringResource(R.string.product_brand_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.category,
                onValueChange = viewModel::updateCategory,
                label = { Text(stringResource(R.string.product_category_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(
                expanded = locationMenuExpanded,
                onExpandedChange = { locationMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.locations.firstOrNull { it.id == uiState.locationId }?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.product_location_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                DropdownMenu(
                    expanded = locationMenuExpanded,
                    onDismissRequest = { locationMenuExpanded = false },
                ) {
                    uiState.locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location.name) },
                            onClick = {
                                viewModel.updateLocation(location.id)
                                locationMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.quantity.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::updateQuantity) },
                label = { Text(stringResource(R.string.product_quantity_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.minQuantity.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::updateMinQuantity) },
                label = { Text(stringResource(R.string.product_min_quantity_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.product_expiry_label), style = MaterialTheme.typography.labelLarge)
            AssistChip(
                onClick = { showDatePicker = true },
                label = {
                    val millis = uiState.expiryDateMillis
                    Text(
                        if (millis != null) {
                            com.google.firebase.Timestamp(java.util.Date(millis)).toDisplayString()
                        } else {
                            stringResource(R.string.product_pick_expiry)
                        },
                    )
                },
            )
            if (uiState.expiryDateMillis != null) {
                TextButton(onClick = { viewModel.updateExpiryMillis(null) }) {
                    Text(stringResource(R.string.product_no_expiry))
                }
            }

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.name.isNotBlank() && uiState.locationId.isNotBlank(),
            ) {
                Text(stringResource(R.string.product_save))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.expiryDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateExpiryMillis(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
