package com.example.healthmate.auth

import com.example.healthmate.model.User
import com.example.healthmate.util.SecureLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication helper for HealthMate.
 *
 * Handles:
 * - Email/password login and signup
 * - User role management (USER/ADMIN)
 * - Session management
 * - Password reset
 */
object FirebaseAuthHelper {

    private const val TAG = "FirebaseAuthHelper"
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

            // Check if document exists
            if (!userDoc.exists()) {
                SecureLogger.w(TAG, "User document not found, defaulting to USER role")
                return Result.success("USER")
            }

            // Get role and normalize to uppercase for consistent comparison
            val rawRole = userDoc.getString("role")
            val role = rawRole?.uppercase() ?: "USER"
            SecureLogger.d(TAG, "Login successful, role: $role")

            Result.success(role)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Register new user with email and password.
     * Creates user document in Firestore with role = "USER"
     */
    suspend fun signUp(email: String, password: String, name: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Signup failed"))

            // Create user document in Firestore
            val user = User(id = userId, email = email, name = name, role = "USER")
            firestore.collection("users").document(userId).set(user).await()

            // Also update Auth profile with name
            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }
            result.user?.updateProfile(profileUpdates)?.await()

            SecureLogger.d(TAG, "User registered successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Signup error", e)
            Result.failure(e)
        }
    }

    /** Logout current user */
    fun logout() {
        auth.signOut()
        SecureLogger.d(TAG, "User logged out")
    }

    /** Get user role from Firestore */
    suspend fun getUserRole(): String {
        val userId = auth.currentUser?.uid ?: return "USER"
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                SecureLogger.w(TAG, "User document not found for role check")
                return "USER"
            }

            val rawRole = userDoc.getString("role")
            // Normalize to uppercase for consistent comparison
            rawRole?.uppercase() ?: "USER"
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error getting user role", e)
            "USER"
        }
    }

    /** Get current user's ID */
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    /** Send password reset email */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            SecureLogger.d(TAG, "Password reset email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error sending password reset email", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google using ID token.
     *
     * @param idToken The Google ID token from credential manager
     * @return Result with user role ("USER" or "ADMIN") on success
     */
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Google sign-in failed"))

            // Check if user document exists
            val userDoc = firestore.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                // Create new user document for first-time Google sign-in
                val user = User(
                    id = userId,
                    email = result.user?.email ?: "",
                    name = result.user?.displayName ?: "",
                    role = "USER",
                    profilePicture = result.user?.photoUrl?.toString() ?: ""
                )
                firestore.collection("users").document(userId).set(user).await()
                SecureLogger.d(TAG, "New Google user created")
                return Result.success("USER")
            }

            // Get existing user's role
            val rawRole = userDoc.getString("role")
            val role = rawRole?.uppercase() ?: "USER"
            SecureLogger.d(TAG, "Google sign-in successful, role: $role")

            Result.success(role)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Google sign-in error", e)
            Result.failure(e)
        }
    }

    /**
     * Link Google account to existing email/password account.
     *
     * @param idToken The Google ID token from credential manager
     * @return Result indicating success or failure
     */
    suspend fun linkGoogleAccount(idToken: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("No user logged in"))

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            currentUser.linkWithCredential(credential).await()

            // Update profile picture if available
            val googlePhotoUrl = currentUser.photoUrl?.toString()
            if (!googlePhotoUrl.isNullOrEmpty()) {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("profilePicture", googlePhotoUrl)
                    .await()
            }

            SecureLogger.d(TAG, "Google account linked successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error linking Google account", e)
            Result.failure(e)
        }
    }
}
