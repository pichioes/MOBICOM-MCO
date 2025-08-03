package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.view.View
import android.widget.Toast
import android.util.Log
import android.content.SharedPreferences
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class EditProfile : AppCompatActivity() {

    // Variables for gender selection
    private var selectedGender: String = "Female"  // Default gender is Female

    // Database helper
    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_editpage)

        // Initialize database helper
        dbHelper = AquaBuddyDatabaseHelper(this)
        sharedPreferences = getSharedPreferences("AquaBuddyPrefs", MODE_PRIVATE)

        // Get current user ID from SharedPreferences (matching ProfileMainPage key)
        currentUserId = sharedPreferences.getLong("user_id", -1)

        if (currentUserId == -1L) {
            // Handle case where user ID is not found
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // EditText fields for user data
        val firstNameField: EditText = findViewById(R.id.edit_firstname)
        val lastNameField: EditText = findViewById(R.id.edit_lastname)
        val emailField: EditText = findViewById(R.id.editTextTextEmailAddress)

        // References for age spinner and height/weight input fields
        val ageSpinner: Spinner = findViewById(R.id.spinner2)
        val heightInput: EditText = findViewById(R.id.height_input)
        val weightInput: EditText = findViewById(R.id.weight_input)

        // Data list for age spinner only
        val ageOptions = (10..100).map { it.toString() }

        // Set up age spinner
        setupSpinner(ageSpinner, ageOptions)

        // Load current user data and populate fields - this loads existing data for editing
        loadCurrentUserData(firstNameField, lastNameField, emailField, ageSpinner, heightInput, weightInput)

        // Gender Button Setup - Updated to include Others button
        val femaleButton: Button = findViewById(R.id.femaleButton)
        val maleButton: Button = findViewById(R.id.maleButton)
        val othersButton: Button = findViewById(R.id.othersButton)

        // OnClickListener for Female Button
        femaleButton.setOnClickListener {
            selectedGender = "Female"
            updateGenderButtonColors(femaleButton, maleButton, othersButton)
        }

        // OnClickListener for Male Button
        maleButton.setOnClickListener {
            selectedGender = "Male"
            updateGenderButtonColors(maleButton, femaleButton, othersButton)
        }

        // OnClickListener for Others Button
        othersButton.setOnClickListener {
            selectedGender = "Others"
            updateGenderButtonColors(othersButton, femaleButton, maleButton)
        }

        // Save Button: When clicked, it saves the data to database
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            saveUserProfile(firstNameField, lastNameField, emailField, ageSpinner, heightInput, weightInput)
        }
    }

    private fun loadCurrentUserData(
        firstNameField: EditText,
        lastNameField: EditText,
        emailField: EditText,
        ageSpinner: Spinner,
        heightInput: EditText,
        weightInput: EditText
    ) {
        try {
            val currentUser = dbHelper.getUserById(currentUserId)

            if (currentUser != null) {
                // Update the header display with current user info
                updateHeaderDisplay(currentUser)

                // Parse full name into first and last name
                val nameParts = currentUser.name.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = nameParts.getOrNull(1) ?: ""

                // Populate text fields
                firstNameField.setText(firstName)
                lastNameField.setText(lastName)
                emailField.setText(currentUser.email)

                // Populate height and weight input fields
                if (currentUser.height > 0) {
                    heightInput.setText(currentUser.height.toInt().toString())
                }

                if (currentUser.weight > 0) {
                    weightInput.setText(currentUser.weight.toInt().toString())
                }

                // Set gender selection - Updated to handle Others
                selectedGender = when (currentUser.sex.lowercase()) {
                    "male", "m" -> "Male"
                    "female", "f" -> "Female"
                    "others", "other", "o" -> "Others"
                    else -> "Female" // Default fallback
                }

                // Update gender button colors - Updated to include Others button
                val femaleButton: Button = findViewById(R.id.femaleButton)
                val maleButton: Button = findViewById(R.id.maleButton)
                val othersButton: Button = findViewById(R.id.othersButton)

                when (selectedGender) {
                    "Female" -> updateGenderButtonColors(femaleButton, maleButton, othersButton)
                    "Male" -> updateGenderButtonColors(maleButton, femaleButton, othersButton)
                    "Others" -> updateGenderButtonColors(othersButton, femaleButton, maleButton)
                }

                // Set age spinner selection
                if (currentUser.age > 0) {
                    val agePosition = (currentUser.age - 10).coerceIn(0, 90)
                    ageSpinner.setSelection(agePosition)
                }

            } else {
                Toast.makeText(this, "Could not load user data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("EditProfile", "Error loading user data: ${e.message}", e)
            Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHeaderDisplay(user: User) {
        try {
            // Update the header name and email TextViews
            val headerNameTextView: TextView = findViewById(R.id.textView13)
            val headerEmailTextView: TextView = findViewById(R.id.textView14)

            headerNameTextView.text = user.name
            headerEmailTextView.text = user.email

        } catch (e: Exception) {
            Log.e("EditProfile", "Error updating header display: ${e.message}", e)
        }
    }

    private fun saveUserProfile(
        firstNameField: EditText,
        lastNameField: EditText,
        emailField: EditText,
        ageSpinner: Spinner,
        heightInput: EditText,
        weightInput: EditText
    ) {
        try {
            // Get current user data
            val currentUser = dbHelper.getUserById(currentUserId)
            if (currentUser == null) {
                Toast.makeText(this, "Error: Could not find user", Toast.LENGTH_SHORT).show()
                return
            }

            // Capture input data
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val fullName = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName

            // Validate required fields
            if (firstName.isEmpty()) {
                Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show()
                firstNameField.requestFocus()
                return
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
                emailField.requestFocus()
                return
            }

            // Check if email is already taken by another user
            val existingUser = dbHelper.getUserByEmail(email)
            if (existingUser != null && existingUser.id != currentUserId) {
                Toast.makeText(this, "Email is already in use by another account", Toast.LENGTH_SHORT).show()
                emailField.requestFocus()
                return
            }

            // Parse input values
            val age = ageSpinner.selectedItem.toString().toIntOrNull() ?: currentUser.age

            // Parse height from input field
            val heightText = heightInput.text.toString().trim()
            val height = if (heightText.isNotEmpty()) {
                heightText.toDoubleOrNull() ?: currentUser.height
            } else {
                currentUser.height
            }

            // Parse weight from input field
            val weightText = weightInput.text.toString().trim()
            val weight = if (weightText.isNotEmpty()) {
                weightText.toDoubleOrNull() ?: currentUser.weight
            } else {
                currentUser.weight
            }

            // Validate height and weight ranges
            if (height > 0 && (height < 100 || height > 250)) {
                Toast.makeText(this, "Please enter a valid height (100-250 cm)", Toast.LENGTH_SHORT).show()
                heightInput.requestFocus()
                return
            }

            if (weight > 0 && (weight < 20 || weight > 300)) {
                Toast.makeText(this, "Please enter a valid weight (20-300 kg)", Toast.LENGTH_SHORT).show()
                weightInput.requestFocus()
                return
            }

            // Calculate new daily water goal based on updated weight and gender
            val newDailyWaterGoal = if (weight > 0) {
                // Use enhanced calculation that handles Others gender
                calculateWaterIntake(weight.toFloat(), height.toFloat(), selectedGender)
            } else {
                currentUser.dailyWaterGoal
            }

            // Create updated user object - Store gender in lowercase for consistency
            val updatedUser = currentUser.copy(
                name = fullName,
                email = email,
                age = age,
                weight = weight,
                height = height,
                sex = selectedGender.lowercase(),
                dailyWaterGoal = newDailyWaterGoal,
                updatedAt = getCurrentDateTime()
            )

            // Save to database
            val rowsUpdated = dbHelper.updateUser(updatedUser)

            if (rowsUpdated > 0) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Update SharedPreferences if needed (matching ProfileMainPage keys)
                with(sharedPreferences.edit()) {
                    putString("user_name", fullName)
                    putString("user_email", email)
                    apply()
                }

                // Navigate back to ProfileMainPage
                val intent = Intent(this, ProfileMainPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("EditProfile", "Error saving user profile: ${e.message}", e)
            Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Enhanced water intake calculation that handles Others gender
    private fun calculateWaterIntake(weight: Float, height: Float, sex: String): Int {
        // Base calculation using weight (35ml per kg for men, 31ml per kg for women, 33ml per kg for others)
        val baseIntake = when (sex.lowercase()) {
            "male", "m" -> weight * 35 // ml per kg for men
            "female", "f" -> weight * 31 // ml per kg for women
            "others", "other", "o" -> weight * 33 // ml per kg for others/non-binary
            else -> weight * 33 // Average for unspecified
        }

        // Height adjustment factor (BMI consideration)
        val heightInMeters = height / 100
        val bmi = weight / (heightInMeters * heightInMeters)

        val heightFactor = when {
            bmi > 25 -> 1.15f // Higher BMI needs more water
            bmi < 18.5 -> 0.95f // Lower BMI needs slightly less
            height > 185 -> 1.1f // Very tall people need more
            height < 155 -> 0.95f // Shorter people need slightly less
            else -> 1.0f
        }

        val adjustedIntake = baseIntake * heightFactor

        // Ensure reasonable bounds (1500ml - 4000ml)
        val finalIntake = adjustedIntake.coerceIn(1500f, 4000f)

        // Round to nearest 50ml for cleaner numbers
        return (finalIntake / 50).kotlin.math.roundToInt() * 50
    }

    // Updated to handle 3 buttons instead of 2
    private fun updateGenderButtonColors(selectedButton: Button, unselectedButton1: Button, unselectedButton2: Button) {
        selectedButton.setBackgroundColor(resources.getColor(R.color.colorSelected))
        unselectedButton1.setBackgroundColor(resources.getColor(R.color.colorUnselected))
        unselectedButton2.setBackgroundColor(resources.getColor(R.color.colorUnselected))
    }

    // Function to set up spinners with data
    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.profile_spinner, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Function to handle Female button click (keeping for XML onClick compatibility)
    fun onFemaleClick(view: View) {
        selectedGender = "Female"
        val femaleButton: Button = findViewById(R.id.femaleButton)
        val maleButton: Button = findViewById(R.id.maleButton)
        val othersButton: Button = findViewById(R.id.othersButton)
        updateGenderButtonColors(femaleButton, maleButton, othersButton)
    }

    // Function to handle Male button click (keeping for XML onClick compatibility)
    fun onMaleClick(view: View) {
        selectedGender = "Male"
        val femaleButton: Button = findViewById(R.id.femaleButton)
        val maleButton: Button = findViewById(R.id.maleButton)
        val othersButton: Button = findViewById(R.id.othersButton)
        updateGenderButtonColors(maleButton, femaleButton, othersButton)
    }

    // NEW: Function to handle Others button click (for XML onClick compatibility)
    fun onOthersClick(view: View) {
        selectedGender = "Others"
        val femaleButton: Button = findViewById(R.id.femaleButton)
        val maleButton: Button = findViewById(R.id.maleButton)
        val othersButton: Button = findViewById(R.id.othersButton)
        updateGenderButtonColors(othersButton, femaleButton, maleButton)
    }

    // Helper function to get current date and time
    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}