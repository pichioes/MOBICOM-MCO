package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
// for tapping the welcome greeting to go to the profile page temporarily
import android.content.Intent
import android.widget.TextView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val composeView = findViewById<ComposeView>(R.id.composeProgress)

        composeView.setContent {
            CircularProgressWithCap()
        }

        val nameText = findViewById<TextView>(R.id.greetingText)
        nameText.setOnClickListener {
            val intent = Intent(this, ProfileMainPage::class.java)
            startActivity(intent)
        }

    }
}
