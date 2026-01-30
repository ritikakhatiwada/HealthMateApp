package com.example.healthmate

import android.app.Application
import com.example.healthmate.util.SecureLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.hilt.android.HiltAndroidApp

/**
 * HealthMate Application class.
 *
 * Initializes:
 * - Hilt dependency injection
 * - Firebase with offline persistence
 * - Application-wide configurations
 */
@HiltAndroidApp
class HealthMateApplication : Application() {

    companion object {
        private const val TAG = "HealthMateApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence for reliability
        configureFirestore()

        SecureLogger.d(TAG, "HealthMate Application initialized")
    }

    private fun configureFirestore() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build())
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings
            SecureLogger.d(TAG, "Firestore offline persistence enabled")
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error configuring Firestore", e)
        }
    }
}
