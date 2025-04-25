package com.example.mentalhealthemotion.Data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mentalhealthemotion.MainActivity
import com.example.mentalhealthemotion.R


class DailyMoodWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("DailyMoodWorker", "Worker is running...")
        return try {
            sendMoodNotification()
            Log.d("DailyMoodWorker", "Notification sent successfully!")
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyMoodWorker", "Error sending notification: ${e.message}")
            Result.failure()
        }
    }


    private fun sendMoodNotification() {
        Log.d("DailyMoodWorker", "sendMoodNotification() is called!")
        val channelId = "daily_mood_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Daily Mood Check-in",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder to check how you're feeling"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("How are you feeling today?")
            .setContentText("If you're feeling sad, click here for support.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.good,
                "Happy 😊",
                PendingIntent.getActivity(applicationContext, 1, Intent(), PendingIntent.FLAG_IMMUTABLE)
            )
            .addAction(
                R.drawable.awful,
                "Sad 😢",
                pendingIntent
            )
            .build()

        // Check for notification permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        }
    }
}
