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

    suspend fun getAllSlots(): List<Slot> {
        return try {
            android.util.Log.d("FirestoreHelper", "Fetching ALL slots from database")
            val snapshot = db.collection("slots").get().await()
            android.util.Log.d("FirestoreHelper", "Total slots in database: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                val slot = doc.toObject(Slot::class.java)?.copy(id = doc.id)
                android.util.Log.d("FirestoreHelper", "Slot: id=${doc.id}, doctorId='${slot?.doctorId}', doctorName='${slot?.doctorName}', isBooked=${slot?.isBooked}")
                slot
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching all slots: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getSlotsByDoctor(doctorId: String): List<Slot> {
        return try {
            android.util.Log.d("FirestoreHelper", "getSlotsByDoctor: Querying for doctorId: '$doctorId'")
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("doctorId", doctorId)
                            .get()
                            .await()
            android.util.Log.d("FirestoreHelper", "getSlotsByDoctor: Found ${snapshot.size()} slots")
            snapshot.documents.mapNotNull { doc ->
                val slot = doc.toObject(Slot::class.java)?.copy(id = doc.id)
                android.util.Log.d("FirestoreHelper", "getSlotsByDoctor: Slot ${doc.id} - doctorId='${slot?.doctorId}', isBooked=${slot?.isBooked}")
                slot
            }.sortedWith(compareBy({ it.date }, { it.time }))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching slots by doctor '$doctorId': ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAvailableSlots(doctorId: String): List<Slot> {
        return try {
            android.util.Log.d("FirestoreHelper", "Querying slots for doctorId: '$doctorId'")
            val snapshot =
                    db.collection("slots")
                            .whereEqualTo("doctorId", doctorId)
                            .whereEqualTo("isBooked", false)
                            .get()
                            .await()
            android.util.Log.d("FirestoreHelper", "Query returned ${snapshot.size()} documents")
            val slots = snapshot.documents.mapNotNull { doc ->
                val slot = doc.toObject(Slot::class.java)?.copy(id = doc.id)
                android.util.Log.d("FirestoreHelper", "Slot: id=${doc.id}, doctorId=${slot?.doctorId}, date=${slot?.date}, time=${slot?.time}")
                slot
            }.sortedWith(compareBy({ it.date }, { it.time }))
            android.util.Log.d("FirestoreHelper", "Returning ${slots.size} sorted slots")
            slots
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching available slots for doctorId '$doctorId': ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addSlot(slot: Slot): Result<String> {
        return try {
            android.util.Log.d("FirestoreHelper", "Adding slot: doctorId='${slot.doctorId}', doctorName='${slot.doctorName}', date='${slot.date}', time='${slot.time}'")
            val docRef = db.collection("slots").add(slot).await()
            android.util.Log.d("FirestoreHelper", "Slot added successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error adding slot: ${e.message}", e)
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
            }.sortedBy { it.time }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching slots by date: ${e.message}", e)
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
            android.util.Log.d("FirestoreHelper", "========================================")
            android.util.Log.d("FirestoreHelper", "getUserAppointments called")
            android.util.Log.d("FirestoreHelper", "User ID: '$userId'")

            val snapshot =
                    db.collection("appointments")
                            .whereEqualTo("patientId", userId)
                            .get()
                            .await()

            android.util.Log.d("FirestoreHelper", "Query returned ${snapshot.size()} documents")

            val appointments = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("FirestoreHelper", "Processing document: ${doc.id}")
                android.util.Log.d("FirestoreHelper", "  - Document data: ${doc.data}")

                val appointment = doc.toObject(Appointment::class.java)?.copy(id = doc.id)

                if (appointment != null) {
                    android.util.Log.d("FirestoreHelper", "  - Mapped to Appointment:")
                    android.util.Log.d("FirestoreHelper", "    - patientId: ${appointment.patientId}")
                    android.util.Log.d("FirestoreHelper", "    - patientName: ${appointment.patientName}")
                    android.util.Log.d("FirestoreHelper", "    - doctorId: ${appointment.doctorId}")
                    android.util.Log.d("FirestoreHelper", "    - doctorName: ${appointment.doctorName}")
                    android.util.Log.d("FirestoreHelper", "    - date: ${appointment.date}")
                    android.util.Log.d("FirestoreHelper", "    - time: ${appointment.time}")
                    android.util.Log.d("FirestoreHelper", "    - status: ${appointment.status}")
                } else {
                    android.util.Log.w("FirestoreHelper", "  - Failed to map to Appointment object")
                }

                appointment
            }

            android.util.Log.d("FirestoreHelper", "Returning ${appointments.size} appointments")
            android.util.Log.d("FirestoreHelper", "========================================")

            // Sort by bookedAt timestamp (most recent first)
            appointments.sortedByDescending { it.bookedAt }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching user appointments for userId '$userId': ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAllAppointments(): List<Appointment> {
        return try {
            val snapshot =
                    db.collection("appointments")
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.bookedAt }
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
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }.sortedBy { it.time }
        } catch (e: Exception) {
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
            db.collection("slots")
                .document(slotId)
                .update("isBooked", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error cancelling appointment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            db.collection("appointments")
                .document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error updating appointment status: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun autoUpdateAppointmentStatuses(): Result<Int> {
        return try {
            android.util.Log.d("FirestoreHelper", "========================================")
            android.util.Log.d("FirestoreHelper", "Auto-updating appointment statuses...")

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            android.util.Log.d("FirestoreHelper", "Today's date: $today")

            val snapshot = db.collection("appointments")
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            android.util.Log.d("FirestoreHelper", "Found ${snapshot.size()} CONFIRMED appointments")

            var updatedCount = 0
            snapshot.documents.forEach { doc ->
                val appointmentDate = doc.getString("date") ?: ""
                val doctorName = doc.getString("doctorName") ?: "Unknown"
                val isPast = appointmentDate < today

                android.util.Log.d("FirestoreHelper", "Checking appointment:")
                android.util.Log.d("FirestoreHelper", "  - ID: ${doc.id}")
                android.util.Log.d("FirestoreHelper", "  - Doctor: $doctorName")
                android.util.Log.d("FirestoreHelper", "  - Date: $appointmentDate")
                android.util.Log.d("FirestoreHelper", "  - Is Past: $isPast (date '$appointmentDate' < today '$today')")

                // If appointment date is in the past, mark as COMPLETED
                if (isPast) {
                    android.util.Log.d("FirestoreHelper", "  → Updating to COMPLETED")
                    db.collection("appointments")
                        .document(doc.id)
                        .update("status", "COMPLETED")
                        .await()
                    updatedCount++
                } else {
                    android.util.Log.d("FirestoreHelper", "  → Keeping as CONFIRMED")
                }
            }

            android.util.Log.d("FirestoreHelper", "Auto-updated $updatedCount appointments to COMPLETED")
            android.util.Log.d("FirestoreHelper", "========================================")
            Result.success(updatedCount)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error auto-updating appointments: ${e.message}", e)
            Result.failure(e)
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
            android.util.Log.d("FirestoreHelper", "========================================")
            android.util.Log.d("FirestoreHelper", "getUserMedicalRecords called for userId: '$userId'")

            val snapshot =
                    db.collection("medical_records")
                            .whereEqualTo("userId", userId)
                            .get()
                            .await()

            android.util.Log.d("FirestoreHelper", "Query returned ${snapshot.size()} medical records")

            val records = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("FirestoreHelper", "Processing medical record: ${doc.id}")
                android.util.Log.d("FirestoreHelper", "  - Document data: ${doc.data}")

                val record = doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)

                if (record != null) {
                    android.util.Log.d("FirestoreHelper", "  - Mapped to MedicalRecord:")
                    android.util.Log.d("FirestoreHelper", "    - fileName: ${record.fileName}")
                    android.util.Log.d("FirestoreHelper", "    - fileUrl: ${record.fileUrl}")
                    android.util.Log.d("FirestoreHelper", "    - uploadedAt: ${record.uploadedAt}")
                } else {
                    android.util.Log.w("FirestoreHelper", "  - Failed to map to MedicalRecord object")
                }

                record
            }.sortedByDescending { it.uploadedAt }

            android.util.Log.d("FirestoreHelper", "Returning ${records.size} medical records")
            android.util.Log.d("FirestoreHelper", "========================================")

            records
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching medical records for userId '$userId': ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAllMedicalRecords(): List<MedicalRecord> {
        return try {
            val snapshot =
                    db.collection("medical_records")
                            .get()
                            .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.uploadedAt }
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
            android.util.Log.d("FirestoreHelper", "========================================")
            android.util.Log.d("FirestoreHelper", "getUserReminders called for userId: '$userId'")

            // First, migrate old 'active' field to 'isActive' if needed
            migrateReminderFields()

            val snapshot =
                    db.collection("reminders")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()

            android.util.Log.d("FirestoreHelper", "Query returned ${snapshot.size()} reminders")

            val reminders = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("FirestoreHelper", "Processing reminder: ${doc.id}")
                android.util.Log.d("FirestoreHelper", "  - Document data: ${doc.data}")

                val reminder = doc.toObject(Reminder::class.java)?.copy(id = doc.id)

                if (reminder != null) {
                    android.util.Log.d("FirestoreHelper", "  - Mapped to Reminder:")
                    android.util.Log.d("FirestoreHelper", "    - medicineName: ${reminder.medicineName}")
                    android.util.Log.d("FirestoreHelper", "    - time: ${reminder.time}")
                    android.util.Log.d("FirestoreHelper", "    - isActive: ${reminder.isActive}")
                } else {
                    android.util.Log.w("FirestoreHelper", "  - Failed to map to Reminder object")
                }

                reminder
            }

            android.util.Log.d("FirestoreHelper", "Returning ${reminders.size} reminders")
            android.util.Log.d("FirestoreHelper", "========================================")

            reminders
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error fetching reminders for userId '$userId': ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun migrateReminderFields() {
        try {
            android.util.Log.d("FirestoreHelper", "Checking for reminders field migration...")
            val snapshot = db.collection("reminders").get().await()

            var migratedCount = 0
            snapshot.documents.forEach { doc ->
                val hasActive = doc.data?.containsKey("active") == true
                val hasIsActive = doc.data?.containsKey("isActive") == true

                if (hasActive && !hasIsActive) {
                    val activeValue = doc.getBoolean("active") ?: true
                    android.util.Log.d("FirestoreHelper", "Migrating reminder ${doc.id}: active=$activeValue -> isActive=$activeValue")

                    db.collection("reminders").document(doc.id)
                        .update("isActive", activeValue)
                        .await()
                    migratedCount++
                }
            }

            if (migratedCount > 0) {
                android.util.Log.d("FirestoreHelper", "Migrated $migratedCount reminders from 'active' to 'isActive'")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreHelper", "Error migrating reminder fields: ${e.message}", e)
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
