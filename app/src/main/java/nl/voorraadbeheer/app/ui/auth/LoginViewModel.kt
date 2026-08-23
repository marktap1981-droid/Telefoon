package nl.voorraadbeheer.app.ui.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.repository.AuthRepository
import nl.voorraadbeheer.app.data.repository.HouseholdRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun googleSignInIntent(context: Context): Intent =
        authRepository.googleSignInClient(context).signInIntent

    fun signInWithGoogleIdToken(idToken: String) {
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                authRepository.signInWithGoogleIdToken(idToken)
                householdRepository.ensureHousehold()
                locationRepository.ensureDefaultLocationsExist()
            }.onSuccess {
                _uiState.value = LoginUiState(isLoading = false)
            }.onFailure {
                _uiState.value = LoginUiState(isLoading = false, error = it.message)
            }
        }
    }

    fun onSignInFailed(message: String?) {
        _uiState.value = LoginUiState(isLoading = false, error = message)
    }
}
