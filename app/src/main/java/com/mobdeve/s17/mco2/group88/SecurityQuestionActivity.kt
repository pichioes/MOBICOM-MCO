package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SecurityQuestionActivity : AppCompatActivity() {

    private lateinit var dbHelper: AquaBuddyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_securityquestion) // Updated to use your actual layout

        dbHelper = AquaBuddyDatabaseHelper(this)

        // Initialize views
        val backButton = findViewById<ImageButton>(R.id.securityBackButton)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val securityQuestionSpinner = findViewById<Spinner>(R.id.securityQuestion)
        val securityAnswerInput = findViewById<EditText>(R.id.securityAnswerInput)

        // Set up security questions dropdown
        setupSecurityQuestions(securityQuestionSpinner)

        // Back button navigation
        backButton.setOnClickListener {
            finish() // Go back to SignupActivity
        }

        // Continue button logic
        continueButton.setOnClickListener {
            val selectedQuestion = securityQuestionSpinner.selectedItem?.toString()
            val answer = securityAnswerInput.text.toString().trim()

            // Validation
            if (selectedQuestion.isNullOrEmpty() || selectedQuestion == "Select a security question") {
                Toast.makeText(this, "Please select a security question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (answer.isEmpty()) {
                Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (answer.length < 2) {
                Toast.makeText(this, "Answer must be at least 2 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Store security question and plain text answer in SharedPreferences
            // The answer will be hashed later in CreateProfileActivity
            val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("security_question", selectedQuestion)
            editor.putString("security_answer", answer) // Store plain text, not hash
            editor.apply()

            // Debug: Verify what was saved
            println("DEBUG: Saved security question: $selectedQuestion")
            println("DEBUG: Saved security answer: ${if (answer.isNotEmpty()) "present" else "missing"}")

            // Navigate to CreateProfileActivity
            navigateToCreateProfile()
        }
    }

    private fun setupSecurityQuestions(spinner: Spinner) {
        val securityQuestions = arrayOf(
            "Select a security question",
            "What was the name of your first pet?",
            "What is your mother's maiden name?",
            "What was the name of your elementary school?",
            "What is the name of the street you grew up on?",
            "What was your childhood nickname?",
            "What is your favorite movie?",
            "What is the name of your favorite teacher?",
            "What was the model of your first car?",
            "What is your favorite food?",
            "In what city were you born?"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, securityQuestions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun navigateToCreateProfile() {
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish() // Close SecurityQuestionActivity
    }
}