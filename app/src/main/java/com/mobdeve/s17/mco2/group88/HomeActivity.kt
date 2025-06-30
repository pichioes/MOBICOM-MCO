package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val composeView = findViewById<ComposeView>(R.id.composeProgress)

        // Set Compose content for the ComposeView
        composeView.setContent {
            // Example: 1000 out of 2150 ml => ~46% progress
            CircularProgressWithCap(
                progress = 1000f / 2150f,
                currentTextNumber = "1000",
                goalText = "Goal 2150 ml"
            )
        }
    }
}
