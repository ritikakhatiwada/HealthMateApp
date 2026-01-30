package com.example.healthmate.data

import com.example.healthmate.model.*
import com.example.healthmate.util.SecureLogger
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await

/** Firestore helper for all database operations. */
object FirestoreHelper {

    private const val TAG = "FirestoreHelper"
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // ==================== USERS ====================

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching user", e)
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").whereEqualTo("role", "USER").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching users", e)
            emptyList()
        }
    }

    suspend fun getUsersCount(): Int {
        return try {
            val snapshot = db.collection("users").whereEqualTo("role", "USER").get().await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting users", e)
            0
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(updates).await()
            SecureLogger.d(TAG, "User profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    // ==================== DOCTORS ====================

    suspend fun getDoctors(): List<Doctor> {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Doctor::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching doctors", e)
            emptyList()
        }
    }

    suspend fun getDoctorsCount(): Int {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting doctors", e)
            0
        }
    }

    suspend fun addDoctor(doctor: Doctor): Result<String> {
        return try {
            val docRef = db.collection("doctors").add(doctor).await()
            SecureLogger.d(TAG, "Doctor added with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding doctor", e)
            Result.failure(e)
        }
    }

    suspend fun getDoctorById(doctorId: String): Doctor? {
        return try {
            val doc = db.collection("doctors").document(doctorId).get().await()
            doc.toObject(Doctor::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching doctor by ID", e)
            null
        }
    }

    suspend fun updateDoctor(doctorId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("doctors").document(doctorId).update(updates).await()
            SecureLogger.d(TAG, "Doctor profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating doctor profile", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(doctorId: String): Result<Unit> {
        return try {
            db.collection("doctors").document(doctorId).delete().await()
            // Also delete associated slots
            val slotsSnapshot = db.collection("slots").whereEqualTo("doctorId", doctorId).get().await()
            slotsSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            SecureLogger.d(TAG, "Doctor and associated slots deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting doctor", e)
            Result.failure(e)
        }
    }

    // ==================== SLOTS ====================

    suspend fun getAllSlots(): List<Slot> {
        return try {
            SecureLogger.d(TAG, "Fetching all slots")
            val snapshot = db.collection("slots").get().await()
            SecureLogger.d(TAG, "Total slots in database: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Slot::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all slots", e)
            emptyList()
        }
    }

    suspend fun getSlotsByDoctor(doctorId: String): List<Slot> {
        return try {
            SecureLogger.d(TAG, "Fetching slots for doctor")
            val snapshot = db.collection("slots").whereEqualTo("doctorId", doctorId).get().await()
            SecureLogger.d(TAG, "Found ${snapshot.size()} slots")
            snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(Slot::class.java)?.copy(id = doc.id)
                    }
                    .sortedWith(compareBy({ it.date }, { it.time }))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching slots by doctor", e)
            emptyList()
        }
    }

    suspend fun getAvailableSlots(doctorId: String): List<Slot> {
        return try {
            SecureLogger.d(TAG, "Fetching available slots for doctor")
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("doctorId", doctorId)
                            .whereEqualTo("isBooked", false)
                            .get()
                            .await()
            SecureLogger.d(TAG, "Found ${snapshot.size()} available slots")
            snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(Slot::class.java)?.copy(id = doc.id)
                    }
                    .sortedWith(compareBy({ it.date }, { it.time }))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching available slots", e)
            emptyList()
        }
    }

    suspend fun addSlot(slot: Slot): Result<String> {
        return try {
            SecureLogger.d(TAG, "Adding slot for doctor: ${slot.doctorName}")
            val docRef = db.collection("slots").add(slot).await()
            SecureLogger.d(TAG, "Slot added with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding slot", e)
            Result.failure(e)
        }
    }

    suspend fun markSlotAsBooked(slotId: String): Result<Unit> {
        return try {
            db.collection("slots").document(slotId).update("isBooked", true).await()
            SecureLogger.d(TAG, "Slot marked as booked")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error marking slot as booked", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSlot(slotId: String): Result<Unit> {
        return try {
            db.collection("slots").document(slotId).delete().await()
            SecureLogger.d(TAG, "Slot deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting slot", e)
            Result.failure(e)
        }
    }

    suspend fun getAvailableSlotsByDate(date: String): List<Slot> {
        return try {
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("date", date)
                            .whereEqualTo("isBooked", false)
                            .get()
                            .await()
            snapshot.documents
                    .mapNotNull { doc -> doc.toObject(Slot::class.java)?.copy(id = doc.id) }
                    .sortedBy { it.time }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching slots by date", e)
            emptyList()
        }
    }

    // ==================== APPOINTMENTS ====================

    suspend fun bookAppointment(appointment: Appointment): Result<String> {
        return try {
            // Mark slot as booked
            markSlotAsBooked(appointment.slotId)
            // Add appointment
            val docRef = db.collection("appointments").add(appointment).await()
            SecureLogger.d(TAG, "Appointment booked with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error booking appointment", e)
            Result.failure(e)
        }
    }

    suspend fun getUserAppointments(userId: String): List<Appointment> {
        return try {
            SecureLogger.d(TAG, "Fetching appointments for user")
            val snapshot =
                    db.collection("appointments").whereEqualTo("patientId", userId).get().await()
            SecureLogger.d(TAG, "Found ${snapshot.size()} appointments")

            val appointments =
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    }

            // Sort by bookedAt timestamp (most recent first)
            appointments.sortedByDescending { it.bookedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching user appointments", e)
            emptyList()
        }
    }

    suspend fun getAllAppointments(): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments").get().await()
            snapshot.documents
                    .mapNotNull { doc -> doc.toObject(Appointment::class.java)?.copy(id = doc.id) }
                    .sortedByDescending { it.bookedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all appointments", e)
            emptyList()
        }
    }

    suspend fun getTodaysAppointmentsCount(): Int {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = db.collection("appointments").whereEqualTo("date", today).get().await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting today's appointments", e)
            0
        }
    }

    suspend fun getTodaysAppointments(): List<Appointment> {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = db.collection("appointments").whereEqualTo("date", today).get().await()
            snapshot.documents
                    .mapNotNull { doc -> doc.toObject(Appointment::class.java)?.copy(id = doc.id) }
                    .sortedBy { it.time }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching today's appointments", e)
            emptyList()
        }
    }

    suspend fun cancelAppointment(appointmentId: String, slotId: String): Result<Unit> {
        return try {
            // Update appointment status to CANCELLED
            db.collection("appointments")
                    .document(appointmentId)
                    .update("status", "CANCELLED")
                    .await()

            // Free up the slot by setting isBooked to false
            db.collection("slots").document(slotId).update("isBooked", false).await()

            SecureLogger.d(TAG, "Appointment cancelled successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error cancelling appointment", e)
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            db.collection("appointments").document(appointmentId).update("status", status).await()
            SecureLogger.d(TAG, "Appointment status updated to: $status")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating appointment status", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            db.collection("appointments").document(appointmentId).delete().await()
            SecureLogger.d(TAG, "Appointment deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting appointment", e)
            Result.failure(e)
        }
    }

    suspend fun autoUpdateAppointmentStatuses(): Result<Int> {
        return try {
            SecureLogger.d(TAG, "Auto-updating appointment statuses")

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot =
                    db.collection("appointments").whereEqualTo("status", "CONFIRMED").get().await()

            SecureLogger.d(TAG, "Found ${snapshot.size()} CONFIRMED appointments to check")

            var updatedCount = 0
            snapshot.documents.forEach { doc ->
                val appointmentDate = doc.getString("date") ?: ""
                val isPast = appointmentDate < today

                // If appointment date is in the past, mark as COMPLETED
                if (isPast) {
                    db.collection("appointments")
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

    // ==================== MEDICAL RECORDS ====================

    suspend fun addMedicalRecord(record: MedicalRecord): Result<String> {
        return try {
            val docRef = db.collection("medical_records").add(record).await()
            SecureLogger.d(TAG, "Medical record added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding medical record", e)
            Result.failure(e)
        }
    }

    suspend fun getUserMedicalRecords(userId: String): List<MedicalRecord> {
        return try {
            SecureLogger.d(TAG, "Fetching medical records for user")
            val snapshot =
                    db.collection("medical_records").whereEqualTo("userId", userId).get().await()
            SecureLogger.d(TAG, "Found ${snapshot.size()} medical records")

            snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
                    }
                    .sortedByDescending { it.uploadedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching medical records", e)
            emptyList()
        }
    }

    suspend fun getAllMedicalRecords(): List<MedicalRecord> {
        return try {
            val snapshot = db.collection("medical_records").get().await()
            snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
                    }
                    .sortedByDescending { it.uploadedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all medical records", e)
            emptyList()
        }
    }

    suspend fun deleteMedicalRecord(recordId: String): Result<Unit> {
        return try {
            db.collection("medical_records").document(recordId).delete().await()
            SecureLogger.d(TAG, "Medical record deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting medical record", e)
            Result.failure(e)
        }
    }

    // ==================== REMINDERS ====================

    suspend fun addReminder(reminder: Reminder): Result<String> {
        return try {
            val docRef = db.collection("reminders").add(reminder).await()
            SecureLogger.d(TAG, "Reminder added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding reminder", e)
            Result.failure(e)
        }
    }

    suspend fun getUserReminders(userId: String): List<Reminder> {
        return try {
            SecureLogger.d(TAG, "Fetching reminders for user")

            // First, migrate old 'active' field to 'isActive' if needed
            migrateReminderFields()

            val snapshot =
                    db.collection("reminders")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()

            SecureLogger.d(TAG, "Found ${snapshot.size()} active reminders")

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reminder::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching reminders", e)
            emptyList()
        }
    }

    private suspend fun migrateReminderFields() {
        try {
            val snapshot = db.collection("reminders").get().await()

            var migratedCount = 0
            snapshot.documents.forEach { doc ->
                val hasActive = doc.data?.containsKey("active") == true
                val hasIsActive = doc.data?.containsKey("isActive") == true

                if (hasActive && !hasIsActive) {
                    val activeValue = doc.getBoolean("active") ?: true
                    db.collection("reminders")
                            .document(doc.id)
                            .update("isActive", activeValue)
                            .await()
                    migratedCount++
                }
            }

            if (migratedCount > 0) {
                SecureLogger.d(TAG, "Migrated $migratedCount reminders from 'active' to 'isActive'")
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error migrating reminder fields", e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            db.collection("reminders").document(reminderId).delete().await()
            SecureLogger.d(TAG, "Reminder deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting reminder", e)
            Result.failure(e)
        }
    }

    suspend fun updateReminder(reminderId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("reminders").document(reminderId).update(updates).await()
            SecureLogger.d(TAG, "Reminder updated")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating reminder", e)
            Result.failure(e)
        }
    }

    // ==================== EMERGENCY CONTACTS ====================

    suspend fun getEmergencyContacts(): List<EmergencyContact> {
        return try {
            val snapshot = db.collection("emergency_contacts").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(EmergencyContact::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching emergency contacts", e)
            emptyList()
        }
    }

    suspend fun addEmergencyContact(contact: EmergencyContact): Result<String> {
        return try {
            val docRef = db.collection("emergency_contacts").add(contact).await()
            SecureLogger.d(TAG, "Emergency contact added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding emergency contact", e)
            Result.failure(e)
        }
    }

    suspend fun updateEmergencyContact(contactId: String, contact: EmergencyContact): Result<Unit> {
        return try {
            db.collection("emergency_contacts").document(contactId).set(contact).await()
            SecureLogger.d(TAG, "Emergency contact updated")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating emergency contact", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> {
        return try {
            db.collection("emergency_contacts").document(contactId).delete().await()
            SecureLogger.d(TAG, "Emergency contact deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting emergency contact", e)
            Result.failure(e)
        }
    }

    // ==================== WELLNESS RESOURCES ====================

    suspend fun getWellnessResources(): List<WellnessResource> {
        return try {
            val snapshot = db.collection("wellness_resources").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(WellnessResource::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching wellness resources", e)
            emptyList()
        }
    }

    suspend fun addWellnessResource(resource: WellnessResource): Result<String> {
        return try {
            val docRef = db.collection("wellness_resources").add(resource).await()
            SecureLogger.d(TAG, "Wellness resource added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding wellness resource", e)
            Result.failure(e)
        }
    }

    suspend fun updateWellnessResource(
            resourceId: String,
            resource: WellnessResource
    ): Result<Unit> {
        return try {
            db.collection("wellness_resources").document(resourceId).set(resource).await()
            SecureLogger.d(TAG, "Wellness resource updated")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating wellness resource", e)
            Result.failure(e)
        }
    }

    suspend fun deleteWellnessResource(resourceId: String): Result<Unit> {
        return try {
            db.collection("wellness_resources").document(resourceId).delete().await()
            SecureLogger.d(TAG, "Wellness resource deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting wellness resource", e)
            Result.failure(e)
        }
    }
}
