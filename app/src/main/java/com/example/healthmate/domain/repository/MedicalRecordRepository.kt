package com.example.healthmate.domain.repository

import com.example.healthmate.model.MedicalRecord

/**
 * Repository interface for medical record operations.
 */
interface MedicalRecordRepository {
    suspend fun addMedicalRecord(record: MedicalRecord): Result<String>
    suspend fun getUserMedicalRecords(userId: String): List<MedicalRecord>
    suspend fun getAllMedicalRecords(): List<MedicalRecord>
    suspend fun deleteMedicalRecord(recordId: String): Result<Unit>
}
