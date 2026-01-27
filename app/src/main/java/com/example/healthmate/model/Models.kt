package com.example.healthmate.model

import com.google.firebase.Timestamp

// User Models
data class User(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val role: String = "USER", // "USER" or "ADMIN"
        val createdAt: Timestamp? = null,
        val age: String = "",
        val gender: String = "",
        val bloodGroup: String = "",
        val profilePicture: String = "",
        val phoneNumber: String = "",
        val address: String = ""
)

// Doctor Models 2.0 - Fully Expanded
data class Doctor(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val age: String = "",
        val gender: String = "",
        val bloodGroup: String = "",
        val profilePicture: String = "", // URL or Base64 placeholder
        val description: String = "",
        val specialization: String = "",
        val experience: String = "",
        val education: String = "",
        val contactNumber: String = "",
        val address: String = "",
        val availableSlots: List<TimeSlot> = emptyList()
)

data class TimeSlot(
        val dayOfWeek: String = "", // e.g., "Monday"
        val startTime: String = "", // "10:00"
        val endTime: String = "", // "11:00"
        val isBooked: Boolean = false,
        val date: String? = null // Optional specific date "2024-02-01"
)

// Root Collection Entity for efficient querying
data class Slot(
        val id: String = "",
        val doctorId: String = "",
        val doctorName: String = "", // Denormalized for fewer reads
        val date: String = "", // "2024-02-01"
        val time: String = "", // "10:00" - "11:00"
        val isBooked: Boolean = false
)

// Appointment Model
data class Appointment(
        val id: String = "",
        val patientId: String = "",
        val patientName: String = "",
        val doctorId: String = "",
        val doctorName: String = "",
        val slotId: String = "", // Link to the booked slot
        val date: String = "", // "2024-02-01"
        val time: String = "", // "10:00"
        val status: String = "CONFIRMED",
        val createdAt: Timestamp = Timestamp.now(),
        val bookedAt: Long = System.currentTimeMillis() // Added for legacy compatibility if needed
)

// Other Models
data class MedicalRecord(
        val id: String = "",
        val userId: String = "",
        val fileName: String = "",
        val fileUrl: String = "",
        val uploadedAt: Long =
                System.currentTimeMillis() // Changed Timestamp to Long to match typical usage in
// this app or converting
)
// Wait, MedicalRecord in Activity used uploadedAt: Timestamp previously?
// FirestoreHelper: uploadedAt query. Activity: Date(record.uploadedAt).
// If Activity does Date(timestamp), it fails. Date expects Long.
// So MedicalRecord should probably use Long for uploadedAt to be safe with Date constructor.
// Reviewing MedicalRecordsActivity: date format uses Date(record.uploadedAt).
// So I will use Long.

data class WellnessResource(
        val id: String = "",
        val title: String = "",
        val content: String = "",
        val type: String = "Article",
        val helplineNumber: String = "" // Added field
)

data class EmergencyContact(
        val id: String = "",
        val name: String = "",
        val number: String = "",
        val type: String = "Ambulance"
)

data class Reminder(
        val id: String = "",
        val userId: String = "",
        val medicineName: String = "",
        val time: String = "",
        val isActive: Boolean = true // Renamed enabled -> isActive
)
