package com.example.healthmate.data.repository

import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.model.Appointment
import com.example.healthmate.util.SecureLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AppointmentRepository using Firebase Firestore.
 */
@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    companion object {
        private const val TAG = "AppointmentRepository"
        private const val COLLECTION_APPOINTMENTS = "appointments"
        private const val COLLECTION_SLOTS = "slots"
    }

    override suspend fun bookAppointment(appointment: Appointment): Result<String> {
        return try {
            // Mark slot as booked
            firestore.collection(COLLECTION_SLOTS)
                .document(appointment.slotId)
                .update("isBooked", true)
                .await()

            // Add appointment
            val docRef = firestore.collection(COLLECTION_APPOINTMENTS).add(appointment).await()
            SecureLogger.d(TAG, "Appointment booked")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error booking appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserAppointments(userId: String): List<Appointment> {
        return try {
            val snapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("patientId", userId)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Appointment::class.java)?.copy(id = doc.id) }
                .sortedByDescending { it.bookedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching user appointments", e)
            emptyList()
        }
    }

    override suspend fun getAllAppointments(): List<Appointment> {
        return try {
            val snapshot = firestore.collection(COLLECTION_APPOINTMENTS).get().await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Appointment::class.java)?.copy(id = doc.id) }
                .sortedByDescending { it.bookedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all appointments", e)
            emptyList()
        }
    }

    override suspend fun getTodaysAppointments(): List<Appointment> {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("date", today)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Appointment::class.java)?.copy(id = doc.id) }
                .sortedBy { it.time }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching today's appointments", e)
            emptyList()
        }
    }

    override suspend fun getTodaysAppointmentsCount(): Int {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("date", today)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting today's appointments", e)
            0
        }
    }

    override suspend fun cancelAppointment(appointmentId: String, slotId: String): Result<Unit> {
        return try {
            // Update appointment status to CANCELLED
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .update("status", "CANCELLED")
                .await()

            // Free up the slot
            firestore.collection(COLLECTION_SLOTS)
                .document(slotId)
                .update("isBooked", false)
                .await()

            SecureLogger.d(TAG, "Appointment cancelled")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error cancelling appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .update("status", status)
                .await()
            SecureLogger.d(TAG, "Appointment status updated to: $status")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating appointment status", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .delete()
                .await()
            SecureLogger.d(TAG, "Appointment deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun autoUpdateAppointmentStatuses(): Result<Int> {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            var updatedCount = 0
            snapshot.documents.forEach { doc ->
                val appointmentDate = doc.getString("date") ?: ""
                if (appointmentDate < today) {
                    firestore.collection(COLLECTION_APPOINTMENTS)
                        .document(doc.id)
                        .update("status", "COMPLETED")
                        .await()
                    updatedCount++
                }
            }

            SecureLogger.d(TAG, "Auto-updated $updatedCount appointments to COMPLETED")
            Result.success(updatedCount)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error auto-updating appointments", e)
            Result.failure(e)
        }
    }
}
