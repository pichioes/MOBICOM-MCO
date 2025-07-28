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
import kotlin.math.roundToInt

class ProfileMainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_mainpage)

        // Setting up Bottom Navigation
        setupBottomNavigation()

        // Load and display user's current goal
        loadAndDisplayGoal()

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
    }

    override fun onResume() {
        super.onResume()
        // Refresh the goal display when returning to this activity
        loadAndDisplayGoal()
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

    // Get user data from SharedPreferences or Database
    private fun getUserData(): Triple<Float, Float, String>? {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)

        // Try to get user ID from SharedPreferences
        val userId = sharedPreferences.getLong("user_id", -1L)

        if (userId != -1L) {
            // Get user data from database
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

    // Show Goals popup with calculated water intake
    private fun showGoalsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_goals, null)

        val subtitleTextView = view.findViewById<TextView>(R.id.popup_subtitle)
        val goalsInput = view.findViewById<EditText>(R.id.goals_input)
        val confirmBtn = view.findViewById<Button>(R.id.confirm_goals_button)

        // Get user data and calculate suggested intake
        val userData = getUserData()
        if (userData != null) {
            val (weight, height, sex) = userData
            val suggestedIntake = calculateWaterIntake(weight, height, sex)

            // Update the subtitle with calculated suggestion
            subtitleTextView.text = "Suggested = $suggestedIntake ml"

            // Set the suggested value as hint or default value
            goalsInput.hint = "$suggestedIntake ml"
        } else {
            // If no user data available, show default message
            subtitleTextView.text = "Suggested = Please update your profile"
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
            // Implement delete account logic here
        }

        dialog.setContentView(view)
        dialog.show()
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

    // Show Notifications Popup
    private fun showNotificationsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_notifications, null)

        val closeButton = view.findViewById<ImageButton>(R.id.customizeCloseButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = view.findViewById<Button>(R.id.confirm_notifications)
        confirmButton.setOnClickListener {
            // Handle logic to confirm notification settings
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
}