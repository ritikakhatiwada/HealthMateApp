package com.example.healthmate.data

import com.example.healthmate.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await

/** Firestore helper for all database operations. */
object FirestoreHelper {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // ==================== USERS ====================

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
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
            emptyList()
        }
    }

    suspend fun getUsersCount(): Int {
        return try {
            val snapshot = db.collection("users").whereEqualTo("role", "USER").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            emptyList()
        }
    }

    suspend fun getDoctorsCount(): Int {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun addDoctor(doctor: Doctor): Result<String> {
        return try {
            val docRef = db.collection("doctors").add(doctor).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorById(doctorId: String): Doctor? {
        return try {
            val doc = db.collection("doctors").document(doctorId).get().await()
            doc.toObject(Doctor::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== SLOTS ====================

    suspend fun getSlotsByDoctor(doctorId: String): List<Slot> {
        return try {
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("doctorId", doctorId)
                            .orderBy("date")
                            .orderBy("time")
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Slot::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAvailableSlots(doctorId: String): List<Slot> {
        return try {
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("doctorId", doctorId)
                            .whereEqualTo("isBooked", false)
                            .orderBy("date")
                            .orderBy("time")
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Slot::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSlot(slot: Slot): Result<String> {
        return try {
            val docRef = db.collection("slots").add(slot).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markSlotAsBooked(slotId: String): Result<Unit> {
        return try {
            db.collection("slots").document(slotId).update("isBooked", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSlot(slotId: String): Result<Unit> {
        return try {
            db.collection("slots").document(slotId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Slot::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
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
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserAppointments(userId: String): List<Appointment> {
        return try {
            val snapshot =
                    db.collection("appointments")
                            .whereEqualTo("patientId", userId) // Fixed: was using "userId"
                            .orderBy("bookedAt", Query.Direction.DESCENDING)
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllAppointments(): List<Appointment> {
        return try {
            val snapshot =
                    db.collection("appointments")
                            .orderBy("bookedAt", Query.Direction.DESCENDING)
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTodaysAppointmentsCount(): Int {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = db.collection("appointments").whereEqualTo("date", today).get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTodaysAppointments(): List<Appointment> {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot =
                    db.collection("appointments")
                            .whereEqualTo("date", today)
                            .orderBy("time")
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== MEDICAL RECORDS ====================

    suspend fun addMedicalRecord(record: MedicalRecord): Result<String> {
        return try {
            val docRef = db.collection("medical_records").add(record).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserMedicalRecords(userId: String): List<MedicalRecord> {
        return try {
            val snapshot =
                    db.collection("medical_records")
                            .whereEqualTo("userId", userId)
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllMedicalRecords(): List<MedicalRecord> {
        return try {
            val snapshot =
                    db.collection("medical_records")
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteMedicalRecord(recordId: String): Result<Unit> {
        return try {
            db.collection("medical_records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REMINDERS ====================

    suspend fun addReminder(reminder: Reminder): Result<String> {
        return try {
            val docRef = db.collection("reminders").add(reminder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserReminders(userId: String): List<Reminder> {
        return try {
            val snapshot =
                    db.collection("reminders")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reminder::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            db.collection("reminders").document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminder(reminderId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("reminders").document(reminderId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            emptyList()
        }
    }

    suspend fun addEmergencyContact(contact: EmergencyContact): Result<String> {
        return try {
            val docRef = db.collection("emergency_contacts").add(contact).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmergencyContact(contactId: String, contact: EmergencyContact): Result<Unit> {
        return try {
            db.collection("emergency_contacts").document(contactId).set(contact).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> {
        return try {
            db.collection("emergency_contacts").document(contactId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            emptyList()
        }
    }

    suspend fun addWellnessResource(resource: WellnessResource): Result<String> {
        return try {
            val docRef = db.collection("wellness_resources").add(resource).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWellnessResource(
            resourceId: String,
            resource: WellnessResource
    ): Result<Unit> {
        return try {
            db.collection("wellness_resources").document(resourceId).set(resource).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWellnessResource(resourceId: String): Result<Unit> {
        return try {
            db.collection("wellness_resources").document(resourceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
