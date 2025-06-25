package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_forgotpassword)

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val otpDialog = OtpBottomSheetDialog()
            otpDialog.show(supportFragmentManager, "OtpBottomSheet")
        }

        val backButton = findViewById<ImageButton>(R.id.forgotBackButton)
        backButton.setOnClickListener {
            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
