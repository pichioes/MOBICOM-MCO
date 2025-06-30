package com.mobdeve.s17.mco2.group88

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.platform.LocalDensity

@Composable
fun CircularProgressWithCap(
    progress: Float,
    goalText: String = "Goal 2150 ml",
    currentTextNumber: String = "1000"
) {
    val strokeWidthDp = 23.dp
    val sizeDp = 260.dp
    val strokeWidthPx = with(LocalDensity.current) { strokeWidthDp.toPx() }
    val outfit = FontFamily(Font(R.font.outfit))

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

            // Progress arc (counter-clockwise: negative sweep)
            drawArc(
                brush = Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF2196F3))),
                startAngle = -90f,
                sweepAngle = -360f * progress,  // negative for counter-clockwise
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Calculate knob position aligned on stroke center
            val angle = Math.toRadians((-360 * progress - 90).toDouble())
            val knobRadius = (size.minDimension / 2) - (strokeWidthPx / 2f) + 18f
            val knobX = arcRect.center.x + knobRadius * cos(angle).toFloat()
            val knobY = arcRect.center.y + knobRadius * sin(angle).toFloat()
            val knobCenter = Offset(knobX, knobY)

            // Draw end knob: blue outer + white inner
            drawCircle(
                color = Color(0xFF64B5F6),
                radius = strokeWidthPx * 1.0f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = strokeWidthPx * 0.6f,
                center = knobCenter
            )


            // White inner circle
            drawCircle(
                color = Color.White,
                radius = strokeWidthPx * 0.6f,
                center = knobCenter
            )
        }

        // Center number
        Text(
            text = currentTextNumber,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = outfit
        )

        // Goal text
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 48.dp),  // spacing below number
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = goalText,
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
            Icon(
                painter = painterResource(id = R.drawable.water_drop),
                contentDescription = "Add Water",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(72.dp)
                    .clickable { /* handle click */ }
            )
        }
    }
}

