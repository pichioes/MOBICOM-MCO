package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is just a placeholder. Replace with your main logic
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
