package com.example.healthmate.domain.repository

import com.example.healthmate.model.Appointment

/**
 * Repository interface for appointment operations.
 */
interface AppointmentRepository {
    suspend fun bookAppointment(appointment: Appointment): Result<String>
    suspend fun getUserAppointments(userId: String): List<Appointment>
    suspend fun getAllAppointments(): List<Appointment>
    suspend fun getTodaysAppointments(): List<Appointment>
    suspend fun getTodaysAppointmentsCount(): Int
    suspend fun cancelAppointment(appointmentId: String, slotId: String): Result<Unit>
    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit>
    suspend fun deleteAppointment(appointmentId: String): Result<Unit>
    suspend fun autoUpdateAppointmentStatuses(): Result<Int>
}
