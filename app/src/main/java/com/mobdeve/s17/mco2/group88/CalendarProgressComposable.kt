// CalendarProgressComposable.kt
package com.mobdeve.s17.mco2.group88

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarProgressComposable(records: List<WaterRecord>, goalAmount: Int = 2150) {
    val strokeWidthDp = 23.dp
    val sizeDp = 180.dp
    val strokeWidthPx = with(LocalDensity.current) { strokeWidthDp.toPx() }

    // Calculate today's total intake from records
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val todayDateString = today.format(formatter)

    // Sum up all water records for today (since records contain individual intake amounts)
    val currentIntake = records
        .filter { it.time == todayDateString }
        .sumOf { it.amount?.toDouble() ?: 0.0 }
        .toInt()

    val progress = (currentIntake.toFloat() / goalAmount).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(strokeWidthDp / 2)
        ) {
            val diameter = size.minDimension - strokeWidthPx
            val arcRect = Rect(
                offset = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(diameter, diameter)
            )

            // Background arc
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            // Progress arc
            if (progress > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF64B5F6), Color(0xFF2196F3)),
                        start = Offset(0f, 0f),
                        end = Offset(diameter, diameter)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = arcRect.topLeft,
                    size = arcRect.size
                )
            }
        }

        // Content in the center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Current intake display
            Text(
                text = "${currentIntake}ml",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Goal text
            Text(
                text = "Goal ${goalAmount}ml",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            // Progress percentage
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}