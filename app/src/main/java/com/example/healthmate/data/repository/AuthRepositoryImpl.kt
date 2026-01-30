package com.example.healthmate.data.repository

import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    override suspend fun login(email: String, password: String): Result<String> {
        return FirebaseAuthHelper.login(email, password)
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<Unit> {
        return FirebaseAuthHelper.signUp(email, password, name)
    }

    override fun logout() {
        FirebaseAuthHelper.logout()
    }

    override fun getCurrentUserId(): String {
        return FirebaseAuthHelper.getCurrentUserId()
    }

    override fun isLoggedIn(): Boolean {
        return FirebaseAuthHelper.isLoggedIn()
    }
}
