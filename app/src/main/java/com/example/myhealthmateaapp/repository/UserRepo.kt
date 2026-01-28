package com.example.myhealthmateaapp.repository

import com.example.myhealthmateaapp.model.User

interface UserRepo {

    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signup(email: String, password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun addUser(user: User)
    suspend fun getUser(uid: String): User?
    fun fetchUser(uid: String, onResult: (User?) -> Unit)
    fun saveUser(
        uid: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}
