package com.example.healthmate.auth

import com.example.healthmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Simple Firebase Authentication helper. Handles login, signup, logout, and user role management.
 */
object FirebaseAuthHelper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /** Get currently logged-in user (null if not logged in) */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /** Check if user is logged in */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Login with email and password
     * @return Result with user role ("USER" or "ADMIN") on success
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Login failed"))

            // Fetch user role from Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role") ?: "USER"

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register new user with email and password Creates user document in Firestore with role =
     * "USER"
     */
    suspend fun signUp(email: String, password: String, name: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Signup failed"))

            // Create user document in Firestore
            val user = User(id = userId, email = email, name = name, role = "USER")
            firestore.collection("users").document(userId).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Logout current user */
    fun logout() {
        auth.signOut()
    }

    /** Get user role from Firestore */
    suspend fun getUserRole(): String {
        val userId = auth.currentUser?.uid ?: return "USER"
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            userDoc.getString("role") ?: "USER"
        } catch (e: Exception) {
            "USER"
        }
    }

    /** Get current user's ID */
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    /** Send password reset email */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
