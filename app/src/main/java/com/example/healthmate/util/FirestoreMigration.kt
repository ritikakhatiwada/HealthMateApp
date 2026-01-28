package com.example.healthmate.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreMigration {

    private const val TAG = "FirestoreMigration"
    private val db = FirebaseFirestore.getInstance()

    /**
     * Migrates old slot documents that use 'booked' field to use 'isBooked' field
     */
    suspend fun migrateSlotFields(): Result<Int> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "Starting Slot Field Migration")
            Log.d(TAG, "Migrating 'booked' field to 'isBooked'")
            Log.d(TAG, "========================================")

            val slotsSnapshot = db.collection("slots").get().await()
            var migratedCount = 0

            slotsSnapshot.documents.forEach { doc ->
                val hasBookedField = doc.data?.containsKey("booked") == true
                val hasIsBookedField = doc.data?.containsKey("isBooked") == true
                val slotId = doc.id

                Log.d(TAG, "Checking slot $slotId:")
                Log.d(TAG, "  - Has 'booked' field: $hasBookedField")
                Log.d(TAG, "  - Has 'isBooked' field: $hasIsBookedField")

                if (hasBookedField && !hasIsBookedField) {
                    // Old format: has 'booked' but not 'isBooked'
                    val bookedValue = doc.getBoolean("booked") ?: false

                    Log.d(TAG, "  → Migrating: booked=$bookedValue → isBooked=$bookedValue")

                    // Update the document
                    db.collection("slots").document(slotId)
                        .update(
                            mapOf(
                                "isBooked" to bookedValue
                            )
                        )
                        .await()

                    // Optional: Remove old 'booked' field
                    // Uncomment the next 3 lines if you want to remove the old field
                    // db.collection("slots").document(slotId)
                    //     .update("booked", FieldValue.delete())
                    //     .await()

                    migratedCount++
                    Log.d(TAG, "  ✓ Migrated successfully")
                } else if (hasIsBookedField) {
                    Log.d(TAG, "  ✓ Already using 'isBooked', no migration needed")
                } else {
                    Log.w(TAG, "  ⚠ No booking status field found, adding isBooked=false")
                    db.collection("slots").document(slotId)
                        .update("isBooked", false)
                        .await()
                    migratedCount++
                }
            }

            Log.d(TAG, "========================================")
            Log.d(TAG, "Migration Complete!")
            Log.d(TAG, "Total slots migrated: $migratedCount")
            Log.d(TAG, "========================================")

            Result.success(migratedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
