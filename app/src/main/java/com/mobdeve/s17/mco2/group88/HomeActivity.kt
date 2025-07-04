package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val composeView = findViewById<ComposeView>(R.id.composeProgress)

        composeView.setContent {
            CircularProgressWithCap()
        }
    }
}
