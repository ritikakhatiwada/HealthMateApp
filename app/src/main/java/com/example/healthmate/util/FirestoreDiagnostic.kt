package com.example.healthmate.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreDiagnostic {

    private const val TAG = "FirestoreDiagnostic"
    private val db = FirebaseFirestore.getInstance()

    suspend fun runDiagnostics() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "Starting Firestore Diagnostics")
        Log.d(TAG, "========================================")

        // 1. Check all doctors
        checkDoctors()

        // 2. Check all slots
        checkSlots()

        // 3. Find mismatches
        findMismatches()

        Log.d(TAG, "========================================")
        Log.d(TAG, "Diagnostics Complete")
        Log.d(TAG, "========================================")
    }

    private suspend fun checkDoctors() {
        try {
            Log.d(TAG, "\n--- DOCTORS COLLECTION ---")
            val snapshot = db.collection("doctors").get().await()
            Log.d(TAG, "Total doctors: ${snapshot.size()}")

            snapshot.documents.forEach { doc ->
                val name = doc.getString("name") ?: "N/A"
                val email = doc.getString("email") ?: "N/A"
                Log.d(TAG, "Doctor ID: '${doc.id}'")
                Log.d(TAG, "  - Name: $name")
                Log.d(TAG, "  - Email: $email")
                Log.d(TAG, "  - Document fields: ${doc.data?.keys}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking doctors: ${e.message}", e)
        }
    }

    private suspend fun checkSlots() {
        try {
            Log.d(TAG, "\n--- SLOTS COLLECTION ---")
            val snapshot = db.collection("slots").get().await()
            Log.d(TAG, "Total slots: ${snapshot.size()}")

            snapshot.documents.forEach { doc ->
                val doctorId = doc.getString("doctorId") ?: "N/A"
                val doctorName = doc.getString("doctorName") ?: "N/A"
                val date = doc.getString("date") ?: "N/A"
                val time = doc.getString("time") ?: "N/A"
                val isBooked = doc.getBoolean("isBooked") ?: false

                Log.d(TAG, "Slot ID: ${doc.id}")
                Log.d(TAG, "  - doctorId: '$doctorId'")
                Log.d(TAG, "  - doctorName: '$doctorName'")
                Log.d(TAG, "  - date: $date")
                Log.d(TAG, "  - time: $time")
                Log.d(TAG, "  - isBooked: $isBooked")
                Log.d(TAG, "  - Document fields: ${doc.data?.keys}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking slots: ${e.message}", e)
        }
    }

    private suspend fun findMismatches() {
        try {
            Log.d(TAG, "\n--- CHECKING FOR MISMATCHES ---")

            val doctorsSnapshot = db.collection("doctors").get().await()
            val slotsSnapshot = db.collection("slots").get().await()

            val doctorIds = doctorsSnapshot.documents.map { it.id }.toSet()
            val slotDoctorIds = slotsSnapshot.documents.mapNotNull { it.getString("doctorId") }.toSet()

            Log.d(TAG, "Doctor IDs in 'doctors' collection: $doctorIds")
            Log.d(TAG, "Doctor IDs referenced in 'slots' collection: $slotDoctorIds")

            // Find slots that reference non-existent doctors
            val orphanedSlots = slotDoctorIds - doctorIds
            if (orphanedSlots.isNotEmpty()) {
                Log.e(TAG, "⚠️ WARNING: Found slots referencing non-existent doctors: $orphanedSlots")
            } else {
                Log.d(TAG, "✓ All slots reference valid doctors")
            }

            // Find doctors without any slots
            val doctorsWithoutSlots = doctorIds - slotDoctorIds
            if (doctorsWithoutSlots.isNotEmpty()) {
                Log.d(TAG, "ℹ️ Doctors without slots: $doctorsWithoutSlots")
            }

            // Check for "Rischal Doctor" specifically
            val rischalDoctor = doctorsSnapshot.documents.find {
                it.getString("name")?.contains("Rischal", ignoreCase = true) == true
            }

            if (rischalDoctor != null) {
                Log.d(TAG, "\n--- RISCHAL DOCTOR ANALYSIS ---")
                Log.d(TAG, "Rischal Doctor ID: '${rischalDoctor.id}'")

                val rischalSlots = slotsSnapshot.documents.filter {
                    it.getString("doctorId") == rischalDoctor.id
                }
                Log.d(TAG, "Slots for Rischal Doctor: ${rischalSlots.size}")

                rischalSlots.forEach { slot ->
                    Log.d(TAG, "  - ${slot.getString("date")} ${slot.getString("time")} (isBooked: ${slot.getBoolean("isBooked")})")
                }

                val availableRischalSlots = rischalSlots.filter { it.getBoolean("isBooked") == false }
                Log.d(TAG, "Available slots for Rischal Doctor: ${availableRischalSlots.size}")
            } else {
                Log.e(TAG, "⚠️ 'Rischal Doctor' not found in database!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error finding mismatches: ${e.message}", e)
        }
    }

    suspend fun testQuery(doctorId: String) {
        try {
            Log.d(TAG, "\n--- TESTING QUERY FOR DOCTOR: '$doctorId' ---")

            // Test query 1: All slots for this doctor
            val allSlotsQuery = db.collection("slots")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()
            Log.d(TAG, "Query 1 - All slots for doctor: ${allSlotsQuery.size()} results")

            // Test query 2: Available slots for this doctor
            val availableSlotsQuery = db.collection("slots")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("isBooked", false)
                .get()
                .await()
            Log.d(TAG, "Query 2 - Available slots for doctor: ${availableSlotsQuery.size()} results")

            availableSlotsQuery.documents.forEach { doc ->
                Log.d(TAG, "  - ${doc.getString("date")} ${doc.getString("time")}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error testing query: ${e.message}", e)
        }
    }
}
