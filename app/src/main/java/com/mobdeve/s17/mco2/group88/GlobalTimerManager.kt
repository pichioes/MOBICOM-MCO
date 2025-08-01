package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.widget.TextView
import java.util.*

// Global Timer Manager - Singleton to persist across activities
object GlobalTimerManager {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var timerStartTime: Long = 0
    private var isTimerRunning = false
    private var notificationHelper: NotificationHelper? = null
    private var currentTextView: TextView? = null

    private const val PREFS_NAME = "GlobalTimerPrefs"
    private const val KEY_TIMER_START_TIME = "timer_start_time"
    private const val KEY_IS_TIMER_RUNNING = "is_timer_running"

    fun initialize(context: Context, textView: TextView, notifHelper: NotificationHelper) {
        notificationHelper = notifHelper
        currentTextView = textView
        loadTimerState(context)

        // CHECK IF NOTIFICATIONS ARE ENABLED FIRST
        if (!areNotificationsEnabled(context)) {
            // Notifications disabled, don't start timer
            completeReset(context)
            currentTextView?.text = "Notifs OFF"
            return
        }

        if (isTimerRunning && timerStartTime > 0) {
            // Timer was already running, check if it should still be running
            val elapsedMinutes = ((System.currentTimeMillis() - timerStartTime) / 60000).toInt()
            val notificationFrequency = getUserNotificationFrequency(context)

            if (elapsedMinutes >= notificationFrequency) {
                // Timer has expired, handle expiration and start new cycle
                handleTimerExpiration(context)
            } else {
                // Timer is still valid, just update display and ensure it's running
                updateTimerDisplay(context)
                ensureTimerIsRunning(context)
            }
        } else {
            // No timer running or invalid state, start fresh timer
            startNewTimerCycle(context)
        }
    }

    private fun saveTimerState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_TIMER_START_TIME, timerStartTime)
            .putBoolean(KEY_IS_TIMER_RUNNING, isTimerRunning)
            .apply()
    }

    private fun loadTimerState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        timerStartTime = prefs.getLong(KEY_TIMER_START_TIME, 0)
        isTimerRunning = prefs.getBoolean(KEY_IS_TIMER_RUNNING, false)
    }

    private fun startNewTimerCycle(context: Context) {
        // CHECK IF NOTIFICATIONS ARE ENABLED
        if (!areNotificationsEnabled(context)) {
            completeReset(context)
            currentTextView?.text = "Notifs OFF"
            return
        }

        // Cancel any existing timer
        timerTask?.cancel()
        timer?.cancel()

        timerStartTime = System.currentTimeMillis()
        isTimerRunning = true
        saveTimerState(context)

        ensureTimerIsRunning(context)
    }

    private fun ensureTimerIsRunning(context: Context) {
        // CHECK IF NOTIFICATIONS ARE ENABLED
        if (!areNotificationsEnabled(context)) {
            completeReset(context)
            currentTextView?.text = "Notifs OFF"
            return
        }

        // Cancel existing timer first
        timerTask?.cancel()
        timer?.cancel()

        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                // Double-check notifications are still enabled
                if (!areNotificationsEnabled(context)) {
                    completeReset(context)
                    return
                }

                updateTimerDisplay(context)

                val elapsedMinutes = ((System.currentTimeMillis() - timerStartTime) / 60000).toInt()
                val notificationFrequency = getUserNotificationFrequency(context)
                val timeLeft = notificationFrequency - elapsedMinutes

                if (timeLeft <= 0) {
                    // Timer expired naturally
                    handleTimerExpiration(context)
                }
            }
        }

        // Update every 30 seconds
        timer?.scheduleAtFixedRate(timerTask, 0, 30000)
    }

    private fun handleTimerExpiration(context: Context) {
        // CHECK IF NOTIFICATIONS ARE STILL ENABLED
        if (!areNotificationsEnabled(context)) {
            completeReset(context)
            return
        }

        // Show notification
        notificationHelper?.showWaterReminderNotification()

        // Cancel current timer
        timerTask?.cancel()
        timer?.cancel()

        // Start new timer cycle
        startNewTimerCycle(context)
    }

    fun updateTimerDisplay(context: Context) {
        val notificationFrequency = getUserNotificationFrequency(context)
        val elapsedMinutes = ((System.currentTimeMillis() - timerStartTime) / 60000).toInt()
        val timeLeft = notificationFrequency - elapsedMinutes

        currentTextView?.post {
            if (!areNotificationsEnabled(context)) {
                currentTextView?.text = "Notifs OFF"
            } else if (timeLeft > 0) {
                currentTextView?.text = "$timeLeft Mins"
            } else {
                currentTextView?.text = "Time to drink!"
            }
        }
    }

    // This should ONLY be called when user actually drinks water
    fun onWaterIntakeRecorded(context: Context) {
        // Always cancel existing timer and notifications when user drinks water
        timerTask?.cancel()
        timer?.cancel()
        notificationHelper?.cancelWaterReminderNotification()

        // CHECK IF NOTIFICATIONS ARE ENABLED - only restart if enabled
        if (areNotificationsEnabled(context)) {
            // Notifications are on, start new timer cycle
            startNewTimerCycle(context)
        } else {
            // Notifications are off, keep them off but clear timer state
            isTimerRunning = false
            timerStartTime = 0
            saveTimerState(context)
            currentTextView?.post {
                currentTextView?.text = "Notifs OFF"
            }
        }
    }

    fun updateTextView(textView: TextView) {
        currentTextView = textView
    }

    private fun getUserNotificationFrequency(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("notification_frequency", 30)
    }

    // NEW: Check if notifications are enabled
    private fun areNotificationsEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    fun cleanup() {
        timerTask?.cancel()
        timer?.cancel()
        // Don't reset the timer state - keep it for when app resumes
        currentTextView = null
    }

    // Method to completely stop and reset timer (for when user logs out, etc.)
    fun completeReset(context: Context) {
        timerTask?.cancel()
        timer?.cancel()
        notificationHelper?.cancelWaterReminderNotification()

        isTimerRunning = false
        timerStartTime = 0
        saveTimerState(context)
        currentTextView?.post {
            currentTextView?.text = "Notifs OFF"
        }
    }
}