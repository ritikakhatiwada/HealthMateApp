package com.example.healthmate.data.repository

import com.example.healthmate.domain.repository.DoctorRepository
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot
import com.example.healthmate.util.SecureLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DoctorRepository using Firebase Firestore.
 */
@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DoctorRepository {

    companion object {
        private const val TAG = "DoctorRepository"
        private const val COLLECTION_DOCTORS = "doctors"
        private const val COLLECTION_SLOTS = "slots"
    }

    override suspend fun getDoctors(): List<Doctor> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DOCTORS).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Doctor::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching doctors", e)
            emptyList()
        }
    }

    override suspend fun getDoctorsCount(): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_DOCTORS).get().await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting doctors", e)
            0
        }
    }

    override suspend fun getDoctorById(doctorId: String): Doctor? {
        return try {
            val doc = firestore.collection(COLLECTION_DOCTORS).document(doctorId).get().await()
            doc.toObject(Doctor::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching doctor by ID", e)
            null
        }
    }

    override suspend fun addDoctor(doctor: Doctor): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_DOCTORS).add(doctor).await()
            SecureLogger.d(TAG, "Doctor added with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding doctor", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllSlots(): List<Slot> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SLOTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Slot::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all slots", e)
            emptyList()
        }
    }

    override suspend fun getSlotsByDoctor(doctorId: String): List<Slot> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SLOTS)
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Slot::class.java)?.copy(id = doc.id) }
                .sortedWith(compareBy({ it.date }, { it.time }))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching slots by doctor", e)
            emptyList()
        }
    }

    override suspend fun getAvailableSlots(doctorId: String): List<Slot> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SLOTS)
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("isBooked", false)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Slot::class.java)?.copy(id = doc.id) }
                .sortedWith(compareBy({ it.date }, { it.time }))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching available slots", e)
            emptyList()
        }
    }

    override suspend fun getAvailableSlotsByDate(date: String): List<Slot> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SLOTS)
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

    override suspend fun addSlot(slot: Slot): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_SLOTS).add(slot).await()
            SecureLogger.d(TAG, "Slot added with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding slot", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSlot(slotId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_SLOTS).document(slotId).delete().await()
            SecureLogger.d(TAG, "Slot deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting slot", e)
            Result.failure(e)
        }
    }
}
