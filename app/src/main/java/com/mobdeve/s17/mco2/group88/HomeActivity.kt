// HomeActivity.kt

package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private val waterRecords = mutableListOf<WaterRecord>() // List to track water intake records

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        userNameTextView.setOnClickListener {
            val intent = Intent(this, ProfileMainPage::class.java)
            startActivity(intent)
        }

        // Set up ComposeView for WeekBar
        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            val userProgress = arrayOf(100, 80, 60, 40, 20, 0, 70)
            WeekBar(userProgress = userProgress)  // Render WeekBar composable
        }

        // Set up ComposeView for CircularProgressWithCap
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap(onWaterIntake = { record ->
                // Add the water record to the list
                waterRecords.add(0, record) // Add to the start of the list
                updateRecyclerView()
            })
        }

        // Set up RecyclerView for displaying water intake records
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = WaterRecordAdapter(waterRecords)
        recyclerView.adapter = adapter
    }

    private fun updateRecyclerView() {
        // Notify the adapter that the data has changed
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        val adapter = recyclerView.adapter as WaterRecordAdapter
        adapter.notifyDataSetChanged()
    }
}
