package com.mobdeve.s17.mco2.group88

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
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

        // If no records were passed, you can still add some default/empty state
        // but don't add random sample data
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
}