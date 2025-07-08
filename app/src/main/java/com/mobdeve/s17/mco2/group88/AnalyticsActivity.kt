// AnalyticsActivity.kt
package com.mobdeve.s17.mco2.group88

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    // Sample data - replace with your actual data source
    private val waterRecords = mutableListOf<WaterRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize with sample data
        initializeSampleData()

        setupToggleButtons()
        setupCalendarView()
        setupGraphs()
        updateWaterReport()
        setupCalendarProgress()
    }

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

    private fun setupToggleButtons() {
        val calendarToggleBtn = findViewById<Button>(R.id.calendarToggleBtn)
        val analyticsToggleBtn = findViewById<Button>(R.id.analyticsToggleBtn)
        val calendarLayout = findViewById<View>(R.id.calendarLayout)
        val graphLayout = findViewById<View>(R.id.graphLayout)

        calendarToggleBtn.setOnClickListener {
            calendarLayout.visibility = View.VISIBLE
            graphLayout.visibility = View.GONE

            // Update button states
            calendarToggleBtn.isEnabled = false
            analyticsToggleBtn.isEnabled = true
        }

        analyticsToggleBtn.setOnClickListener {
            calendarLayout.visibility = View.GONE
            graphLayout.visibility = View.VISIBLE

            // Update button states
            calendarToggleBtn.isEnabled = true
            analyticsToggleBtn.isEnabled = false
        }

        val weeklyGraphBtn = findViewById<Button>(R.id.weeklyGraphBtn)
        val monthlyGraphBtn = findViewById<Button>(R.id.monthlyGraphBtn)
        val weeklyGraphLayout = findViewById<View>(R.id.weeklyGraphLayout)
        val monthlyGraphLayout = findViewById<View>(R.id.monthlyGraphLayout)

        weeklyGraphBtn.setOnClickListener {
            weeklyGraphLayout.visibility = View.VISIBLE
            monthlyGraphLayout.visibility = View.GONE

            // Update button states
            weeklyGraphBtn.isEnabled = false
            monthlyGraphBtn.isEnabled = true
        }

        monthlyGraphBtn.setOnClickListener {
            weeklyGraphLayout.visibility = View.GONE
            monthlyGraphLayout.visibility = View.VISIBLE

            // Update button states
            weeklyGraphBtn.isEnabled = true
            monthlyGraphBtn.isEnabled = false
        }
    }

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

    private fun setupGraphs() {
        setupWeeklyGraph()
        setupMonthlyGraph()
    }

    private fun setupWeeklyGraph() {
        val weeklyGraphView = findViewById<GraphView>(R.id.weeklyGraphView)
        val weeklyData = extractWeeklyData()

        val dataPoints = weeklyData.mapIndexed { index, value ->
            DataPoint(index.toDouble(), value.toDouble())
        }.toTypedArray()

        val series = BarGraphSeries(dataPoints)
        series.color = Color.parseColor("#4F8CFF")
        series.spacing = 20

        weeklyGraphView.removeAllSeries()
        weeklyGraphView.addSeries(series)

        // Customize the graph
        weeklyGraphView.viewport.isXAxisBoundsManual = true
        weeklyGraphView.viewport.setMinX(0.0)
        weeklyGraphView.viewport.setMaxX(6.0)
        weeklyGraphView.viewport.isYAxisBoundsManual = true
        weeklyGraphView.viewport.setMinY(0.0)
        weeklyGraphView.viewport.setMaxY(3000.0)

        // Set labels
        weeklyGraphView.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        weeklyGraphView.gridLabelRenderer.verticalLabelsColor = Color.WHITE
        weeklyGraphView.gridLabelRenderer.gridColor = Color.GRAY
    }

    private fun setupMonthlyGraph() {
        val monthlyGraphView = findViewById<GraphView>(R.id.monthlyGraphView)
        val monthlyData = extractMonthlyData()

        val dataPoints = monthlyData.mapIndexed { index, value ->
            DataPoint(index.toDouble(), value.toDouble())
        }.toTypedArray()

        val series = BarGraphSeries(dataPoints)
        series.color = Color.parseColor("#4F8CFF")
        series.spacing = 20

        monthlyGraphView.removeAllSeries()
        monthlyGraphView.addSeries(series)

        // Customize the graph
        monthlyGraphView.viewport.isXAxisBoundsManual = true
        monthlyGraphView.viewport.setMinX(0.0)
        monthlyGraphView.viewport.setMaxX(11.0)
        monthlyGraphView.viewport.isYAxisBoundsManual = true
        monthlyGraphView.viewport.setMinY(0.0)
        monthlyGraphView.viewport.setMaxY(50000.0)

        // Set labels
        monthlyGraphView.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        monthlyGraphView.gridLabelRenderer.verticalLabelsColor = Color.WHITE
        monthlyGraphView.gridLabelRenderer.gridColor = Color.GRAY
    }

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

        // Update weekly graph layout TextViews
        try {
            val weeklyGraphLayout = findViewById<View>(R.id.weeklyGraphLayout)
            weeklyGraphLayout.findViewById<TextView>(R.id.weeklyAverageText)?.text = weeklyText
            weeklyGraphLayout.findViewById<TextView>(R.id.monthlyAverageText)?.text = monthlyText
            weeklyGraphLayout.findViewById<TextView>(R.id.averageCompletionText)?.text = completionText
            weeklyGraphLayout.findViewById<TextView>(R.id.drinkFrequencyText)?.text = frequencyText
        } catch (e: Exception) {
            // Handle if views not found
        }

        // Update monthly graph layout TextViews
        try {
            val monthlyGraphLayout = findViewById<View>(R.id.monthlyGraphLayout)
            monthlyGraphLayout.findViewById<TextView>(R.id.weeklyAverageText)?.text = weeklyText
            monthlyGraphLayout.findViewById<TextView>(R.id.monthlyAverageText)?.text = monthlyText
            monthlyGraphLayout.findViewById<TextView>(R.id.averageCompletionText)?.text = completionText
            monthlyGraphLayout.findViewById<TextView>(R.id.drinkFrequencyText)?.text = frequencyText
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