package nl.voorraadbeheer.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.repository.AuthRepository
import nl.voorraadbeheer.app.data.repository.UserPreferences
import nl.voorraadbeheer.app.data.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val userEmail: String?
        get() = authRepository.currentUser?.email

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }

    fun setExpiryReminderDays(days: Int) {
        viewModelScope.launch { preferencesRepository.setExpiryReminderDays(days) }
    }

    fun signOut(context: Context) {
        authRepository.signOut(context)
    }
}
