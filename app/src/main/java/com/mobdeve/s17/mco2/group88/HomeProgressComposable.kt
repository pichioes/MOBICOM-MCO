package com.mobdeve.s17.mco2.group88

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CircularProgressWithCap(
    goalAmount: Int = 2150,
    selectedCupSize: Int,  // Accept selected cup size as a parameter
    onWaterIntake: (WaterRecord) -> Unit
) {
    val strokeWidthDp = 23.dp
    val sizeDp = 260.dp
    val strokeWidthPx = with(LocalDensity.current) { strokeWidthDp.toPx() }
    val outfit = FontFamily(Font(R.font.outfit))

    var currentIntake by remember { mutableStateOf(0) }
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
                color = Color.White,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress arc (counter-clockwise)
            drawArc(
                brush = Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF2196F3))),
                startAngle = -90f,
                sweepAngle = -360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Number display
        Text(
            text = currentIntake.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = outfit
        )

        // Goal text
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Goal $goalAmount ml",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = outfit
            )
        }

        // Water drop button
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 150.dp),
            contentAlignment = Alignment.Center
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Icon(
                painter = painterResource(id = R.drawable.water_drop),
                contentDescription = "Add Water",
                tint = if (isPressed) Color(0xFF5091C5).copy(alpha = 0.8f) else Color.Unspecified,
                modifier = Modifier
                    .size(72.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        // Update the current intake
                        currentIntake += selectedCupSize // Update based on selected cup size

                        // Get the current time in 12-hour format
                        val currentTime = LocalTime.now()
                        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                        val formattedTime = currentTime.format(formatter)

                        // Create a new water record
                        val record = WaterRecord(time = formattedTime, amount = selectedCupSize.toString())  // Convert to String without "ml" suffix

                        // Trigger the onWaterIntake callback
                        onWaterIntake(record)
                    }
            )
        }
    }
}
