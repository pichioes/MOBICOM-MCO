package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_signup)

        // Input fields
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)

        // Eye toggle buttons
        val passwordToggle = findViewById<ImageButton>(R.id.passwordToggle)
        val confirmPasswordToggle = findViewById<ImageButton>(R.id.confirmPasswordToggle)

        // Navigation buttons
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginHereLink = findViewById<TextView>(R.id.loginHereLink)
        val signupBackButton = findViewById<ImageButton>(R.id.signupBackButton)

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

        // Toggle confirm password visibility
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

        // Go back
        signupBackButton.setOnClickListener {
            finish()
        }

        // ✅ Directly go to CreateProfileActivity
        createAccountButton.setOnClickListener {
            val intent = Intent(this@SignupActivity, CreateProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
