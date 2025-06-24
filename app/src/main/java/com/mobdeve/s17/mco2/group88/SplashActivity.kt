package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        val splashGif = findViewById<ImageView>(R.id.splashGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.splashscreen) // make sure this is a valid GIF
            .into(splashGif)

        // Show splash screen for 3 seconds (3000ms)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, EditProfile::class.java))
            finish()
        }, 3000L)
    }
}
