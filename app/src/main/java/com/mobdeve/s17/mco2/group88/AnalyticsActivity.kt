package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserId: Long = -1L

    private val waterRecords = mutableListOf<WaterRecord>()
    private var selectedDateString: String? = null
    private var currentDisplayedMonth: Int = LocalDate.now().monthValue
    private var currentDisplayedYear: Int = LocalDate.now().year

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize database helper and shared preferences
        initializeDatabase()

        // Check if user is logged in and get user ID
        if (!checkUserSession()) {
            redirectToLogin()
            return
        }

        // Load water records from database
        loadWaterRecordsFromDatabase()

        setupCalendarView()
        updateWaterReport()
        setupCalendarProgress()
        setupBottomNavigation()
    }

    private fun initializeDatabase() {
        dbHelper = AquaBuddyDatabaseHelper(this)
        sharedPreferences = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun checkUserSession(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
        currentUserId = sharedPreferences.getLong(LoginActivity.KEY_USER_ID, -1L)

        return isLoggedIn && currentUserId != -1L
    }

    private fun redirectToLogin() {
        Toast.makeText(this, "Please log in to view analytics", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadWaterRecordsFromDatabase() {
        Thread {
            try {
                // Get last 30 days summary from database
                val dailySummaries = dbHelper.getLast30DaysSummary(currentUserId)

                // Convert DailyIntakeSummary to WaterRecord format
                val records = dailySummaries.map { summary ->
                    WaterRecord(summary.date, summary.totalIntake.toString())
                }

                runOnUiThread {
                    waterRecords.clear()
                    waterRecords.addAll(records)

                    // Refresh UI components after loading data
                    refreshUIWithNewData()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AnalyticsActivity,
                        "Error loading water intake data", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshUIWithNewData() {
        // Update calendar decorators
        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.removeDecorators()
        val waterProgressDecorator = WaterProgressDecoratorWithDate(this, waterRecords)
        calendarView.addDecorator(waterProgressDecorator)

        // Update water report with new data
        updateWaterReport()

        // Update calendar progress
        setupCalendarProgress()
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
        bottomNav.selectedItemId = R.id.nav_analytics

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_analytics -> {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendarView() {
        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        calendarView.setSelectedDate(CalendarDay.today())
        selectedDateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Add the water progress decorator
        val waterProgressDecorator = WaterProgressDecoratorWithDate(this, waterRecords)
        calendarView.addDecorator(waterProgressDecorator)

        calendarView.setOnDateChangedListener { widget, date, selected ->
            selectedDateString = LocalDate.of(date.year, date.month, date.day)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            updateCalendarProgressForDate(date)
        }

        // Track month changes to update decorator
        calendarView.setOnMonthChangedListener { widget, date ->
            currentDisplayedMonth = date.month
            currentDisplayedYear = date.year
            // Update the decorator with new month/year info
            calendarView.removeDecorators()
            val updatedDecorator = WaterProgressDecoratorWithDate(this, waterRecords, currentDisplayedYear, currentDisplayedMonth)
            calendarView.addDecorator(updatedDecorator)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun extractWeeklyData(): List<Float> {
        val result = MutableList(7) { 0f }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        for (i in 0..6) {
            val targetDate = today.minusDays(6 - i.toLong())
            val dateString = targetDate.format(formatter)
            val record = waterRecords.find { it.time == dateString }
            result[i] = record?.amount?.toFloat() ?: 0f
        }

        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun extractMonthlyData(): List<Float> {
        val result = MutableList(12) { 0f }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (record in waterRecords) {
            try {
                val date = LocalDate.parse(record.time, formatter)
                val month = date.monthValue - 1
                if (month in 0..11) {
                    result[month] += record?.amount?.toFloat() ?: 0f
                }
            } catch (e: Exception) {
                // Handle parsing errors
            }
        }

        return result
    }

    // Add this method to get individual intake records for frequency calculation
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getWeeklyIntakeFrequency(): Double {
        return try {
            val now = LocalDate.now()
            val weekAgo = now.minusDays(6)

            // Get individual intake records for the last 7 days, not daily summaries
            val weeklyIntakeRecords = dbHelper.getIntakeRecordsBetweenDates(
                currentUserId,
                weekAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )

            // Calculate average intake sessions per day
            if (weeklyIntakeRecords.isNotEmpty()) {
                weeklyIntakeRecords.size / 7.0
            } else {
                0.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    // Modified updateWaterReport method
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateWaterReport() {
        Thread {
            try {
                val now = LocalDate.now()
                val weekAgo = now.minusDays(6)
                val monthAgo = now.withDayOfMonth(1)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val weeklyEntries = waterRecords.filter {
                    try {
                        val date = LocalDate.parse(it.time, formatter)
                        date in weekAgo..now
                    } catch (e: Exception) {
                        false
                    }
                }

                val monthlyEntries = waterRecords.filter {
                    try {
                        val date = LocalDate.parse(it.time, formatter)
                        date >= monthAgo
                    } catch (e: Exception) {
                        false
                    }
                }

                // Get user's daily goal from database
                val user = dbHelper.getUserById(currentUserId)
                val dailyGoal = user?.dailyWaterGoal?.toDouble() ?: 2000.0

                val weeklyAvg = if (weeklyEntries.isNotEmpty()) {
                    weeklyEntries.sumOf { it.amount?.toDouble() ?: 0.0 } / 7
                } else 0.0

                val monthlyAvg = if (monthlyEntries.isNotEmpty()) {
                    monthlyEntries.sumOf { it.amount?.toDouble() ?: 0.0 } / now.lengthOfMonth()
                } else 0.0

                val completionRate = if (weeklyEntries.isNotEmpty()) {
                    (weeklyEntries.count { (it.amount?.toDouble() ?: 0.0) >= dailyGoal } / 7.0) * 100
                } else 0.0

                // FIXED: Get actual intake frequency instead of daily summary count
                val frequency = getWeeklyIntakeFrequency()

                runOnUiThread {
                    updateTextViewsInAllLayouts(weeklyAvg, monthlyAvg, completionRate, frequency)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AnalyticsActivity,
                        "Error calculating water report", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun updateTextViewsInAllLayouts(
        weeklyAvg: Double,
        monthlyAvg: Double,
        completionRate: Double,
        frequency: Double
    ) {
        val weeklyText = "${weeklyAvg.toInt()}ml / Day"
        val monthlyText = "${monthlyAvg.toInt()}ml / Day"
        val completionText = "${completionRate.toInt()}%"
        val frequencyText = "${"%.1f".format(frequency)} Times / Day"

        try {
            val calendarLayout = findViewById<View>(R.id.calendarLayout)
            calendarLayout.findViewById<TextView>(R.id.wAverageTv)?.text = weeklyText
            calendarLayout.findViewById<TextView>(R.id.mAverageTv)?.text = monthlyText
            calendarLayout.findViewById<TextView>(R.id.completionTv)?.text = completionText
            calendarLayout.findViewById<TextView>(R.id.frequencyTv)?.text = frequencyText
        } catch (e: Exception) {
            // Handle if views not found
        }
    }

    private fun setupCalendarProgress() {
        val calendarProgressComposeView = findViewById<ComposeView>(R.id.calendarProgressComposeView)
        calendarProgressComposeView.setContent {
            CalendarProgressComposable(
                records = waterRecords,
                selectedDate = selectedDateString
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendarProgressForDate(date: CalendarDay) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = LocalDate.of(date.year, date.month, date.day).format(formatter)

        val calendarProgressComposeView = findViewById<ComposeView>(R.id.calendarProgressComposeView)
        calendarProgressComposeView.setContent {
            CalendarProgressComposable(
                records = waterRecords,
                selectedDate = dateString
            )
        }
    }

    // Method to refresh data (call this when returning from other activities)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshData() {
        loadWaterRecordsFromDatabase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        if (currentUserId != -1L) {
            refreshData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}