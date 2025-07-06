package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Set up ComposeView for WeekBar
        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            // Example user progress data for the week
            val userProgress = arrayOf(100, 80, 60, 40, 20, 0, 70)
            WeekBar(userProgress = userProgress)  // Render WeekBar composable
        }

        // Set up ComposeView for CircularProgressWithCap
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap()  // Render CircularProgressWithCap composable
        }

        // Set up greeting text click listener to navigate to the Profile Main Page
        val greetingText = findViewById<TextView>(R.id.greetingText)
        greetingText.setOnClickListener {
            val intent = Intent(this, ProfileMainPage::class.java)
            startActivity(intent)  // Navigates to the ProfileMainPage activity
        }
    }
}
