package com.example.healthmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.domain.repository.UserRepository
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val userState: StateFlow<DashboardState> = _userState.asStateFlow()

    fun loadUserData() {
        viewModelScope.launch {
            _userState.value = DashboardState.Loading
            try {
                val userId = userRepository.getCurrentUserId()
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        _userState.value = DashboardState.Success(user)
                    } else {
                        _userState.value = DashboardState.Error("User not found")
                    }
                } else {
                    _userState.value = DashboardState.Error("Not logged in")
                }
            } catch (e: Exception) {
                _userState.value = DashboardState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    sealed class DashboardState {
        object Loading : DashboardState()
        data class Success(val user: User) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }
}
