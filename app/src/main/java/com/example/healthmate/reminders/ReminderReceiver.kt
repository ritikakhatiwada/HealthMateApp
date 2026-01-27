package com.example.healthmate.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthmate.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicineName") ?: "Medication"
        val reminderId = intent.getStringExtra("reminderId") ?: "0"

        showNotification(context, medicineName, reminderId.hashCode())
    }

    private fun showNotification(context: Context, medicineName: String, notificationId: Int) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "medication_reminders"
        val channelName = "Medication Reminders"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                            .apply {
                                description = "Reminders for your medications"
                                enableVibration(true)
                            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification =
                NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Medication Reminder")
                        .setContentText("Time to take: $medicineName")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(notificationId, notification)
    }
}
