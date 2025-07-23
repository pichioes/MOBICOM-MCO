package com.mobdeve.s17.mco2.group88

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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate


class HomeActivity : AppCompatActivity() {

    private val waterRecords = mutableListOf<WaterRecord>()
    private var selectedCupSize = mutableStateOf(250)  // Using mutableStateOf to track cup size state
    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var userNameTextView: TextView
    private lateinit var streakTextView: TextView
    private lateinit var goalPercentageTextView: TextView
    private lateinit var nextSipTextView: TextView  // Added as class property for easier access
    private var userId: Long = -1
    private var currentIntake = mutableStateOf(0) // This will store the current intake for the day.

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

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
        nextSipTextView = findViewById<TextView>(R.id.NextSip)  // Initialize as class property

        // Set the username in the TextView
        userNameTextView.text = user?.name ?: "User"

        setupBottomNavigation()

        // Fetch water intake records from the database
        fetchWaterIntake()

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
        startNextSipCountdown(nextSipTextView)

        val weekBarComposeView = findViewById<ComposeView>(R.id.WeekBar)
        weekBarComposeView.setContent {
            // Fetch the progress for the last 7 days
            val progressForLast7Days = calculateWaterIntakeProgress(dbHelper)

            // Pass the progress to the WeekBar composable
            WeekBar(userProgress = progressForLast7Days) // Ensure that userProgress is passed here
        }

        // Compose view for the CircularProgressWithCap (water intake progress)
        val circularProgressComposeView = findViewById<ComposeView>(R.id.composeProgress)
        circularProgressComposeView.setContent {
            CircularProgressWithCap(
                goalAmount = 2150,
                selectedCupSize = selectedCupSize.value,  // Pass the selected cup size here
                currentIntake = currentIntake.value, // Pass current intake here
                onWaterIntake = { record ->
                    // Add new record to the beginning of the list to show newest first
                    waterRecords.add(0, record)
                    updateRecyclerView()
                    // Log water intake to database
                    logWaterIntake(record)
                    // Update streak and goal percentage
                    updateStreakAndGoalPercentage()

                    // Update the current intake when water is added
                    currentIntake.value += selectedCupSize.value

                    // RESET THE TIMER WHEN NEW WATER INTAKE IS RECORDED
                    resetTimer(nextSipTextView)
                }
            )
        }

        // Update streak and goal percentage initially
        updateStreakAndGoalPercentage()
    }

    // Function to fetch water intake records
    private fun fetchWaterIntake() {
        val waterIntakes = dbHelper.getWaterIntakeHistory(userId, getCurrentDate())
        var totalToday = 0
        waterRecords.clear()

        // Sort water intakes by time in reverse chronological order (newest first)
        val sortedIntakes = waterIntakes.sortedByDescending { intake ->
            // Parse the time string and convert to comparable format
            parseTimeToMinutes(intake.time)
        }

        // Add records to the list in reverse chronological order (newest first)
        for (intake in sortedIntakes) {
            val record = WaterRecord(
                time = intake.time,
                amount = intake.amount.toString()
            )
            waterRecords.add(record) // Add to end of list since we already sorted in reverse
            totalToday += intake.amount // Add the amount to the total for the day
        }

        currentIntake.value = totalToday // Set the current intake for the day
    }

    // Helper function to parse time string to minutes for sorting
    private fun parseTimeToMinutes(timeString: String): Int {
        try {
            val format = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = format.parse(timeString)
            val calendar = Calendar.getInstance()
            calendar.time = date ?: return 0
            return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        } catch (e: Exception) {
            return 0 // Return 0 if parsing fails
        }
    }

    private fun resetTimer(nextSipTextView: TextView) {
        // Cancel the existing timer if it exists
        timerTask?.cancel()
        timer?.cancel()

        // Start a new timer
        startNextSipCountdown(nextSipTextView)
    }

    private fun updateStreakAndGoalPercentage() {
        // Calculate the streak of consecutive days with goal achieved
        val streak = calculateStreak()

        // Set streak text
        streakTextView.text = "$streak"

        // Calculate the goal percentage based on the user's intake for the current day
        val dailyGoal = 2150  // Goal amount per day (in ml)

        // If the user hasn't met the goal yet, calculate the percentage
        val goalPercentage = if (currentIntake.value > 0) {
            val percentage = (currentIntake.value.toFloat() / dailyGoal) * 100
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

        // Loop through water intakes to check streak
        for (intake in waterIntakes) {
            if (intake.amount >= 2150) {
                streak++
            }
        }
        return streak
    }

    // Function to calculate water intake progress for the last 7 days
    private fun calculateWaterIntakeProgress(dbHelper: AquaBuddyDatabaseHelper): Array<Int> {
        val progressArray = Array(7) { -1 } // Initialize with -1 to indicate no data

        // Get current date
        val today = LocalDate.now()

        // Get current day of week (0 = Sunday, 1 = Monday, etc.)
        val calendar = Calendar.getInstance()
        val todayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 0
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 0
        }

        println("=== PROGRESS CALCULATION DEBUG ===")
        println("Today index: $todayIndex")

        // Calculate progress for each day of the current week
        for (dayOffset in 0..6) {
            // Calculate which day of the week this represents
            val dayIndex = (todayIndex - dayOffset + 7) % 7

            // Get the actual date for this day
            val date = today.minusDays(dayOffset.toLong()).toString()

            // Get water intake data for this date
            val waterIntakes = dbHelper.getWaterIntakeHistory(userId, date)

            // Calculate total intake for the day
            val totalIntakeForDay = waterIntakes.sumOf { it.amount }

            // Calculate progress percentage (daily goal is 2150 ml)
            val progress = if (totalIntakeForDay > 0) {
                ((totalIntakeForDay.toFloat() / 2150) * 100).toInt().coerceIn(0, 100)
            } else {
                0 // 0 means no intake, -1 means no data (which we don't use here)
            }

            progressArray[dayIndex] = progress

            println("Day offset: $dayOffset, Day index: $dayIndex, Date: $date, Progress: $progress")
        }

        println("Final progress array: ${progressArray.contentToString()}")
        println("===================================")

        return progressArray
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

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun startNextSipCountdown(nextSipTextView: TextView) {
        val notificationFrequency = 60  // Replace with the actual notification frequency from the user settings or profile

        // Create a new timer and task
        timer = Timer()
        timerTask = object : TimerTask() {
            var timeLeft = notificationFrequency + 1

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
        timer?.scheduleAtFixedRate(timerTask, 0, 60000)  // 60000ms = 1 minute
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

    override fun onDestroy() {
        super.onDestroy()
        // Clean up timer when activity is destroyed
        timerTask?.cancel()
        timer?.cancel()
    }
}