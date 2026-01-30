package com.example.healthmate.domain.repository

import com.example.healthmate.model.Reminder

/**
 * Repository interface for medication reminder operations.
 */
interface ReminderRepository {
    suspend fun addReminder(reminder: Reminder): Result<String>
    suspend fun getUserReminders(userId: String): List<Reminder>
    suspend fun deleteReminder(reminderId: String): Result<Unit>
    suspend fun updateReminder(reminderId: String, updates: Map<String, Any>): Result<Unit>
}
