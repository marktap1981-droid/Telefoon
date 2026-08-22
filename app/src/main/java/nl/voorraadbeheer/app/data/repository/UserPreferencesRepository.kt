package nl.voorraadbeheer.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "voorraadbeheer_settings")

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val expiryReminderDays: Int = 3,
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val EXPIRY_REMINDER_DAYS = intPreferencesKey("expiry_reminder_days")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            expiryReminderDays = prefs[Keys.EXPIRY_REMINDER_DAYS] ?: 3,
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setExpiryReminderDays(days: Int) {
        context.dataStore.edit { it[Keys.EXPIRY_REMINDER_DAYS] = days.coerceIn(1, 30) }
    }
}
