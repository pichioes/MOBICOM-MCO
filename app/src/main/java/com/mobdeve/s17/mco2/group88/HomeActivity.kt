package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import android.app.Dialog
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private val waterRecords = mutableListOf<WaterRecord>() // List to track water intake records
    private var selectedCupSize = 250 // Default value (can be updated after confirmation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Set up Bottom Navigation
        setupBottomNavigation()

        // Set up ComposeView for WeekBar
        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            val userProgress = arrayOf(100, 80, 60, 40, 20, 0, 70)
            WeekBar(userProgress = userProgress)  // Render WeekBar composable
        }

        // Set up ComposeView for CircularProgressWithCap
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap(
                goalAmount = 2150, // Set goal here
                onWaterIntake = { record ->
                    waterRecords.add(0, record) // Add water record to the list
                    updateRecyclerView()
                }
            )
        }

        // Set up RecyclerView for displaying water intake records
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = WaterRecordAdapter(waterRecords)
        recyclerView.adapter = adapter

        // Set up ImageButton for the "Switch Cup" button (to trigger the popup)
        val switchCupButton = findViewById<ImageButton>(R.id.switchcup)
        switchCupButton.setOnClickListener {
            showPopup()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Create color state lists programmatically
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // Selected state
            intArrayOf(-android.R.attr.state_checked) // Unselected state
        )

        // Define colors for the states (selected vs unselected)
        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.nav_selected_icon_color),  // Selected color
            ContextCompat.getColor(this, R.color.nav_unselected_icon_color)  // Unselected color
        )

        val colorStateList = ColorStateList(states, colors)

        // Set the color state list for both item icon and item text
        bottomNav.itemIconTintList = colorStateList
        bottomNav.itemTextColor = colorStateList

        // Make sure the selected item is properly set
        bottomNav.selectedItemId = R.id.nav_home  // Ensure the default is 'home'

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Refresh or stay on HomeActivity
                    true
                }
                R.id.nav_analytics -> {
                    val intent = Intent(this, AnalyticsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileMainPage::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }


    private fun showPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_switchcup)
        dialog.setCancelable(true)

        val closeButton = dialog.findViewById<ImageButton>(R.id.switchCloseButton)
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog on close
        }

        setCupSizeButtonClickListener(dialog, R.id.btn_100ml, 100)
        setCupSizeButtonClickListener(dialog, R.id.btn_125ml, 125)
        setCupSizeButtonClickListener(dialog, R.id.btn_150ml, 150)
        setCupSizeButtonClickListener(dialog, R.id.btn_175ml, 175)
        setCupSizeButtonClickListener(dialog, R.id.btn_200ml, 200)
        setCupSizeButtonClickListener(dialog, R.id.btn_300ml, 300)
        setCupSizeButtonClickListener(dialog, R.id.btn_400ml, 400)

        val customizeButton = dialog.findViewById<ImageButton>(R.id.btn_customize)
        customizeButton.setOnClickListener {
            showCustomizePopup() // Show the popup to input a custom size
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            Toast.makeText(this, "Cup Size Confirmed: $selectedCupSize ml", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCustomizePopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_customizecup)
        dialog.setCancelable(true)

        val closeButton = dialog.findViewById<ImageButton>(R.id.customizeCloseButton)
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog on close
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        val cupSizeInput = dialog.findViewById<EditText>(R.id.cupSizeInput)

        confirmButton.setOnClickListener {
            val customCupSize = cupSizeInput.text.toString().toIntOrNull()

            if (customCupSize != null && customCupSize > 0) {
                selectedCupSize = customCupSize
                Toast.makeText(this, "Custom Cup Size Confirmed: $selectedCupSize ml", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid cup size", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss() // Close the custom popup
        }

        dialog.show()
    }

    private fun setCupSizeButtonClickListener(dialog: Dialog, buttonId: Int, cupSize: Int) {
        val button = dialog.findViewById<ImageButton>(buttonId)
        button.setOnClickListener {
            selectedCupSize = cupSize // Save the selected cup size
            Toast.makeText(this, "Selected: $selectedCupSize ml", Toast.LENGTH_SHORT).show()
            highlightSelectedButton(dialog, button)
        }
    }

    private fun highlightSelectedButton(dialog: Dialog, selectedButton: ImageButton) {
        val gridLayout = dialog.findViewById<GridLayout>(R.id.gridLayout)
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as ImageButton
            button.setBackgroundResource(0)  // Reset background
        }
        selectedButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
    }

    private fun updateRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        val adapter = recyclerView.adapter as WaterRecordAdapter
        adapter.notifyDataSetChanged()
    }

    private fun getCurrentTime(): String {
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        return currentTime
    }
}
