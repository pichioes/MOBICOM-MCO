package com.mobdeve.s17.mco2.group88
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.content.res.ColorStateList
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ProfileMainPage : AppCompatActivity() {
    private var isNotificationsEnabled = true
    private var notificationFrequencyMinutes = 30 // Default 30 minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_mainpage)

        // Setting up Bottom Navigation
        setupBottomNavigation()

        // Load and display user's current goal
        loadAndDisplayGoal()

        // Load and display user's name
        loadAndDisplayUserName()

        // Load and display user's height and weight
        loadAndDisplayHeightWeight()

        // Load notification settings from database
        loadNotificationSettingsFromDatabase()

        // Handling click for logout section
        val logoutLayer = findViewById<View>(R.id.logout_layer)
        logoutLayer.setOnClickListener {
            showLogoutPopup()  // Show the logout popup
        }

        // Handling click for delete account section
        val deleteaccLayer = findViewById<View>(R.id.deleteacc_layer)
        deleteaccLayer.setOnClickListener {
            showDeleteAccPopup()  // Show the delete account popup
        }

        // Handling click for goals section
        val goalsLayer = findViewById<View>(R.id.goals_layer)
        goalsLayer.setOnClickListener {
            showGoalsPopup()  // Show the goals popup
        }

        // Handling click for notifications section
        val notificationsLayer = findViewById<View>(R.id.notifications_layer)
        notificationsLayer.setOnClickListener {
            showNotificationsPopup()  // Show the notifications popup
        }

        // Handling click for account management
        val myAccountLayer = findViewById<View>(R.id.myaccount_layer)
        myAccountLayer.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        // Handle the profile notifications switch
        val profileNotificationSwitch = findViewById<Switch>(R.id.switch1)
        profileNotificationSwitch.isChecked = isNotificationsEnabled
        profileNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNotificationsEnabled = isChecked
            saveNotificationSettingsToDatabase()
            updateWaterReminderService()

            // Update the GlobalTimerManager with new settings
            if (isChecked) {
                // Restart timer with new frequency when enabled
                GlobalTimerManager.onWaterIntakeRecorded(this)
            } else {
                // Complete reset when disabled
                GlobalTimerManager.completeReset(this)
            }
        }

        // Start the water reminder service if notifications are enabled
        if (isNotificationsEnabled) {
            WaterReminderServiceHelper.startWaterReminderService(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the goal display when returning to this activity
        loadAndDisplayGoal()
        // Refresh the user name display
        loadAndDisplayUserName()
        // Refresh the height and weight display
        loadAndDisplayHeightWeight()
        // Reload notification settings from database
        loadNotificationSettingsFromDatabase()
    }

    // NEW: Load and display user's name from database
    private fun loadAndDisplayUserName() {
        val profileNameTextView = findViewById<TextView>(R.id.ProfileName)
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null && user.name.isNotEmpty()) {
                profileNameTextView.text = user.name
            } else {
                profileNameTextView.text = "User" // Default value
            }
        } else {
            profileNameTextView.text = "User" // Default value when not logged in
        }
    }

    // NEW: Load and display user's height and weight from database
    private fun loadAndDisplayHeightWeight() {
        val heightTextView = findViewById<TextView>(R.id.height)
        val weightTextView = findViewById<TextView>(R.id.weight)
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                // Display height with proper formatting
                if (user.height > 0) {
                    heightTextView.text = "${user.height.toInt()}cm"
                } else {
                    heightTextView.text = "-- cm" // Default when height not set
                }

                // Display weight with proper formatting
                if (user.weight > 0) {
                    weightTextView.text = "${user.weight.toInt()}kg"
                } else {
                    weightTextView.text = "-- kg" // Default when weight not set
                }
            } else {
                // User not found, show defaults
                heightTextView.text = "-- cm"
                weightTextView.text = "-- kg"
            }
        } else {
            // Not logged in, show defaults
            heightTextView.text = "-- cm"
            weightTextView.text = "-- kg"
        }
    }

    // Load notification settings from database instead of SharedPreferences
    private fun loadNotificationSettingsFromDatabase() {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                // Load from database if available
                isNotificationsEnabled = user.notificationsEnabled ?: true
                notificationFrequencyMinutes = user.notificationFrequency ?: 30
            } else {
                // Fallback to default values
                isNotificationsEnabled = true
                notificationFrequencyMinutes = 30
            }
        } else {
            // Not logged in, use defaults
            isNotificationsEnabled = true
            notificationFrequencyMinutes = 30
        }

        // Update the profile switch
        findViewById<Switch>(R.id.switch1)?.isChecked = isNotificationsEnabled

        // Also save to SharedPreferences for compatibility
        saveNotificationSettingsToSharedPrefs()
    }

    // Save notification settings to database
    private fun saveNotificationSettingsToDatabase() {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                // Update the user's notification settings
                val updatedUser = user.copy(
                    notificationsEnabled = isNotificationsEnabled,
                    notificationFrequency = notificationFrequencyMinutes,
                    updatedAt = getCurrentDateTime()
                )
                val updateResult = dbHelper.updateUser(updatedUser)
                if (updateResult > 0) {
                    // Also save to SharedPreferences for quick access
                    saveNotificationSettingsToSharedPrefs()
                } else {
                    Toast.makeText(this, "Failed to update notification settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Helper method to save to SharedPreferences (for compatibility)
    private fun saveNotificationSettingsToSharedPrefs() {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("notifications_enabled", isNotificationsEnabled)
        editor.putInt("notification_frequency", notificationFrequencyMinutes)
        editor.apply()
    }

    // Update the water reminder service based on current settings
    private fun updateWaterReminderService() {
        if (isNotificationsEnabled) {
            WaterReminderServiceHelper.startWaterReminderService(this)
        } else {
            WaterReminderServiceHelper.stopWaterReminderService(this)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Create color state lists programmatically
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // Selected state
            intArrayOf(-android.R.attr.state_checked) // Unselected state
        )

        // Define colors for the states (selected vs unselected)
        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.nav_selected_icon_color),  // Selected color
            ContextCompat.getColor(this, R.color.nav_unselected_icon_color)  // Unselected color
        )

        val colorStateList = ColorStateList(states, colors)

        // Set the color state list for both item icon and item text
        bottomNav.itemIconTintList = colorStateList
        bottomNav.itemTextColor = colorStateList

        // Set the selected item to Profile
        bottomNav.selectedItemId = R.id.nav_profile  // Ensure Profile is selected by default

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_analytics -> {
                    val intent = Intent(this, AnalyticsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }
    }

    // Load and display user's current water goal
    private fun loadAndDisplayGoal() {
        val goalDisplayTextView = findViewById<TextView>(R.id.goal_display)
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                goalDisplayTextView.text = "${user.dailyWaterGoal}ml"
            } else {
                goalDisplayTextView.text = "2150ml" // Default value
            }
        } else {
            goalDisplayTextView.text = "2150ml" // Default value when not logged in
        }
    }

    // Calculate suggested water intake based on weight, height and sex
    private fun calculateWaterIntake(weight: Float, height: Float, sex: String): Int {
        // Use the database helper's calculation method as base
        val dbHelper = AquaBuddyDatabaseHelper(this)
        val baseIntake = dbHelper.calculateRecommendedWaterIntake(weight.toDouble(), sex)

        // Optional: Add height factor for very tall or short people
        val heightFactor = when {
            height > 180 -> 1.1f  // Taller people need more water
            height < 150 -> 0.9f  // Shorter people need slightly less
            else -> 1.0f
        }

        val adjustedIntake = baseIntake * heightFactor

        // Round to nearest 50ml for cleaner numbers
        return (adjustedIntake / 50).roundToInt() * 50
    }

    // Get user data from database (weight, height, sex)
    private fun getUserDataFromDatabase(): Triple<Float, Float, String>? {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            return if (user != null && user.weight > 0 && user.height > 0) {
                Triple(user.weight.toFloat(), user.height.toFloat(), user.sex)
            } else {
                null
            }
        }
        return null
    }

    // Show Goals popup with calculated water intake based on database values
    private fun showGoalsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_goals, null)
        val subtitleTextView = view.findViewById<TextView>(R.id.popup_subtitle)
        val goalsInput = view.findViewById<EditText>(R.id.goals_input)
        val confirmBtn = view.findViewById<Button>(R.id.confirm_goals_button)

        // Get user data from database and calculate suggested intake
        val userData = getUserDataFromDatabase()
        if (userData != null) {
            val (weight, height, sex) = userData
            val suggestedIntake = calculateWaterIntake(weight, height, sex)

            // Update the subtitle with calculated suggestion
            subtitleTextView.text = "Based on your profile: Weight: ${weight.toInt()}kg, Height: ${height.toInt()}cm\nSuggested = $suggestedIntake ml"
            // Set the suggested value as hint or default value
            goalsInput.hint = "$suggestedIntake ml"
        } else {
            // If no user data available, show default message
            subtitleTextView.text = "Please update your weight and height in profile settings for personalized recommendations"
            goalsInput.hint = "Enter ml"
        }

        confirmBtn.setOnClickListener {
            // Save the goal to database
            val goalValue = goalsInput.text.toString()
            if (goalValue.isNotEmpty()) {
                val goal = goalValue.toIntOrNull() ?: 0
                if (goal > 0) {
                    saveWaterGoalToDatabase(goal)
                }
            }
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(0.5f)  // Dim the background
            setGravity(android.view.Gravity.CENTER)
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    // Save water goal to database
    private fun saveWaterGoalToDatabase(goal: Int) {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                // Update the user's daily water goal
                val updatedUser = user.copy(
                    dailyWaterGoal = goal,
                    updatedAt = getCurrentDateTime()
                )
                val updateResult = dbHelper.updateUser(updatedUser)
                if (updateResult > 0) {
                    // Also save to SharedPreferences for quick access
                    val editor = sharedPreferences.edit()
                    editor.putInt("daily_water_goal", goal)
                    editor.apply()

                    // Refresh the goal display on the main page
                    loadAndDisplayGoal()
                    Toast.makeText(this, "Water goal updated: ${goal}ml", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update water goal", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please log in to set goals", Toast.LENGTH_SHORT).show()
        }
    }

    // Show Delete Account popup
    private fun showDeleteAccPopup() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_deleteacc, null)

        val cancelBtn = view.findViewById<Button>(R.id.cancel_delete_button)
        val deleteBtn = view.findViewById<Button>(R.id.confirm_delete_button)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        deleteBtn.setOnClickListener {
            dialog.dismiss()
            deleteUserAccount() // Call the delete account function
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Handle user account deletion
    private fun deleteUserAccount() {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            val dbHelper = AquaBuddyDatabaseHelper(this)
            // Attempt to delete the user from the database
            val deleteResult = dbHelper.deleteUser(userId)

            if (deleteResult > 0) {
                // User successfully deleted from database
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                // Stop the water reminder service
                WaterReminderServiceHelper.stopWaterReminderService(this)

                // Complete reset of GlobalTimerManager
                GlobalTimerManager.completeReset(this)

                // Clear all SharedPreferences data
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                // Navigate back to login screen and clear the activity stack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Failed to delete user from database
                Toast.makeText(this, "Failed to delete account. Please try again.", Toast.LENGTH_LONG).show()
            }
        } else {
            // No user is logged in
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Show Logout popup
    private fun showLogoutPopup() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_logout, null)

        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val logoutBtn = view.findViewById<Button>(R.id.logout_button)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        logoutBtn.setOnClickListener {
            dialog.dismiss()
            logoutUser() // Call logoutUser() function when clicked
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Handle user logout functionality
    private fun logoutUser() {
        // Stop the water reminder service
        WaterReminderServiceHelper.stopWaterReminderService(this)

        // Complete reset of GlobalTimerManager
        GlobalTimerManager.completeReset(this)

        // Clear user data (e.g., SharedPreferences)
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all user data
        editor.apply()

        // Navigate back to login screen
        val intent = Intent(this, LoginActivity::class.java)  // Navigate to your login activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear the stack
        startActivity(intent)
        finish()  // Close the current activity to prevent returning back to it
    }

    // Show Notifications Popup with enhanced functionality and database saving
    private fun showNotificationsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_notifications, null)

        val closeButton = view.findViewById<ImageButton>(R.id.customizeCloseButton)
        val reminderSpinner = view.findViewById<Spinner>(R.id.reminder_spinner)
        val notificationSwitch = view.findViewById<Switch>(R.id.notification_switch)
        val confirmButton = view.findViewById<Button>(R.id.confirm_notifications)

        // Set current notification settings in the popup
        notificationSwitch.isChecked = isNotificationsEnabled

        // Setup spinner with current frequency
        val reminderOptions = arrayOf("1 minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "2 hours", "3 hours")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reminderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderSpinner.adapter = adapter

        // Set spinner to current frequency (loaded from database)
        val currentSelection = when (notificationFrequencyMinutes) {
            1 -> 0
            5 -> 1
            10 -> 2
            15 -> 3
            30 -> 4
            60 -> 5
            120 -> 6
            180 -> 7
            else -> 4 // Default to 30 minutes
        }
        reminderSpinner.setSelection(currentSelection)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            // Get new settings from popup
            val newNotificationsEnabled = notificationSwitch.isChecked
            val selectedFrequency = when (reminderSpinner.selectedItemPosition) {
                0 -> 1   // 1 minute
                1 -> 5   // 5 minutes
                2 -> 10  // 10 minutes
                3 -> 15  // 15 minutes
                4 -> 30  // 30 minutes
                5 -> 60  // 1 hour
                6 -> 120 // 2 hours
                7 -> 180 // 3 hours
                else -> 30
            }

            // Update settings
            val oldFrequency = notificationFrequencyMinutes
            isNotificationsEnabled = newNotificationsEnabled
            notificationFrequencyMinutes = selectedFrequency

            // Save settings to database
            saveNotificationSettingsToDatabase()

            // Update profile switch
            findViewById<Switch>(R.id.switch1)?.isChecked = isNotificationsEnabled

            // Update the background service with new settings
            WaterReminderServiceHelper.updateServiceSettings(this)

            // IMPORTANT: Update GlobalTimerManager with new frequency
            if (isNotificationsEnabled) {
                // If frequency changed or notifications were just enabled, restart timer
                if (oldFrequency != selectedFrequency || !newNotificationsEnabled) {
                    GlobalTimerManager.onWaterIntakeRecorded(this)
                }
            } else {
                // If notifications disabled, complete reset of timer
                GlobalTimerManager.completeReset(this)
            }

            // Show confirmation message
            val statusMessage = if (isNotificationsEnabled) {
                val timeUnit = if (notificationFrequencyMinutes < 60) "minutes" else "hours"
                val timeValue = if (notificationFrequencyMinutes < 60) notificationFrequencyMinutes else (notificationFrequencyMinutes / 60)
                "Notifications enabled every $timeValue $timeUnit"
            } else {
                "Notifications disabled"
            }
            Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(0.5f)  // Dim the background
            setGravity(android.view.Gravity.CENTER)
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    // Helper function to get current date and time
    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}