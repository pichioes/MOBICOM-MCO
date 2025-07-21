package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var dbHelper: AquaBuddyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_signup)

        dbHelper = AquaBuddyDatabaseHelper(this)

        // Input fields for signup
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginHereLink = findViewById<TextView>(R.id.loginHereLink)

        // Eye toggle buttons
        val passwordToggle = findViewById<ImageButton>(R.id.passwordToggle)
        val confirmPasswordToggle = findViewById<ImageButton>(R.id.confirmPasswordToggle)

        // Toggle password visibility
        passwordToggle.setOnClickListener {
            val pos = passwordInput.selectionStart
            isPasswordVisible = !isPasswordVisible
            passwordInput.transformationMethod =
                if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
            passwordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.password_eyeopen else R.drawable.password_eyeclosed
            )
            passwordInput.setSelection(pos)
        }

        confirmPasswordToggle.setOnClickListener {
            val pos = confirmPasswordInput.selectionStart
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            confirmPasswordInput.transformationMethod =
                if (isConfirmPasswordVisible) null else PasswordTransformationMethod.getInstance()
            confirmPasswordToggle.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.password_eyeopen else R.drawable.password_eyeclosed
            )
            confirmPasswordInput.setSelection(pos)
        }

        // Navigate to login page
        loginHereLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Handle create account logic
        createAccountButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validate input
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hash the password
            val hashedPassword = hashPassword(password)

            // Create user object (with empty profile initially)
            val newUser = User(
                name = "", // Empty name initially, to be filled in Profile Creation
                email = email,
                passwordHash = hashedPassword,
                age = 0,
                weight = 0.0,
                height = 0.0,
                sex = "",
                dailyWaterGoal = 2000, // Default or from input
                notificationFrequency = 60 // Default or from input
            )

            // Insert the user into the database
            val userId = dbHelper.insertUser(newUser)

            if (userId > 0) {
                // Successfully created, navigate to profile creation
                navigateToProfileCreation(userId)
            } else {
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()
            }
        }

        // Back navigation logic
        val backButton = findViewById<ImageButton>(R.id.signupBackButton)
        backButton.setOnClickListener {
            finish()  // This will close the SignupActivity and navigate back to the previous activity
        }
    }

    private fun navigateToProfileCreation(userId: Long) {
        // Store the user's ID temporarily for profile creation
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("user_id", userId)
        editor.apply()

        // Navigate to profile creation activity
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()  // Close SignupActivity so user cannot go back
    }
}
