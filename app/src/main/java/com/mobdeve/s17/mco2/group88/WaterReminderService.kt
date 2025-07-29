package com.mobdeve.s17.mco2.group88

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.content.SharedPreferences
import android.util.Log

class WaterReminderService : Service() {

    companion object {
        const val TAG = "WaterReminderService"
        const val ACTION_START_SERVICE = "com.mobdeve.s17.mco2.group88.START_WATER_REMINDER"
        const val ACTION_STOP_SERVICE = "com.mobdeve.s17.mco2.group88.STOP_WATER_REMINDER"
        const val ACTION_UPDATE_SETTINGS = "com.mobdeve.s17.mco2.group88.UPDATE_REMINDER_SETTINGS"
    }

    private var handler: Handler? = null
    private var reminderRunnable: Runnable? = null
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var sharedPreferences: SharedPreferences

    private var isServiceRunning = false
    private var notificationFrequencyMinutes = 30
    private var isNotificationsEnabled = true

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        notificationHelper = NotificationHelper(this)
        sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        handler = Handler(Looper.getMainLooper())

        loadNotificationSettings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                loadNotificationSettings()
                if (isNotificationsEnabled) {
                    startWaterReminders()
                }
            }
            ACTION_STOP_SERVICE -> {
                stopWaterReminders()
                stopSelf()
            }
            ACTION_UPDATE_SETTINGS -> {
                loadNotificationSettings()
                if (isNotificationsEnabled) {
                    restartWaterReminders()
                } else {
                    stopWaterReminders()
                }
            }
        }

        // Return START_STICKY so the service restarts if killed by system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopWaterReminders()
    }

    private fun loadNotificationSettings() {
        isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        notificationFrequencyMinutes = sharedPreferences.getInt("notification_frequency", 30)

        Log.d(TAG, "Loaded settings - Enabled: $isNotificationsEnabled, Frequency: $notificationFrequencyMinutes minutes")
    }

    private fun startWaterReminders() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running, stopping first")
            stopWaterReminders()
        }

        if (!isNotificationsEnabled) {
            Log.d(TAG, "Notifications disabled, not starting reminders")
            return
        }

        isServiceRunning = true
        val intervalMillis = notificationFrequencyMinutes * 60 * 1000L

        Log.d(TAG, "Starting water reminders with interval: $intervalMillis ms")

        reminderRunnable = object : Runnable {
            override fun run() {
                if (isServiceRunning && isNotificationsEnabled) {
                    Log.d(TAG, "Showing water reminder notification")
                    notificationHelper.showWaterReminderNotification()

                    // Schedule the next reminder
                    handler?.postDelayed(this, intervalMillis)
                }
            }
        }

        // Start the first reminder after the specified interval
        handler?.postDelayed(reminderRunnable!!, intervalMillis)
    }

    private fun stopWaterReminders() {
        Log.d(TAG, "Stopping water reminders")
        isServiceRunning = false

        reminderRunnable?.let { runnable ->
            handler?.removeCallbacks(runnable)
        }
        reminderRunnable = null

        // Cancel any existing notifications
        notificationHelper.cancelWaterReminderNotification()
    }

    private fun restartWaterReminders() {
        Log.d(TAG, "Restarting water reminders with new settings")
        stopWaterReminders()
        startWaterReminders()
    }
}