package com.example.invoicemaker.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invoicemaker.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    val isLoggedIn: Boolean
        get() = authRepository.currentUser != null

    val currentUserUid: String?
        get() = authRepository.currentUser?.uid

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = AuthUiState.Error("Email and password can't be empty")
            return
        }
        if (password.length < 6) {
            uiState = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(email.trim(), password)
            uiState = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = AuthUiState.Error("Email and password can't be empty")
            return
        }
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email.trim(), password)
            uiState = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        val session = authRepository.beginLogout()
        uiState = AuthUiState.Idle
        if (session != null) {
            viewModelScope.launch { authRepository.revoke(session) }
        }
    }

    fun resetState() {
        uiState = AuthUiState.Idle
    }
}
