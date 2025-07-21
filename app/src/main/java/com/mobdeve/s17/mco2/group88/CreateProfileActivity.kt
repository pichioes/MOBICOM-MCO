package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CreateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_createprofile)

        // Initialize views
        val backButton = findViewById<ImageButton>(R.id.createprofileBackButton)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val genderGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)

        val dbHelper = AquaBuddyDatabaseHelper(this)
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)

        // Retrieve the email from SharedPreferences to pass to User object
        val email = sharedPreferences.getString("user_email", "") ?: ""

        // Retrieve userId from SharedPreferences (stored during signup)
        val userId = sharedPreferences.getLong("user_id", -1L)

        // Back navigation
        backButton.setOnClickListener {
            finish()
        }

        // Continue Button logic
        continueButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val height = heightInput.text.toString().trim()
            val weight = weightInput.text.toString().trim()
            val selectedGenderId = genderGroup.checkedRadioButtonId
            val selectedGender = findViewById<RadioButton>(selectedGenderId)?.text.toString()

            // Validation for missing fields
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill in all profile fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save profile data in SQLite database
            val updatedUser = User(
                id = userId,
                email = email,  // Pass the email retrieved from SharedPreferences
                name = name,
                age = age.toInt(),
                height = height.toDouble(),
                weight = weight.toDouble(),
                sex = selectedGender,
                dailyWaterGoal = 2000, // Default or from input
                notificationFrequency = 60 // Default or from input
            )

            // Update user profile in the database
            dbHelper.updateUser(updatedUser)

            // Show confirmation popup
            showAccountCreatedPopup(name)
        }
    }

    // Function to show the account created popup
    private fun showAccountCreatedPopup(name: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_accountcreated, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        // Popup size and transparency
        dialog.window?.apply {
            setLayout(
                resources.getDimensionPixelSize(R.dimen.popup_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER)
        }

        val okButton = dialogView.findViewById<Button>(R.id.btnGetStarted)
        okButton.setOnClickListener {
            dialog.dismiss()

            // Show a welcome message
            Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()

            // After completing the profile, navigate to HomeActivity (or another activity)
            navigateToHome()
        }
    }

    // Function to navigate to HomeActivity (or wherever you want after profile creation)
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()  // Close this activity so the user can't go back to it
    }
}
