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
// import com.jjoe64.graphview.GraphView
// import com.jjoe64.graphview.series.BarGraphSeries
// import com.jjoe64.graphview.series.DataPoint
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    // Sample data - replace with your actual data source
    private val waterRecords = mutableListOf<WaterRecord>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize with sample data
        initializeSampleData()
        setupCalendarView()
        updateWaterReport()
        setupCalendarProgress()

        // Set up Bottom Navigation for Analytics
        setupBottomNavigation()  // Ensure you have this method here
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

        // Set the selected item to Analytics by default
        bottomNav.selectedItemId = R.id.nav_analytics  // Ensure Analytics is selected by default

        // Handle item selection
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_analytics -> {
                    // Stay on AnalyticsActivity
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
    private fun initializeSampleData() {
        // Sample data for demonstration - replace with your actual data loading
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        // Add sample data for the past week
        for (i in 0..6) {
            val date = today.minusDays(i.toLong())
            val amount = (1500..2500).random()
            waterRecords.add(WaterRecord(date.format(formatter), amount.toFloat().toString()))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendarView() {
        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        // Set up calendar with current date
        calendarView.setSelectedDate(CalendarDay.today())

        // Add click listener for date selection
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // Handle date selection - update progress ring for selected date
            updateCalendarProgressForDate(date)
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
                    result[month] += record.amount?.toFloat() ?: 0f
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

        // Update TextViews in all layouts
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

        // Update calendar layout TextViews
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
            CalendarProgressComposable(records = waterRecords)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendarProgressForDate(date: CalendarDay) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = LocalDate.of(date.year, date.month, date.day).format(formatter)

        // Filter records for selected date
        val selectedDateRecords = waterRecords.filter { it.time == dateString }

        val calendarProgressComposeView = findViewById<ComposeView>(R.id.calendarProgressComposeView)
        calendarProgressComposeView.setContent {
            CalendarProgressComposable(records = selectedDateRecords)
        }
    }
}
