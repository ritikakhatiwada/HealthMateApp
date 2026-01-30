package com.example.healthmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { role ->
                    _loginState.value = LoginState.Success(role)
                    onResult(true, "Login success")
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Login failed")
                    onResult(false, error.message ?: "Login failed")
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val role: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
