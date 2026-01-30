package com.example.healthmate.domain.repository

import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot

/**
 * Repository interface for doctor and slot operations.
 */
interface DoctorRepository {
    suspend fun getDoctors(): List<Doctor>
    suspend fun getDoctorsCount(): Int
    suspend fun getDoctorById(doctorId: String): Doctor?
    suspend fun addDoctor(doctor: Doctor): Result<String>
    suspend fun getAllSlots(): List<Slot>
    suspend fun getSlotsByDoctor(doctorId: String): List<Slot>
    suspend fun getAvailableSlots(doctorId: String): List<Slot>
    suspend fun getAvailableSlotsByDate(date: String): List<Slot>
    suspend fun addSlot(slot: Slot): Result<String>
    suspend fun deleteSlot(slotId: String): Result<Unit>
}
