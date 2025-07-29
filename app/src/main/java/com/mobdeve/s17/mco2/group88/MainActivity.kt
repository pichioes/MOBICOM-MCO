package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            // User is logged in, start water reminder service if notifications are enabled
            if (WaterReminderServiceHelper.areNotificationsEnabled(this)) {
                WaterReminderServiceHelper.startWaterReminderService(this)
            }

            // Navigate to main app (you might want to go to HomeActivity instead)
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User not logged in, go to login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}