package nl.voorraadbeheer.app.ui.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import nl.voorraadbeheer.app.R
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.util.daysUntil
import nl.voorraadbeheer.app.util.toDisplayString

@Composable
fun InventoryListScreen(
    onScanClick: () -> Unit,
    onAddManuallyClick: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.inventory_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onScanClick) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.nav_scan))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::setQuery,
                label = { Text(stringResource(R.string.inventory_search_hint)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.selectedLocationId.isNullOrBlank(),
                    onClick = { viewModel.selectLocation(null) },
                    label = { Text(stringResource(R.string.inventory_all_locations)) },
                )
                uiState.locations.forEach { location ->
                    FilterChip(
                        selected = uiState.selectedLocationId == location.id,
                        onClick = { viewModel.selectLocation(location.id) },
                        label = { Text(location.name) },
                    )
                }
            }

            val filtered = uiState.filteredItems
            if (!uiState.isLoading && filtered.isEmpty()) {
                Text(
                    stringResource(R.string.inventory_empty),
                    modifier = Modifier.padding(16.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.id }) { item ->
                    InventoryRow(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onIncrement = { viewModel.changeQuantity(item, 1) },
                        onDecrement = { viewModel.changeQuantity(item, -1) },
                        onDelete = { viewModel.deleteItem(item.id) },
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), onClick = onAddManuallyClick) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Text(
                                stringResource(R.string.inventory_add_manually),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                if (item.brand.isNotBlank()) {
                    Text(item.brand, style = MaterialTheme.typography.labelLarge)
                }
                val expiry = item.expiryDate
                if (expiry != null) {
                    val days = expiry.daysUntil()
                    val color = when {
                        days < 0 -> MaterialTheme.colorScheme.error
                        days <= 3 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        stringResource(R.string.inventory_expiry, expiry.toDisplayString()),
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            IconButton(onClick = onDecrement) {
                Icon(Icons.Filled.Remove, contentDescription = null)
            }
            Text(
                "${item.quantity}",
                style = MaterialTheme.typography.titleLarge,
                color = if (item.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.inventory_delete))
            }
        }
    }
}
