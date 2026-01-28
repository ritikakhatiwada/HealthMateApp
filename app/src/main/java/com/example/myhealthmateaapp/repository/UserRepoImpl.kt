package com.example.myhealthmateaapp.repository

import com.example.myhealthmateaapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserRepo {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    override suspend fun getUser(uid: String): User? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
    }

    override fun fetchUser(uid: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject(User::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    override fun saveUser(
        uid: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = User(uid = uid, name = name, email = email)
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Failed to save user") }
    }
}
