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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Initial)

    private val _loginState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val loginState: StateFlow<AuthActionState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val registerState: StateFlow<AuthActionState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val forgotPasswordState: StateFlow<AuthActionState> = _forgotPasswordState.asStateFlow()

    private val _accountActionState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val accountActionState: StateFlow<AuthActionState> = _accountActionState.asStateFlow()

    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    private fun isValidEmail(email: String): Boolean = emailPattern.matches(email)

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit = {}) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            _loginState.value = AuthActionState.Error("Please enter your email address")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _loginState.value = AuthActionState.Error("Please enter a valid email address")
            return
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
            authRepository.signInWithEmail(trimmedEmail, password)
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
        if (trimmedEmail.isBlank() || !isValidEmail(trimmedEmail)) {
            _registerState.value = AuthActionState.Error("Please enter a valid email address")
            return
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
                authRepository.linkAnonymousAccount(trimmedName, trimmedEmail, password, sendVerification)
            } else {
                authRepository.registerWithEmail(trimmedName, trimmedEmail, password, sendVerification)
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

    fun sendSignInLinkToEmail(email: String, url: String) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !isValidEmail(trimmed)) {
            _loginState.value = AuthActionState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.sendSignInLinkToEmail(trimmed, url)
                .onSuccess {
                    _loginState.value = AuthActionState.Success("Sign-in link sent! Please check your email.")
                }
                .onFailure { error ->
                    _loginState.value = AuthActionState.Error(error.message ?: "Failed to send sign-in link")
                }
        }
    }

    fun signInWithEmailLink(email: String, emailLink: String, onSuccess: () -> Unit = {}) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !isValidEmail(trimmed)) {
            _loginState.value = AuthActionState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthActionState.Loading
            authRepository.signInWithEmailLink(trimmed, emailLink)
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
        val trimmed = email.trim()
        if (trimmed.isBlank() || !isValidEmail(trimmed)) {
            _forgotPasswordState.value = AuthActionState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _forgotPasswordState.value = AuthActionState.Loading
            authRepository.sendPasswordResetEmail(trimmed)
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
}
