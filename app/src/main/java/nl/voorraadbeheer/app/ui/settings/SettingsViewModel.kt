package nl.voorraadbeheer.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.repository.AuthRepository
import nl.voorraadbeheer.app.data.repository.HouseholdRepository
import nl.voorraadbeheer.app.data.repository.UserPreferences
import nl.voorraadbeheer.app.data.repository.UserPreferencesRepository
import javax.inject.Inject

enum class JoinHouseholdResult { SUCCESS, NOT_FOUND }

data class SettingsUiState(
    val householdCode: String? = null,
    val isJoiningHousehold: Boolean = false,
    val joinResult: JoinHouseholdResult? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    val userEmail: String?
        get() = authRepository.currentUser?.email

    init {
        viewModelScope.launch {
            val code = runCatching { householdRepository.getShareCode() }.getOrNull()
            _uiState.value = _uiState.value.copy(householdCode = code)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }

    fun setExpiryReminderDays(days: Int) {
        viewModelScope.launch { preferencesRepository.setExpiryReminderDays(days) }
    }

    fun joinHousehold(code: String) {
        if (code.isBlank()) return
        _uiState.value = _uiState.value.copy(isJoiningHousehold = true, joinResult = null)
        viewModelScope.launch {
            val success = runCatching { householdRepository.joinHousehold(code) }.getOrDefault(false)
            _uiState.value = _uiState.value.copy(
                isJoiningHousehold = false,
                joinResult = if (success) JoinHouseholdResult.SUCCESS else JoinHouseholdResult.NOT_FOUND,
                householdCode = if (success) runCatching { householdRepository.getShareCode() }.getOrNull() else _uiState.value.householdCode,
            )
        }
    }

    fun clearJoinResult() {
        _uiState.value = _uiState.value.copy(joinResult = null)
    }

    fun signOut(context: Context) {
        authRepository.signOut(context)
    }
}
