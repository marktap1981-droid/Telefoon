package nl.voorraadbeheer.app.ui.locations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.voorraadbeheer.app.R
import nl.voorraadbeheer.app.data.model.Location

@Composable
fun LocationsScreen(
    viewModel: LocationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Location?>(null) }
    var blockedDeleteCount by remember { mutableStateOf<Int?>(null) }
    var pendingRename by remember { mutableStateOf<Location?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.locations_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.locations_add))
            }
        },
    ) { padding ->
        if (uiState.locations.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.locations_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.locations, key = { it.id }) { location ->
                    val itemCount = uiState.itemCounts[location.id] ?: 0
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { pendingRename = location }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(location.name, style = MaterialTheme.typography.bodyLarge)
                                Text("$itemCount", style = MaterialTheme.typography.labelLarge)
                            }
                            IconButton(onClick = {
                                if (itemCount > 0) {
                                    blockedDeleteCount = itemCount
                                } else {
                                    pendingDelete = location
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.locations_add)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.locations_name_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addLocation(name)
                    showAddDialog = false
                }) { Text(stringResource(R.string.action_add)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    pendingDelete?.let { location ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.locations_delete_confirm)) },
            text = { Text(location.name) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLocation(location.id)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    blockedDeleteCount?.let { count ->
        AlertDialog(
            onDismissRequest = { blockedDeleteCount = null },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.locations_delete_blocked, count)) },
            confirmButton = {
                TextButton(onClick = { blockedDeleteCount = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    pendingRename?.let { location ->
        var name by remember(location.id) { mutableStateOf(location.name) }
        AlertDialog(
            onDismissRequest = { pendingRename = null },
            title = { Text(stringResource(R.string.locations_rename)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.locations_name_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameLocation(location.id, name)
                    pendingRename = null
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRename = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
