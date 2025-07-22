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

        // Input fields for sign-up
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

        // Navigate to login page if the user already has an account
        loginHereLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Handle create account logic (for email/password)
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

            // Hash the password before inserting it
            val hashedPassword = hashPassword(password)

            // Store email and password hash temporarily in SharedPreferences
            val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("user_email", email)
            editor.putString("user_password_hash", hashedPassword)
            editor.apply()

            // Navigate to profile creation
            navigateToProfileCreation()
        }
    }

    private fun navigateToProfileCreation() {
        // Move to profile creation activity
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()  // Close SignupActivity
    }
}
