package com.mobdeve.s17.mco2.group88

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import java.time.LocalDate
import java.time.DayOfWeek

@Composable
fun WeekBar(userProgress: Array<Int>) {
    val weekDays = arrayOf("S", "M", "T", "W", "T", "F", "S")

    // Get the current day of the week from LocalDate
    val currentDayOfWeek = LocalDate.now().dayOfWeek
    val currentDay = when (currentDayOfWeek) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 0
        else -> 0 // Fallback case
    }

    // Create a horizontal row to hold the days and their progress bars
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly // Space evenly to ensure they're not too far apart
    ) {
        weekDays.forEachIndexed { index, day ->
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f), // Ensure equal width for each day
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(45.dp) // Circle size
                        .padding(4.dp)
                ) {
                    // Draw circular progress around the day text
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val size = size.minDimension
                        val strokeWidth = 6f // Stroke width for the circle

                        // Determine progress for the day based on the current day
                        val progress = when {
                            index > currentDay -> 0f // No progress for future days
                            else -> userProgress[index] / 100f // Normal progress for current and past days
                        }

                        // Draw the progress circle
                        drawArc(
                            color = when {
                                userProgress[index] == 100 -> ComposeColor(0xFF32CD32) // Green (Completed)
                                index < currentDay -> ComposeColor(0xFF64B5F6) // Light Blue (In Progress)
                                else -> ComposeColor(0xFF6E8DAE) // Grayish Blue (Future Day)
                            },
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        // Draw the background arc
                        drawArc(
                            color = Color.Gray.copy(alpha = 0.3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
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

@Preview
@Composable
fun PreviewWeekBar() {
    // Example progress array where each element represents progress for each day (0 - 100)
    val userProgress = arrayOf(100, 80, 60, 40, 0, 0, 0)
    WeekBar(userProgress = userProgress)
}