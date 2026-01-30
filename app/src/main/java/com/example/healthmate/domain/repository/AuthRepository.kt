package com.example.healthmate.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String, name: String): Result<Unit>
    fun logout()
    fun getCurrentUserId(): String
    fun isLoggedIn(): Boolean
}
