package com.example.mentalhealthemotion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.mentalhealthemotion.Data.DailyMoodWorker
import com.example.mentalhealthemotion.ui.theme.MentalHealthEmotionTheme
import com.example.mentalhealthemotion.ui.theme.MentalHeathApp
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val REQUEST_CODE = 1001 // Permission request code

    // ✅ BroadcastReceiver for mood check navigation
    private val moodReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "NAVIGATE_TO_MAP_SCREEN") {
                runOnUiThread {
                    Toast.makeText(context, "Navigating to Map Screen...", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate() started!")
        initializeApp()
        createNotificationChannel()


        // ✅ Register BroadcastReceiver
        val filter = IntentFilter("NAVIGATE_TO_MAP_SCREEN")
        registerReceiver(moodReceiver, filter, Context.RECEIVER_NOT_EXPORTED)


        // ✅ Initialize Firebase with AppCheck
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // ✅ Check and request necessary permissions
        checkAndRequestPermissions()

        enableEdgeToEdge()

        setContent {
            MentalHealthEmotionTheme {
                MentalHeathApp()
                val navController = rememberNavController()
            }
        }
    }

    // ✅ Permission Handling
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Android 13+ Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // SMS and CALL_PHONE Permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
            permissionsToRequest.add(Manifest.permission.CALL_PHONE)
        }

        // Request Permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE
            )
        }
    }

    // ✅ Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isEmpty()) {
                // ✅ All permissions granted, proceed with app logic
                initializeApp()
            } else {
                Toast.makeText(
                    this,
                    "Some permissions were denied. Features may not work properly.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initializeApp() {
        Log.d("MainActivity", "Initializing app and scheduling daily mood check")
        scheduleDailyMoodCheck()
    }

    private fun createNotificationChannel() {
        Log.d("MainActivity", "Creating notification channel...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_mood_channel",
                "Daily Mood Check-in",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder to check how you're feeling"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("MainActivity", "Notification channel created successfully!")
        } else {
            Log.d("MainActivity", "Notification channel not needed (API < 26)")
        }
    }


    private fun scheduleDailyMoodCheck() {
        val delay = calculateDelayForNextDay()
        Log.d("DailyMoodCheck", "Scheduling worker with delay: $delay milliseconds")

        val workRequest = PeriodicWorkRequestBuilder<DailyMoodWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Add delay until the next day
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    fun calculateDelayForNextDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)  // Set the time to 9 AM
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        Log.d("DailyMoodCheck", "Calculated delay until next 9 AM: $delay milliseconds")
        return delay
    }
}




