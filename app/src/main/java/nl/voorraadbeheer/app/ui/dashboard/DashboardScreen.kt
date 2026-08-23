package nl.voorraadbeheer.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.voorraadbeheer.app.R

@Composable
fun DashboardScreen(
    onLocationClick: (locationId: String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) }) },
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.dashboard_total_products),
                        value = uiState.totalProducts.toString(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    val lowStockHighlight = uiState.lowStockCount > 0
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.dashboard_low_stock),
                        value = uiState.lowStockCount.toString(),
                        containerColor = if (lowStockHighlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                        contentColor = if (lowStockHighlight) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                    )
                    val expiringHighlight = uiState.expiringSoonCount > 0
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.dashboard_expiring_soon),
                        value = uiState.expiringSoonCount.toString(),
                        containerColor = if (expiringHighlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                        contentColor = if (expiringHighlight) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.dashboard_locations),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (uiState.locationSummaries.isEmpty()) {
                item { Text(stringResource(R.string.dashboard_no_locations)) }
            } else {
                items(uiState.locationSummaries, key = { it.location.id }) { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onLocationClick(summary.location.id) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(summary.location.name, style = MaterialTheme.typography.bodyLarge)
                            Text("${summary.itemCount}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
