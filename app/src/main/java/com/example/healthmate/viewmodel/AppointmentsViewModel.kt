package com.example.healthmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.domain.repository.AuthRepository
import com.example.healthmate.model.Appointment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _appointmentsState = MutableStateFlow<AppointmentsState>(AppointmentsState.Loading)
    val appointmentsState: StateFlow<AppointmentsState> = _appointmentsState.asStateFlow()

    fun loadAppointments() {
        viewModelScope.launch {
            _appointmentsState.value = AppointmentsState.Loading
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId.isNotEmpty()) {
                    val appointments = appointmentRepository.getUserAppointments(userId)
                    _appointmentsState.value = AppointmentsState.Success(appointments)
                } else {
                    _appointmentsState.value = AppointmentsState.Error("User not logged in")
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentsState.Error(e.message ?: "Failed to load appointments")
            }
        }
    }

    sealed class AppointmentsState {
        object Loading : AppointmentsState()
        data class Success(val appointments: List<Appointment>) : AppointmentsState()
        data class Error(val message: String) : AppointmentsState()
    }
}
