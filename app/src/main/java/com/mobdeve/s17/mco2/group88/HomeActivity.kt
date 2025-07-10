package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.Dialog
import android.widget.Toast
import android.widget.GridLayout
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private val waterRecords = mutableListOf<WaterRecord>() // List to track water intake records
    private var selectedCupSize = 250 // Default value (can be updated after confirmation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        userNameTextView.setOnClickListener {
            // Open ProfileMainPage
            val intent = Intent(this, ProfileMainPage::class.java)
            startActivity(intent)
        }

        // Set up ComposeView for WeekBar
        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            val userProgress = arrayOf(100, 80, 60, 40, 20, 0, 70)
            WeekBar(userProgress = userProgress)  // Render WeekBar composable
        }

        // Set up ComposeView for CircularProgressWithCap (This is where HomeProgress is added)
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap(onWaterIntake = { record ->
                // Handle water intake here if needed
                waterRecords.add(0, record)
                updateRecyclerView()
            })
        }

        // Set up RecyclerView for displaying water intake records
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = WaterRecordAdapter(waterRecords)
        recyclerView.adapter = adapter

        // Set up ImageButton for the "Switch Cup" button (to trigger the popup)
        val switchCupButton = findViewById<ImageButton>(R.id.switchcup)
        switchCupButton.setOnClickListener {
            // Call the function to show the popup
            showPopup()
        }
    }

    private fun showPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_switchcup) // Inflate the popup layout
        dialog.setCancelable(true)

        // Close button functionality
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog on close
        }

        // Handle cup size button clicks
        val gridLayout = dialog.findViewById<GridLayout>(R.id.gridLayout)
        gridLayout.setOnClickListener { view ->
            val selectedCup = when (view.id) {
                R.id.btn_100ml -> 100
                R.id.btn_125ml -> 125
                R.id.btn_150ml -> 150
                R.id.btn_175ml -> 175
                R.id.btn_200ml -> 200
                R.id.btn_300ml -> 300
                R.id.btn_400ml -> 400
                else -> 0 // Custom
            }

            selectedCupSize = selectedCup // Save the selected cup size
            Toast.makeText(this, "Selected: $selectedCupSize ml", Toast.LENGTH_SHORT).show()
        }

        // Confirm button functionality
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            // Confirm the selected cup size (No need to add to the water records list)
            Toast.makeText(this, "Cup Size Confirmed: $selectedCupSize ml", Toast.LENGTH_SHORT).show()

            // Close the popup
            dialog.dismiss()
        }

        dialog.show() // Show the dialog
    }

    private fun updateRecyclerView() {
        // Notify the adapter that the data has changed
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        val adapter = recyclerView.adapter as WaterRecordAdapter
        adapter.notifyDataSetChanged()
    }

    // Function to get current time for the water intake record
    private fun getCurrentTime(): String {
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        return currentTime
    }
}
