package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var dbHelper: AquaBuddyDatabaseHelper

    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_forgotpassword)

        Log.d(TAG, "ForgotPasswordActivity created")

        dbHelper = AquaBuddyDatabaseHelper(this)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val backButton = findViewById<ImageButton>(R.id.forgotBackButton)

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            Log.d(TAG, "Submit clicked with email: '$email'")

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if user exists first
            val user = dbHelper.getUserByEmail(email)
            Log.d(TAG, "User found: ${user != null}")

            if (user == null) {
                Toast.makeText(this, "No account found with this email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get security question from the user object
            val securityQuestion = user.securityQuestion
            Log.d(TAG, "Security question: '$securityQuestion'")

            if (securityQuestion.isNotEmpty()) {
                Log.d(TAG, "Showing security question dialog")
                // Show security question dialog with the question from database
                val securityDialog = SecurityQuestionBottomSheetDialog.newInstance(email, securityQuestion)
                securityDialog.show(supportFragmentManager, "SecurityQuestionBottomSheet")
            } else {
                Toast.makeText(this, "No security question found for this account. Please contact support.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}