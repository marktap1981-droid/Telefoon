package nl.voorraadbeheer.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.voorraadbeheer.app.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var joinCode by remember { mutableStateOf("") }

    LaunchedEffect(uiState.joinResult) {
        if (uiState.joinResult != null) joinCode = ""
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            viewModel.userEmail?.let {
                Text(stringResource(R.string.settings_account, it))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.settings_notifications_enabled), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled,
                )
            }

            OutlinedTextField(
                value = prefs.expiryReminderDays.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::setExpiryReminderDays) },
                label = { Text(stringResource(R.string.settings_expiry_reminder_days)) },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_household_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.settings_household_explanation), style = MaterialTheme.typography.bodyLarge)

                    uiState.householdCode?.let { code ->
                        Text(
                            code,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        OutlinedButton(onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, code)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, null))
                        }) {
                            Text(stringResource(R.string.settings_household_share))
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_household_join_title), style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it.uppercase() },
                        label = { Text(stringResource(R.string.settings_household_join_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { viewModel.joinHousehold(joinCode) },
                        enabled = joinCode.isNotBlank() && !uiState.isJoiningHousehold,
                    ) {
                        Text(stringResource(R.string.settings_household_join_button))
                    }
                    uiState.joinResult?.let { result ->
                        Text(
                            if (result == JoinHouseholdResult.SUCCESS) {
                                stringResource(R.string.settings_household_join_success)
                            } else {
                                stringResource(R.string.settings_household_join_failed)
                            },
                            color = if (result == JoinHouseholdResult.SUCCESS) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }

            Button(onClick = { viewModel.signOut(context) }) {
                Text(stringResource(R.string.settings_sign_out))
            }
        }
    }
}
