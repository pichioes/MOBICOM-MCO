package com.mobdeve.s17.mco2.group88

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SecurityQuestionBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private var userEmail: String = ""
    private var securityQuestion: String = ""

    companion object {
        private const val ARG_EMAIL = "email"
        private const val ARG_SECURITY_QUESTION = "security_question"
        private const val TAG = "SecurityDialog"

        fun newInstance(email: String, securityQuestion: String): SecurityQuestionBottomSheetDialog {
            val fragment = SecurityQuestionBottomSheetDialog()
            val args = Bundle()
            args.putString(ARG_EMAIL, email)
            args.putString(ARG_SECURITY_QUESTION, securityQuestion)
            fragment.arguments = args
            Log.d(TAG, "newInstance created with email: $email, question: $securityQuestion")
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        dbHelper = AquaBuddyDatabaseHelper(requireContext())

        arguments?.let {
            userEmail = it.getString(ARG_EMAIL, "")
            securityQuestion = it.getString(ARG_SECURITY_QUESTION, "")
        }

        Log.d(TAG, "Dialog created with email: '$userEmail'")
        Log.d(TAG, "Security question: '$securityQuestion'")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog called")

        val dialog = BottomSheetDialog(requireContext(), theme)
        val view: View = LayoutInflater.from(context).inflate(R.layout.popup_security, null)

        // Initialize views
        val securityQuestionText = view.findViewById<TextView>(R.id.securityQuestionText)
        val answerInput = view.findViewById<EditText>(R.id.securityAnswerInput)
        val submitButton = view.findViewById<Button>(R.id.btnSecuritySubmit)

        // Set the security question from database
        securityQuestionText.text = securityQuestion
        Log.d(TAG, "Security question set in UI")

        // Handle submit button click
        submitButton.setOnClickListener {
            val answer = answerInput.text.toString().trim()
            Log.d(TAG, "Submit button clicked with answer: '$answer'")

            if (answer.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verify the security answer against database
            val isCorrect = dbHelper.verifySecurityQuestionAnswer(userEmail, answer)
            Log.d(TAG, "Verification result: $isCorrect")

            if (isCorrect) {
                Log.d(TAG, "Answer correct! Starting navigation...")
                Toast.makeText(requireContext(), "Answer verified! Redirecting...", Toast.LENGTH_SHORT).show()

                // Try direct navigation without dismissing first
                navigateToResetPassword()

                // Dismiss after a short delay
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        dismiss()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error dismissing dialog", e)
                    }
                }, 500)

            } else {
                Log.d(TAG, "Answer incorrect")
                Toast.makeText(requireContext(), "Incorrect answer. Please try again.", Toast.LENGTH_SHORT).show()
                answerInput.text.clear()
            }
        }

        dialog.setContentView(view)
        Log.d(TAG, "Dialog setup complete")
        return dialog
    }

    private fun navigateToResetPassword() {
        Log.d(TAG, "navigateToResetPassword called")

        try {
            val activity = requireActivity()
            Log.d(TAG, "Got activity: ${activity.javaClass.simpleName}")

            val intent = Intent(activity, ResetPasswordActivity::class.java)
            intent.putExtra("user_email", userEmail)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Log.d(TAG, "Intent created with email: '$userEmail'")

            // Start the activity
            activity.startActivity(intent)
            Log.d(TAG, "startActivity called")

            // Finish the parent activity
            activity.finish()
            Log.d(TAG, "Parent activity finished")

        } catch (e: Exception) {
            Log.e(TAG, "Error in navigateToResetPassword", e)
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()

            // Fallback: try without finishing parent activity
            try {
                val intent = Intent(requireContext(), ResetPasswordActivity::class.java)
                intent.putExtra("user_email", userEmail)
                startActivity(intent)
                Log.d(TAG, "Fallback navigation attempted")
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback navigation also failed", e2)
            }
        }
    }
}