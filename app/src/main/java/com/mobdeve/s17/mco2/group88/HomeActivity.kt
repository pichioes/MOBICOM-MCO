package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.mutableStateOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private val waterRecords = mutableListOf<WaterRecord>()
    private var selectedCupSize = mutableStateOf(250)  // Using mutableStateOf to track cup size state
    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var userNameTextView: TextView
    private lateinit var streakTextView: TextView
    private lateinit var goalPercentageTextView: TextView
    private var userId: Long = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        dbHelper = AquaBuddyDatabaseHelper(this)

        // Get the user ID from SharedPreferences (or wherever it's stored)
        userId = getCurrentUserId()

        // Fetch user data (e.g., username) from the database
        val user = dbHelper.getUserById(userId)
        userNameTextView = findViewById(R.id.userName)
        streakTextView = findViewById(R.id.Streak)  // Streak TextView
        goalPercentageTextView = findViewById(R.id.GoalPercentage)  // Goal Percentage TextView

        // Set the username in the TextView
        userNameTextView.text = user?.name ?: "User"

        setupBottomNavigation()

        // Fetch water intake records from the database
        val waterIntakes = dbHelper.getWaterIntakeHistory(userId, getCurrentDate())

        // Map the WaterIntake records to WaterRecord
        waterRecords.clear()
        for (intake in waterIntakes) {
            val record = WaterRecord(
                time = intake.time,
                amount = intake.amount.toString()  // Convert amount (Int) to String
            )
            waterRecords.add(record)
        }

        // RecyclerView setup
        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = WaterRecordAdapter(waterRecords)
        recyclerView.adapter = adapter


        val switchCupButton = findViewById<ImageButton>(R.id.switchcup)
        switchCupButton.setOnClickListener {
            showPopup()
        }

        // Set up notification countdown
        val nextSipTextView = findViewById<TextView>(R.id.NextSip)
        startNextSipCountdown(nextSipTextView)

        // Compose view for the WeekBar (user progress)
        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            val userProgress = arrayOf(100, 100, 100, 50, 0, 0, 0)
            WeekBar(userProgress = userProgress)
        }

        // Compose view for the CircularProgressWithCap (water intake progress)
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap(
                goalAmount = 2150,
                selectedCupSize = selectedCupSize.value,  // Pass the selected cup size here
                onWaterIntake = { record ->
                    waterRecords.add(0, record)
                    updateRecyclerView()
                    // Log water intake to database
                    logWaterIntake(record)
                    // Update streak and goal percentage
                    updateStreakAndGoalPercentage()
                }
            )
        }

        // Update streak and goal percentage initially
        updateStreakAndGoalPercentage()
    }

    private fun updateStreakAndGoalPercentage() {
        // Calculate the streak of consecutive days with goal achieved
        val streak = calculateStreak()

        // Set streak text
        streakTextView.text = "$streak"

        // Calculate the goal percentage based on the user's intake for the current day
        val totalIntakeToday = waterRecords.sumOf { it.amount.toInt() } // Sum of today's water intake
        val dailyGoal = 2150  // Goal amount per day (in ml)

        // If the user hasn't met the goal yet, calculate the percentage
        val goalPercentage = if (totalIntakeToday > 0) {
            val percentage = (totalIntakeToday.toFloat() / dailyGoal) * 100
            percentage.coerceIn(0f, 100f)  // Ensure it stays between 0% and 100%
        } else {
            0f  // If no intake, set percentage to 0
        }

        // Set goal percentage text
        goalPercentageTextView.text = "${goalPercentage.toInt()}%"
    }

    private fun calculateStreak(): Int {
        val waterIntakes = dbHelper.getWaterIntakeHistory(userId, getCurrentDate())
        var streak = 0
        var isStreakActive = true

        // Loop through water intakes to check streak
        for (intake in waterIntakes) {
            if (intake.amount >= 2150) {
                streak++
            } else {
                isStreakActive = false
            }
        }
        return streak
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.nav_selected_icon_color),
            ContextCompat.getColor(this, R.color.nav_unselected_icon_color)
        )

        val colorStateList = ColorStateList(states, colors)

        bottomNav.itemIconTintList = colorStateList
        bottomNav.itemTextColor = colorStateList
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_analytics -> {
                    // Pass water records to AnalyticsActivity
                    val intent = Intent(this, AnalyticsActivity::class.java)
                    intent.putExtra("waterRecords", ArrayList(waterRecords.map { "${it.time}|${it.amount}" }))
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
            dialog.dismiss()
        }

        // Setting up button listeners for each cup size
        setCupSizeButtonClickListener(dialog, R.id.btn_100ml, "100 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_125ml, "125 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_150ml, "150 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_175ml, "175 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_200ml, "200 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_300ml, "300 ml")
        setCupSizeButtonClickListener(dialog, R.id.btn_400ml, "400 ml")

        val customizeButton = dialog.findViewById<ImageButton>(R.id.btn_customize)
        customizeButton.setOnClickListener {
            showCustomizePopup()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            Toast.makeText(this, "Cup Size Confirmed: ${selectedCupSize.value} ml", Toast.LENGTH_SHORT).show()
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
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        val cupSizeInput = dialog.findViewById<EditText>(R.id.cupSizeInput)

        confirmButton.setOnClickListener {
            val customCupSize = cupSizeInput.text.toString().toIntOrNull()

            if (customCupSize != null && customCupSize > 0) {
                selectedCupSize.value = customCupSize
                Toast.makeText(this, "Custom Cup Size Confirmed: ${selectedCupSize.value} ml", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid cup size", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setCupSizeButtonClickListener(dialog: Dialog, buttonId: Int, cupSizeText: String) {
        val button = dialog.findViewById<ImageButton>(buttonId)
        button.setOnClickListener {
            // Remove the " ml" from the cupSizeText (e.g., "150 ml" -> "150")
            val cupSize = cupSizeText.replace(" ml", "").toIntOrNull()

            if (cupSize != null) {
                selectedCupSize.value = cupSize
                Toast.makeText(this, "Selected: ${selectedCupSize.value} ml", Toast.LENGTH_SHORT).show()
                highlightSelectedButton(dialog, button)
            } else {
                Toast.makeText(this, "Invalid cup size", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun highlightSelectedButton(dialog: Dialog, selectedButton: ImageButton) {
        val gridLayout = dialog.findViewById<GridLayout>(R.id.gridLayout)
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as ImageButton
            button.setBackgroundResource(0)
        }
        selectedButton.setBackgroundColor(resources.getColor(R.color.grey))
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

    private fun getCurrentUserId(): Long {
        val sharedPreferences = getSharedPreferences("AquaBuddyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("user_id", -1L)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun startNextSipCountdown(nextSipTextView: TextView) {
        val notificationFrequency = 60  // Replace with the actual notification frequency from the user settings or profile

        // Assuming you want to start with a timer
        val timer = Timer()
        val task = object : TimerTask() {
            var timeLeft = notificationFrequency

            override fun run() {
                runOnUiThread {
                    nextSipTextView.text = "$timeLeft Mins"
                }

                timeLeft--

                if (timeLeft < 0) {
                    timeLeft = notificationFrequency  // Reset the timer if it's time for the next notification
                }
            }
        }

        // Schedule the timer to update every minute
        timer.scheduleAtFixedRate(task, 0, 60000)  // 60000ms = 1 minute
    }

    private fun logWaterIntake(record: WaterRecord) {
        val intake = WaterIntake(
            userId = userId,
            amount = record.amount.toInt(), // Convert to Int
            date = getCurrentDate(),
            time = getCurrentTime(),
            createdAt = getCurrentDateTime()
        )
        dbHelper.logWaterIntake(intake)
    }
}
