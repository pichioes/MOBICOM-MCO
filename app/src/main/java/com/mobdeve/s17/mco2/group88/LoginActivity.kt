package com.mobdeve.s17.mco2.group88

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extend layout into status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        setContentView(R.layout.login_signin)

        val loginButton: Button = findViewById(R.id.loginButton)
        val forgotPasswordText: TextView = findViewById(R.id.textForgotPassword)
        val signUpText: TextView = findViewById(R.id.textSignUpHere)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val passwordToggle: ImageButton = findViewById(R.id.passwordToggle)

        loginButton.setOnClickListener {
            showAccountCreatedDialog()
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        passwordToggle.setOnClickListener {
            val position = passwordInput.selectionStart
            if (isPasswordVisible) {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.password_eyeclosed)
            } else {
                passwordInput.transformationMethod = null
                passwordToggle.setImageResource(R.drawable.password_eyeopen)
            }
            passwordInput.setSelection(position)
            isPasswordVisible = !isPasswordVisible
        }
    }

    private fun showAccountCreatedDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_accountcreated, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        val getStartedButton = dialogView.findViewById<Button>(R.id.btnGetStarted)
        getStartedButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}
