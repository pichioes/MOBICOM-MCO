package com.mobdeve.s17.mco2.group88

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.DayOfWeek
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@Composable
@RequiresApi(Build.VERSION_CODES.O) // Only for API level 26 and higher
fun WeekBar(userProgress: Array<Int>) { // Accept userProgress as a parameter
    val weekDays = arrayOf("S", "M", "T", "W", "T", "F", "S")

    // Multiple methods to get current day for debugging
    val calendar = Calendar.getInstance()
    val systemDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // Also try with SimpleDateFormat for comparison
    val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val currentDayName = dateFormat.format(Date())

    // Also try System.currentTimeMillis
    val currentTime = System.currentTimeMillis()
    val calendarFromMillis = Calendar.getInstance().apply { timeInMillis = currentTime }
    val dayFromMillis = calendarFromMillis.get(Calendar.DAY_OF_WEEK)

    // Convert Calendar day to array index
    val currentDay = when (systemDayOfWeek) {
        Calendar.SUNDAY -> 0
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 0
    }

    // Extensive debug output
    println("=== DAY DEBUG INFO ===")
    println("Calendar.DAY_OF_WEEK: $systemDayOfWeek")
    println("Day from millis: $dayFromMillis")
    println("SimpleDateFormat day: $currentDayName")
    println("Current time millis: $currentTime")
    println("Calculated array index: $currentDay")
    println("======================")

    // Create a horizontal row to hold the days and their progress bars
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ComposeColor(0xFF284A6B), shape = RoundedCornerShape(12.dp)) // Apply background color with rounded corners
            .padding(3.dp) // Padding inside the rounded box
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center // Center the row content
        ) {
            weekDays.forEachIndexed { index, day ->
                Column(
                    modifier = Modifier
                        .padding(2.dp)
                        .weight(1f), // Ensure equal width for each day
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp) // Circle size
                            .padding(4.dp)
                    ) {
                        // Draw circular progress around the day text
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val size = size.minDimension
                            val strokeWidth = 6f // Stroke width for the circle

                            // Check if there's valid data for this day
                            val hasData = userProgress.size > index && userProgress[index] >= 0

                            // Determine progress for the day based on the current day and data availability
                            val progress = when {
                                !hasData -> 0f // No data available, show no progress
                                index > currentDay -> 0f // Future days show no progress
                                else -> (userProgress[index].coerceIn(0, 100)) / 100f // Current and past days with valid data
                            }

                            // Draw the background arc first
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.3f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            // Draw the progress circle on top
                            if (progress > 0f && hasData) {
                                drawArc(
                                    color = when {
                                        userProgress[index] >= 100 -> ComposeColor(0xFF32CD32) // Green (Completed)
                                        index == currentDay -> ComposeColor(0xFFFFD700) // Gold for current day with progress
                                        index < currentDay -> ComposeColor(0xFF64B5F6) // Light Blue (Past days with progress)
                                        else -> ComposeColor(0xFF6E8DAE) // Grayish Blue (shouldn't reach here)
                                    },
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }

                        // Day Text in the Center of the Circle
                        Text(
                            text = day,
                            color = ComposeColor.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}