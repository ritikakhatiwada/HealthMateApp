package com.example.healthmate.data.repository

import com.example.healthmate.domain.repository.ReminderRepository
import com.example.healthmate.model.Reminder
import com.example.healthmate.util.SecureLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ReminderRepository using Firebase Firestore.
 */
@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReminderRepository {

    companion object {
        private const val TAG = "ReminderRepository"
        private const val COLLECTION_REMINDERS = "reminders"
    }

    override suspend fun addReminder(reminder: Reminder): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_REMINDERS).add(reminder).await()
            SecureLogger.d(TAG, "Reminder added")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error adding reminder", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserReminders(userId: String): List<Reminder> {
        return try {
            // First, migrate old 'active' field to 'isActive' if needed
            migrateReminderFields()

            val snapshot = firestore.collection(COLLECTION_REMINDERS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reminder::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching reminders", e)
            emptyList()
        }
    }

    override suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_REMINDERS)
                .document(reminderId)
                .delete()
                .await()
            SecureLogger.d(TAG, "Reminder deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error deleting reminder", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReminder(reminderId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_REMINDERS)
                .document(reminderId)
                .update(updates)
                .await()
            SecureLogger.d(TAG, "Reminder updated")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating reminder", e)
            Result.failure(e)
        }
    }

    private suspend fun migrateReminderFields() {
        try {
            val snapshot = firestore.collection(COLLECTION_REMINDERS).get().await()

            var migratedCount = 0
            snapshot.documents.forEach { doc ->
                val hasActive = doc.data?.containsKey("active") == true
                val hasIsActive = doc.data?.containsKey("isActive") == true

                if (hasActive && !hasIsActive) {
                    val activeValue = doc.getBoolean("active") ?: true
                    firestore.collection(COLLECTION_REMINDERS)
                        .document(doc.id)
                        .update("isActive", activeValue)
                        .await()
                    migratedCount++
                }
            }

            if (migratedCount > 0) {
                SecureLogger.d(TAG, "Migrated $migratedCount reminders")
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error migrating reminder fields", e)
        }
    }
}
