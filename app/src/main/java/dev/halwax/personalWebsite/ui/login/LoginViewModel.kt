package dev.halwax.personalWebsite.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.halwax.personalWebsite.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel für den Login-Bildschirm
 * Kapselt die Login-Logik und den UI-Status
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Login-Status
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Email-Text
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // Passwort-Text
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Auth-Status abonnieren
    val isSignedIn = authRepository.isSignedIn

    /**
     * E-Mail aktualisieren
     */
    fun updateEmail(email: String) {
        _email.value = email
    }

    /**
     * Passwort aktualisieren
     */
    fun updatePassword(password: String) {
        _password.value = password
    }

    /**
     * Login-Versuch
     */
    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _loginState.value = LoginState.Error("E-Mail und Passwort müssen ausgefüllt werden")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.signIn(_email.value, _password.value)

            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Ungültige E-Mail oder Passwort")
            }
        }
    }

    /**
     * Login-Status zurücksetzen
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Sealed Class für verschiedene Login-Zustände
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}