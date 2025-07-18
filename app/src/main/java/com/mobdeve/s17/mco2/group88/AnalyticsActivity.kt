package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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

    private val waterRecords = mutableListOf<WaterRecord>()
    private var selectedDateString: String? = null
    private var currentDisplayedMonth: Int = LocalDate.now().monthValue
    private var currentDisplayedYear: Int = LocalDate.now().year

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Get real data from HomeActivity instead of generating sample data
        loadWaterRecordsFromIntent()

        setupCalendarView()
        updateWaterReport()
        setupCalendarProgress()
        setupBottomNavigation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadWaterRecordsFromIntent() {
        // Get the water records passed from HomeActivity
        val recordStrings = intent.getStringArrayListExtra("waterRecords")
        if (recordStrings != null) {
            waterRecords.clear()
            for (recordString in recordStrings) {
                val parts = recordString.split("|")
                if (parts.size == 2) {
                    waterRecords.add(WaterRecord(parts[0], parts[1]))
                }
            }
        }

        // Add sample data if no real records exist (for testing/demonstration)
        if (waterRecords.isEmpty()) {
            generateSampleWaterRecords()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSampleWaterRecords() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        // Generate sample data for the past 30 days
        for (i in 0..29) {
            val date = today.minusDays(i.toLong())
            val dateString = date.format(formatter)

            // Create varied water intake amounts to show different progress levels
            val intake = when (i % 7) {
                0 -> 2500 // Exceeded goal
                1 -> 2150 // Exactly at goal
                2 -> 1800 // Good progress
                3 -> 1200 // Moderate progress
                4 -> 800  // Low progress
                5 -> 0    // No intake
                6 -> 1600 // Decent progress
                else -> 1000
            }

            if (intake > 0) {
                waterRecords.add(WaterRecord(dateString, intake.toString()))
            }
        }

        // Add some specific dates with high intake for current month
        val currentMonth = today.month
        val currentYear = today.year

        // Add records for specific days in current month
        val specificDays = listOf(1, 5, 10, 15, 20, 25)
        for (day in specificDays) {
            try {
                val specificDate = LocalDate.of(currentYear, currentMonth, day)
                val dateString = specificDate.format(formatter)

                // Remove any existing record for this date
                waterRecords.removeAll { it.time == dateString }

                // Add new record with high intake
                val intake = when (day) {
                    1 -> 2300  // 107% of goal
                    5 -> 1900  // 88% of goal
                    10 -> 2150 // 100% of goal
                    15 -> 1400 // 65% of goal
                    20 -> 2600 // 121% of goal
                    25 -> 1100 // 51% of goal
                    else -> 1500
                }

                waterRecords.add(WaterRecord(dateString, intake.toString()))
            } catch (e: Exception) {
                // Skip if day doesn't exist in current month
            }
        }

        // Add today's record
        waterRecords.removeAll { it.time == today.format(formatter) }
        waterRecords.add(WaterRecord(today.format(formatter), "1750"))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateWaterReport() {
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

        val weeklyAvg = if (weeklyEntries.isNotEmpty()) {
            weeklyEntries.sumOf { it.amount?.toDouble() ?: 0.0 } / 7
        } else 0.0

        val monthlyAvg = if (monthlyEntries.isNotEmpty()) {
            monthlyEntries.sumOf { it.amount?.toDouble() ?: 0.0 } / now.lengthOfMonth()
        } else 0.0

        val completionRate = if (weeklyEntries.isNotEmpty()) {
            (weeklyEntries.count { (it.amount?.toFloat() ?: 0f) >= 2000f } / 7.0) * 100
        } else 0.0

        val frequency = if (weeklyEntries.isNotEmpty()) {
            weeklyEntries.size / 7.0
        } else 0.0

        updateTextViewsInAllLayouts(weeklyAvg, monthlyAvg, completionRate, frequency)
    }

    private fun updateTextViewsInAllLayouts(
        weeklyAvg: Double,
        monthlyAvg: Double,
        completionRate: Double,
        frequency: Double
    ) {
        val weeklyText = "Weekly Average: ${weeklyAvg.toInt()}ml / Day"
        val monthlyText = "Monthly Average: ${monthlyAvg.toInt()}ml / Day"
        val completionText = "Average Completion: ${completionRate.toInt()}%"
        val frequencyText = "Drink Frequency: ${"%.1f".format(frequency)} Times / Day"

        try {
            val calendarLayout = findViewById<View>(R.id.calendarLayout)
            calendarLayout.findViewById<TextView>(R.id.weeklyAverageText)?.text = weeklyText
            calendarLayout.findViewById<TextView>(R.id.monthlyAverageText)?.text = monthlyText
            calendarLayout.findViewById<TextView>(R.id.averageCompletionText)?.text = completionText
            calendarLayout.findViewById<TextView>(R.id.drinkFrequencyText)?.text = frequencyText
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
}