package com.example.healthmate.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.domain.repository.DoctorRepository
import com.example.healthmate.domain.repository.UserRepository
import com.example.healthmate.model.Appointment
import com.example.healthmate.util.SecureLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Admin Dashboard.
 *
 * Manages:
 * - Dashboard statistics (users, doctors, appointments)
 * - Today's appointments
 * - Weekly appointment data for charts
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val doctorRepository: DoctorRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AdminDashboardVM"
    }

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Fetch current user name
                val userId = userRepository.getCurrentUserId()
                val userName = if (userId != null) {
                    userRepository.getUserById(userId)?.name ?: "Admin"
                } else {
                    "Admin"
                }

                // Fetch counts
                val usersCount = userRepository.getUsersCount()
                val doctorsCount = doctorRepository.getDoctorsCount()
                val todaysAppointmentsCount = appointmentRepository.getTodaysAppointmentsCount()
                val todaysAppointments = appointmentRepository.getTodaysAppointments().take(5)

                // Calculate weekly data for charts
                val allAppointments = appointmentRepository.getAllAppointments()
                val weeklyData = calculateWeeklyData(allAppointments)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = userName,
                        usersCount = usersCount,
                        doctorsCount = doctorsCount,
                        todaysAppointmentsCount = todaysAppointmentsCount,
                        todaysAppointments = todaysAppointments,
                        weeklyAppointmentData = weeklyData,
                        error = null
                    )
                }

                SecureLogger.d(TAG, "Dashboard data loaded successfully")
            } catch (e: Exception) {
                SecureLogger.e(TAG, "Error loading dashboard data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard data. Please try again."
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun calculateWeeklyData(appointments: List<Appointment>): List<DayAppointmentCount> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Get the last 7 days
        val last7Days = (0..6).map { daysAgo ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(calendar.time)
        }.reversed()

        return last7Days.map { dateString ->
            val count = appointments.count { it.date == dateString }
            val date = dateFormat.parse(dateString) ?: Date()
            DayAppointmentCount(
                dayLabel = displayFormat.format(date),
                date = dateString,
                count = count
            )
        }
    }
}

/**
 * UI State for Admin Dashboard.
 */
data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val userName: String = "Admin",
    val usersCount: Int = 0,
    val doctorsCount: Int = 0,
    val todaysAppointmentsCount: Int = 0,
    val todaysAppointments: List<Appointment> = emptyList(),
    val weeklyAppointmentData: List<DayAppointmentCount> = emptyList(),
    val error: String? = null
)

/**
 * Data class for daily appointment count (for charts).
 */
data class DayAppointmentCount(
    val dayLabel: String,
    val date: String,
    val count: Int
)
