package com.example.healthmate.di

import android.content.Context
import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.domain.repository.DoctorRepository
import com.example.healthmate.domain.repository.MedicalRecordRepository
import com.example.healthmate.domain.repository.ReminderRepository
import com.example.healthmate.domain.repository.UserRepository
import com.example.healthmate.data.repository.AppointmentRepositoryImpl
import com.example.healthmate.data.repository.DoctorRepositoryImpl
import com.example.healthmate.data.repository.MedicalRecordRepositoryImpl
import com.example.healthmate.data.repository.ReminderRepositoryImpl
import com.example.healthmate.data.repository.UserRepositoryImpl
import com.example.healthmate.util.ConnectivityObserver
import com.example.healthmate.util.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.healthmate.domain.repository.AuthRepository
import com.example.healthmate.data.repository.AuthRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 *
 * Provides:
 * - Repository implementations
 * - Connectivity observer
 * - Other application services
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideDoctorRepository(
        firestore: FirebaseFirestore
    ): DoctorRepository {
        return DoctorRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        firestore: FirebaseFirestore
    ): AppointmentRepository {
        return AppointmentRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideMedicalRecordRepository(
        firestore: FirebaseFirestore
    ): MedicalRecordRepository {
        return MedicalRecordRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideReminderRepository(
        firestore: FirebaseFirestore
    ): ReminderRepository {
        return ReminderRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
}
