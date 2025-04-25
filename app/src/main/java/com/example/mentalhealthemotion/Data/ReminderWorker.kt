package com.example.mentalhealthemotion.Data

import android.app.NotificationChannel
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mentalhealthemotion.Data.Reminder
import com.example.mentalhealthemotion.R
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Retrieve data from input data (title and description)
        val title = inputData.getString("title") ?: "Reminder"
        val description = inputData.getString("description") ?: "No Description"

        // Trigger the notification
        sendNotification(title, description)

        // Indicate that the work is done
        return Result.success()
    }

    private fun sendNotification(title: String, description: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, "reminder_channel")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismiss notification when clicked
            .build()

        // Show the notification
        notificationManager.notify(0, notification)
    }
}


fun scheduleReminderNotification(context: Context, reminder: Reminder) {
    val workManager = WorkManager.getInstance(context)
    val data = workDataOf("title" to reminder.title, "description" to reminder.description)

    // Calculate delay before the reminder
    val delay = reminder.time - System.currentTimeMillis()

    // Ensure the delay is positive; otherwise, immediately trigger it
    val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
        .setInputData(data)
        .build()

    // Enqueue the work request to be executed later
    workManager.enqueue(reminderRequest)
}
