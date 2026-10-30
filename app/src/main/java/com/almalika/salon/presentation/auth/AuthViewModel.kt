package com.almalika.salon.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.almalika.salon.domain.model.UserSessionState
import com.almalika.salon.domain.model.sessionState
import kotlinx.coroutines.flow.map

sealed interface AuthActionState {
    data object Idle : AuthActionState
    data object Loading : AuthActionState
    data class Success(val message: String = "") : AuthActionState
    data class Error(val message: String) : AuthActionState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            authRepository.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
        )

    val sessionState: StateFlow<UserSessionState> = authState
        .map { it.sessionState }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            authRepository.currentUser?.sessionState ?: UserSessionState.SIGNED_OUT
        )

    private val _loginState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val loginState: StateFlow<AuthActionState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val registerState: StateFlow<AuthActionState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val forgotPasswordState: StateFlow<AuthActionState> = _forgotPasswordState.asStateFlow()

    private val _accountActionState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val accountActionState: StateFlow<AuthActionState> = _accountActionState.asStateFlow()

    private fun isValidEmail(email: String): Boolean =
        com.almalika.salon.core.util.ContactValidator.isValidEmail(email)

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit = {}) {
        val validation = com.almalika.salon.core.util.ContactValidator.validateIdentifier(email)
        val normalizedEmail: String = when (validation) {
            is com.almalika.salon.core.util.InputValidationResult.ValidEmail -> validation.normalizedEmail
            is com.almalika.salon.core.util.InputValidationResult.ValidPhone -> {
                // Phone authentication is not supported for email/password auth mode
                _loginState.value = AuthActionState.Error(com.almalika.salon.core.util.ValidationError.PHONE_AUTH_DISABLED.defaultMessage)
                return
            }
            is com.almalika.salon.core.util.InputValidationResult.Invalid -> {
                _loginState.value = AuthActionState.Error(validation.error.defaultMessage)
                return
            }
        }
        if (password.isBlank()) {
            _loginState.value = AuthActionState.Error("Please enter your password")
            return
        }
        if (password.length < 6) {
            _loginState.value = AuthActionState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.signInWithEmail(normalizedEmail, password)
                .onSuccess {
                    _loginState.value = AuthActionState.Success("Sign in successful")
                    onSuccess()
                }
                .onFailure { error ->
                    _loginState.value = AuthActionState.Error(error.message ?: "Authentication failed")
                }
        }
    }

    fun signInAnonymously(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.signInAnonymously()
                .onSuccess {
                    _loginState.value = AuthActionState.Success("Guest session started")
                    onSuccess()
                }
                .onFailure { error ->
                    _loginState.value = AuthActionState.Error(error.message ?: "Failed to sign in as guest")
                }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptTerms: Boolean = true,
        requireTerms: Boolean = false,
        sendVerification: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isBlank()) {
            _registerState.value = AuthActionState.Error("Please enter your full name")
            return
        }
        val emailValidation = com.almalika.salon.core.util.ContactValidator.validateEmail(trimmedEmail)
        val normalizedEmail = when (emailValidation) {
            is com.almalika.salon.core.util.InputValidationResult.ValidEmail -> emailValidation.normalizedEmail
            else -> {
                _registerState.value = AuthActionState.Error("Please enter a valid email address")
                return
            }
        }
        if (password.length < 6) {
            _registerState.value = AuthActionState.Error("Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = AuthActionState.Error("Passwords do not match")
            return
        }
        if (requireTerms && !acceptTerms) {
            _registerState.value = AuthActionState.Error("Please accept Terms and Conditions to proceed")
            return
        }

        viewModelScope.launch {
            _registerState.value = AuthActionState.Loading
            // If user is already anonymous guest, link credentials seamlessly
            val currentUser = authRepository.currentUser
            val result = if (currentUser?.isAnonymous == true) {
                authRepository.linkAnonymousAccount(trimmedName, normalizedEmail, password, sendVerification)
            } else {
                authRepository.registerWithEmail(trimmedName, normalizedEmail, password, sendVerification)
            }

            result
                .onSuccess {
                    _registerState.value = AuthActionState.Success("Account created successfully")
                    onSuccess()
                }
                .onFailure { error ->
                    _registerState.value = AuthActionState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun sendSignInLinkToEmail(
        email: String,
        url: String = "https://almalaika-salon.firebaseapp.com/__/auth/action",
        onSuccess: () -> Unit = {}
    ) {
        val validation = com.almalika.salon.core.util.ContactValidator.validateIdentifier(email)
        val normalizedEmail: String = when (validation) {
            is com.almalika.salon.core.util.InputValidationResult.ValidEmail -> validation.normalizedEmail
            is com.almalika.salon.core.util.InputValidationResult.ValidPhone -> {
                _loginState.value = AuthActionState.Error(com.almalika.salon.core.util.ValidationError.PHONE_AUTH_DISABLED.defaultMessage)
                return
            }
            is com.almalika.salon.core.util.InputValidationResult.Invalid -> {
                _loginState.value = AuthActionState.Error(validation.error.defaultMessage)
                return
            }
        }
        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.sendSignInLinkToEmail(normalizedEmail, url)
                .onSuccess {
                    _loginState.value = AuthActionState.Success("Sign-in link sent! Please check your email.")
                    onSuccess()
                }
                .onFailure { error ->
                    _loginState.value = AuthActionState.Error(error.message ?: "Failed to send sign-in link")
                }
        }
    }

    fun signInWithEmailLink(email: String, emailLink: String, onSuccess: () -> Unit = {}) {
        val emailValidation = com.almalika.salon.core.util.ContactValidator.validateEmail(email)
        val normalizedEmail = when (emailValidation) {
            is com.almalika.salon.core.util.InputValidationResult.ValidEmail -> emailValidation.normalizedEmail
            else -> {
                _loginState.value = AuthActionState.Error("Please enter a valid email address")
                return
            }
        }
        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.signInWithEmailLink(normalizedEmail, emailLink)
                .onSuccess {
                    _loginState.value = AuthActionState.Success("Signed in successfully")
                    onSuccess()
                }
                .onFailure { error ->
                    _loginState.value = AuthActionState.Error(error.message ?: "Failed to sign in with email link")
                }
        }
    }

    fun sendPasswordReset(email: String) {
        val validation = com.almalika.salon.core.util.ContactValidator.validateIdentifier(email)
        val normalizedEmail: String = when (validation) {
            is com.almalika.salon.core.util.InputValidationResult.ValidEmail -> validation.normalizedEmail
            is com.almalika.salon.core.util.InputValidationResult.ValidPhone -> {
                _forgotPasswordState.value = AuthActionState.Error("Password reset is only available for email accounts. Please enter your email address.")
                return
            }
            is com.almalika.salon.core.util.InputValidationResult.Invalid -> {
                _forgotPasswordState.value = AuthActionState.Error("Please enter a valid email address")
                return
            }
        }

        viewModelScope.launch {
            _forgotPasswordState.value = AuthActionState.Loading
            authRepository.sendPasswordResetEmail(normalizedEmail)
                .onSuccess {
                    _forgotPasswordState.value = AuthActionState.Success("Password reset link sent! Check your inbox.")
                }
                .onFailure { error ->
                    _forgotPasswordState.value = AuthActionState.Error(error.message ?: "Failed to send reset email")
                }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _accountActionState.value = AuthActionState.Loading
            authRepository.sendEmailVerification()
                .onSuccess {
                    _accountActionState.value = AuthActionState.Success("Verification email sent! Check your inbox.")
                }
                .onFailure { error ->
                    _accountActionState.value = AuthActionState.Error(error.message ?: "Failed to send verification email")
                }
        }
    }

    fun signOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            _accountActionState.value = AuthActionState.Loading
            authRepository.signOut()
                .onSuccess {
                    _accountActionState.value = AuthActionState.Success("Signed out successfully")
                    onSignedOut()
                }
                .onFailure { error ->
                    _accountActionState.value = AuthActionState.Error(error.message ?: "Failed to sign out")
                }
        }
    }

    fun clearLoginState() {
        _loginState.value = AuthActionState.Idle
    }

    fun clearRegisterState() {
        _registerState.value = AuthActionState.Idle
    }

    fun clearForgotPasswordState() {
        _forgotPasswordState.value = AuthActionState.Idle
    }

    fun clearAccountActionState() {
        _accountActionState.value = AuthActionState.Idle
    }

    fun clearAllActionStates() {
        _loginState.value = AuthActionState.Idle
        _registerState.value = AuthActionState.Idle
        _forgotPasswordState.value = AuthActionState.Idle
        _accountActionState.value = AuthActionState.Idle
    }
}
