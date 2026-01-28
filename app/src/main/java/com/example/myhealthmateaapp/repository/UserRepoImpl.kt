package com.example.myhealthmateaapp.repository

import com.example.myhealthmateaapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

abstract class   UserRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepo {

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
}
