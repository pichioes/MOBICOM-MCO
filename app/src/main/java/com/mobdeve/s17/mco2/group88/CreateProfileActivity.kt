package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: AquaBuddyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_createprofile)

        dbHelper = AquaBuddyDatabaseHelper(this)

        // Initialize views
        val backButton = findViewById<ImageButton>(R.id.createprofileBackButton)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val genderGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)

        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)

        // Retrieve stored data from SharedPreferences
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val passwordHash = sharedPreferences.getString("user_password_hash", "") ?: ""
        val securityQuestion = sharedPreferences.getString("security_question", "") ?: ""
        val securityAnswer = sharedPreferences.getString("security_answer", "") ?: "" // Get plain text answer

        // Debug: Check retrieved data
        Log.d("CreateProfile", "Retrieved from SharedPreferences:")
        Log.d("CreateProfile", "email: $email")
        Log.d("CreateProfile", "passwordHash: ${if (passwordHash.isNotEmpty()) "present" else "missing"}")
        Log.d("CreateProfile", "securityQuestion: $securityQuestion")
        Log.d("CreateProfile", "securityAnswer: ${if (securityAnswer.isNotEmpty()) "present" else "missing"}")

        // Back navigation
        backButton.setOnClickListener {
            finish()  // Close this activity and go back to SecurityQuestionActivity
        }

        // Continue Button logic for saving profile data
        continueButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val ageText = ageInput.text.toString().trim()
            val heightText = heightInput.text.toString().trim()
            val weightText = weightInput.text.toString().trim()
            val selectedGenderId = genderGroup.checkedRadioButtonId

            // Check if gender is selected
            if (selectedGenderId == -1) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGender = findViewById<RadioButton>(selectedGenderId)?.text.toString()

            // Validation for missing fields
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ageText.isEmpty() || heightText.isEmpty() || weightText.isEmpty()) {
                Toast.makeText(this, "Please fill in all profile fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate numeric inputs
            val age: Int
            val height: Double
            val weight: Double

            try {
                age = ageText.toInt()
                height = heightText.toDouble()
                weight = weightText.toDouble()

                if (age <= 0 || age > 150) {
                    Toast.makeText(this, "Please enter a valid age (1-150)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (height <= 0 || height > 300) {
                    Toast.makeText(this, "Please enter a valid height in cm", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (weight <= 0 || weight > 500) {
                    Toast.makeText(this, "Please enter a valid weight in kg", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers for age, height, and weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate that we have required data from previous steps
            if (email.isEmpty() || passwordHash.isEmpty()) {
                Toast.makeText(this, "Missing account data. Please start over.", Toast.LENGTH_SHORT).show()
                navigateToSignup()
                return@setOnClickListener
            }

            // Calculate recommended water intake based on weight and gender (returns ml)
            val recommendedWaterIntake = calculateRecommendedWaterIntake(weight, selectedGender.lowercase())

            Log.d("CreateProfile", "About to insert user:")
            Log.d("CreateProfile", "name: $name")
            Log.d("CreateProfile", "email: $email")
            Log.d("CreateProfile", "age: $age")
            Log.d("CreateProfile", "height: $height")
            Log.d("CreateProfile", "weight: $weight")
            Log.d("CreateProfile", "gender: $selectedGender")
            Log.d("CreateProfile", "recommendedWaterIntake: ${recommendedWaterIntake}ml")

            // Store the security answer as plain text (no hashing)
            val securityAnswerPlain = securityAnswer

            // Create user object with all collected data
            val newUser = User(
                id = 0,  // Auto-generated ID
                name = name,
                email = email,
                passwordHash = passwordHash,
                age = age,
                height = height,
                weight = weight,
                sex = selectedGender.lowercase(), // Normalize to lowercase
                dailyWaterGoal = recommendedWaterIntake, // This is in ml as expected by the database
                notificationFrequency = 60, // Default value in minutes
                securityQuestion = securityQuestion,
                securityAnswerHash = securityAnswerPlain // Store plain text instead of hash
            )

            // Debug: Log final user data
            Log.d("CreateProfile", "Final user data: $newUser")

            try {
                // Check if user already exists
                val existingUser = dbHelper.getUserByEmail(email)
                if (existingUser != null) {
                    Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Insert user data into the database
                val userId = dbHelper.insertUser(newUser)
                Log.d("CreateProfile", "insertUser returned: $userId")

                if (userId > 0) {
                    // Save the user session
                    saveUserSession(userId, name)

                    // Clear temporary signup data from SharedPreferences
                    clearTemporarySignupData()

                    // Show confirmation popup and navigate to Login
                    showAccountCreatedPopup(name)
                } else {
                    Toast.makeText(this, "Error creating account. Please try again.", Toast.LENGTH_SHORT).show()
                    Log.e("CreateProfile", "Failed to create user - insertUser returned: $userId")
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("CreateProfile", "Exception during user creation", e)
                e.printStackTrace()
            }
        }
    }

    // Calculate recommended water intake based on weight and gender
    private fun calculateRecommendedWaterIntake(weight: Double, sex: String): Int {
        // Basic calculation: 35ml per kg for men, 31ml per kg for women
        val multiplier = if (sex.lowercase() == "male") 35.0 else 31.0
        return (weight * multiplier).toInt()
    }

    // Password hashing utility
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun saveUserSession(userId: Long, userName: String) {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save the user ID for future sessions
        editor.putLong("user_id", userId)
        editor.putString("user_name", userName)
        editor.putBoolean("is_logged_in", true)
        editor.apply()

        Log.d("CreateProfile", "Saved user session:")
        Log.d("CreateProfile", "user_id: $userId")
        Log.d("CreateProfile", "user_name: $userName")
    }

    private fun clearTemporarySignupData() {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Remove temporary signup data
        editor.remove("user_email")
        editor.remove("user_password_hash")
        editor.remove("security_question")
        editor.remove("security_answer")
        editor.apply()

        Log.d("CreateProfile", "Cleared temporary signup data from SharedPreferences")
    }

    private fun showAccountCreatedPopup(name: String) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.popup_accountcreated, null)
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialog.show()

            // Popup size and transparency
            dialog.window?.apply {
                setLayout(
                    (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
                setGravity(Gravity.CENTER)
            }

            val okButton = dialogView.findViewById<Button>(R.id.btnGetStarted)
            okButton?.setOnClickListener {
                dialog.dismiss()

                // Show a welcome message
                Toast.makeText(this, "Account created successfully! Please login, $name!", Toast.LENGTH_SHORT).show()

                // Navigate to login screen
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e("CreateProfile", "Error showing popup", e)
            // Fallback: just show toast and navigate
            Toast.makeText(this, "Account created successfully! Please login, $name!", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}