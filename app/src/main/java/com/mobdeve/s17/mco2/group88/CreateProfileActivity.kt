package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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

        // Retrieve stored email and password from SharedPreferences
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val passwordHash = sharedPreferences.getString("user_password_hash", "") ?: ""

        // Back navigation
        backButton.setOnClickListener {
            finish()  // Close this activity and go back
        }

        // Continue Button logic for saving profile data
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
                id = 0,  // Auto-generated ID
                email = email,  // Email passed from SharedPreferences
                passwordHash = passwordHash,  // Password hash passed from SharedPreferences
                name = name,
                age = age.toInt(),
                height = height.toDouble(),
                weight = weight.toDouble(),
                sex = selectedGender,
                dailyWaterGoal = 2000, // Default value
                notificationFrequency = 60 // Default value
            )

            // Insert user data into the database
            val userId = dbHelper.insertUser(updatedUser)

            // Show confirmation popup and navigate to HomeActivity
            showAccountCreatedPopup(name)
        }
    }

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

            // After profile update, navigate to HomeActivity
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()  // Close this activity
    }
}
