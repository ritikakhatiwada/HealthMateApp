package com.example.healthmate.domain.repository

import com.example.healthmate.model.User

/**
 * Repository interface for user operations.
 */
interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun getUsersCount(): Int
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun getUserRole(): String
}
