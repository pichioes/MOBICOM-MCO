package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileMainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_mainpage)

        // Setting up Bottom Navigation
        setupBottomNavigation()

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

    // Show Goals popup
    private fun showGoalsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_goals, null)

        val confirmBtn = view.findViewById<Button>(R.id.confirm_goals_button)
        confirmBtn.setOnClickListener { dialog.dismiss() }

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
        // Clear user data (e.g., SharedPreferences, FirebaseAuth, etc.)
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all user data
        editor.apply()

        // Navigate back to login screen or home page
        val intent = Intent(this, LoginActivity::class.java)  // Navigate to your login activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear the stack
        startActivity(intent)
        finish()  // Close the current activity
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
