package com.example.healthmate.data.repository

import com.example.healthmate.domain.repository.UserRepository
import com.example.healthmate.model.User
import com.example.healthmate.util.SecureLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository using Firebase Firestore.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
        private const val COLLECTION_USERS = "users"
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            doc.toObject(User::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching user", e)
            null
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("role", "USER")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error fetching users", e)
            emptyList()
        }
    }

    override suspend fun getUsersCount(): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("role", "USER")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error counting users", e)
            0
        }
    }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_USERS).document(userId).update(updates).await()
            SecureLogger.d(TAG, "User profile updated")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getUserRole(): String {
        val userId = auth.currentUser?.uid ?: return "USER"
        return try {
            val userDoc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (!userDoc.exists()) return "USER"
            userDoc.getString("role")?.uppercase() ?: "USER"
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error getting user role", e)
            "USER"
        }
    }
}
