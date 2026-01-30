package com.example.healthmate.data.repository

import com.example.healthmate.domain.repository.MedicalRecordRepository
import com.example.healthmate.model.MedicalRecord
import com.example.healthmate.util.SecureLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MedicalRecordRepository using Firebase Firestore.
 */
@Singleton
class MedicalRecordRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MedicalRecordRepository {

    companion object {
        private const val TAG = "MedicalRecordRepository"
        private const val COLLECTION_MEDICAL_RECORDS = "medical_records"
    }

    override suspend fun addMedicalRecord(record: MedicalRecord): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_MEDICAL_RECORDS).add(record).await()
            SecureLogger.d(TAG, "Medical record added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding medical record", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserMedicalRecords(userId: String): List<MedicalRecord> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MEDICAL_RECORDS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id) }
                .sortedByDescending { it.uploadedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching medical records", e)
            emptyList()
        }
    }

    override suspend fun getAllMedicalRecords(): List<MedicalRecord> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MEDICAL_RECORDS).get().await()
            snapshot.documents
                .mapNotNull { doc -> doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id) }
                .sortedByDescending { it.uploadedAt }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching all medical records", e)
            emptyList()
        }
    }

    override suspend fun deleteMedicalRecord(recordId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_MEDICAL_RECORDS)
                .document(recordId)
                .delete()
                .await()
            SecureLogger.d(TAG, "Medical record deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting medical record", e)
            Result.failure(e)
        }
    }
}
