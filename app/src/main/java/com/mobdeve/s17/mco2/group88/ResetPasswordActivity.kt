package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResetPasswordActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ResetPasswordActivity"
    }

    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private var userEmail: String = ""
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== ResetPasswordActivity onCreate START ===")

        // Initialize database helper
        dbHelper = AquaBuddyDatabaseHelper(this)

        // Get the email from intent
        userEmail = intent.getStringExtra("user_email") ?: ""
        Log.d(TAG, "Received email: '$userEmail'")

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No email provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            setContentView(R.layout.login_newpassword)
            Log.d(TAG, "Successfully set layout: login_newpassword")

            setupViews()

            Toast.makeText(this, "Set new password for: $userEmail", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error setting layout", e)
            Toast.makeText(this, "Layout error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }

        Log.d(TAG, "=== ResetPasswordActivity onCreate END ===")
    }

    private fun setupViews() {
        // Get references to views
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val newPasswordToggle = findViewById<ImageButton>(R.id.newPasswordToggle)
        val confirmPasswordToggle = findViewById<ImageButton>(R.id.confirmPasswordToggle)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val backButton = findViewById<ImageButton>(R.id.newpasswordBackButton)

        // Setup password visibility toggles
        newPasswordToggle.setOnClickListener {
            togglePasswordVisibility(passwordInput, newPasswordToggle, isPasswordVisible)
            isPasswordVisible = !isPasswordVisible
        }

        confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput, confirmPasswordToggle, isConfirmPasswordVisible)
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }

        // Setup back button
        backButton.setOnClickListener {
            finish()
        }

        // Setup submit button
        submitButton.setOnClickListener {
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validatePasswords(password, confirmPassword)) {
                updatePassword(password)
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageButton, isVisible: Boolean) {
        if (isVisible) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.password_eyeopen)
        } else {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.password_eyeclosed) // You might need to create this drawable
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        when {
            password.isEmpty() -> {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return false
            }
            confirmPassword.isEmpty() -> {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
                return false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun updatePassword(newPassword: String) {
        try {
            val success = dbHelper.updateUserPassword(userEmail, newPassword)

            if (success) {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Password updated for user: $userEmail")

                // Optionally, navigate back to login or main activity
                finish()
            } else {
                Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update password for user: $userEmail")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password", e)
            Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "ResetPasswordActivity onResume")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "ResetPasswordActivity onStart")
    }
}