package nl.voorraadbeheer.app.ui.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.voorraadbeheer.app.R

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var newItemText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.shopping_list_title)) }) },
    ) { padding ->
        if (!uiState.isLoading && uiState.lowStockItems.isEmpty() && uiState.manualItems.isEmpty()) {
            Text(
                stringResource(R.string.shopping_list_empty),
                modifier = Modifier.padding(padding).padding(16.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (uiState.lowStockItems.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.shopping_list_auto_section),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                items(uiState.lowStockItems, key = { "low_${it.id}" }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${item.name} (${item.quantity}/${item.minQuantity})")
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.shopping_list_manual_section),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            items(uiState.manualItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { viewModel.setChecked(item.id, it) },
                        )
                        Text(
                            item.name,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    IconButton(onClick = { viewModel.deleteManualItem(item.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text(stringResource(R.string.shopping_list_add_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    IconButton(onClick = {
                        viewModel.addManualItem(newItemText)
                        newItemText = ""
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
                    }
                }
            }
        }
    }
}
