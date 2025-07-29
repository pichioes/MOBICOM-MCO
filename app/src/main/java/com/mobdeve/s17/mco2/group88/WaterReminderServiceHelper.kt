package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.util.Log

object WaterReminderServiceHelper {

    private const val TAG = "WaterReminderHelper"

    /**
     * Start the water reminder service
     */
    fun startWaterReminderService(context: Context) {
        Log.d(TAG, "Starting water reminder service")
        val intent = Intent(context, WaterReminderService::class.java).apply {
            action = WaterReminderService.ACTION_START_SERVICE
        }
        context.startService(intent)
    }

    /**
     * Stop the water reminder service
     */
    fun stopWaterReminderService(context: Context) {
        Log.d(TAG, "Stopping water reminder service")
        val intent = Intent(context, WaterReminderService::class.java).apply {
            action = WaterReminderService.ACTION_STOP_SERVICE
        }
        context.startService(intent)
    }

    /**
     * Update service settings (when user changes notification preferences)
     */
    fun updateServiceSettings(context: Context) {
        Log.d(TAG, "Updating service settings")
        val intent = Intent(context, WaterReminderService::class.java).apply {
            action = WaterReminderService.ACTION_UPDATE_SETTINGS
        }
        context.startService(intent)
    }

    /**
     * Check if notifications are enabled in SharedPreferences
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    /**
     * Get notification frequency from SharedPreferences
     */
    fun getNotificationFrequency(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("notification_frequency", 30)
    }
}