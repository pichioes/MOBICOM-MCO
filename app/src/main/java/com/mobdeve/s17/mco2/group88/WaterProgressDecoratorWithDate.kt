package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WaterProgressDecoratorWithDate(
    private val context: Context,
    private val waterRecords: List<WaterRecord>,
    private val displayYear: Int = LocalDate.now().year,
    private val displayMonth: Int = LocalDate.now().monthValue,
    private val goalAmount: Int = 2150
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true // Apply to all days
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(WaterProgressSpanWithDate(context, waterRecords, displayYear, displayMonth, goalAmount))
    }
}

class WaterProgressSpanWithDate(
    private val context: Context,
    private val waterRecords: List<WaterRecord>,
    private val displayYear: Int,
    private val displayMonth: Int,
    private val goalAmount: Int = 2150
) : LineBackgroundSpan {

    private val backgroundPaint = Paint().apply {
        color = Color.DKGRAY
        alpha = 50 // 30% transparency
        style = Paint.Style.STROKE
        strokeWidth = 7f
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = Color.parseColor("#64B5F6")
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        try {
            // Calculate progress for this date
            val progress = getProgressForDate(text.toString())

            // Calculate circle dimensions - smaller to fit within day cell
            val centerX = (left + right) / 2f
            val centerY = (top + bottom) / 2f
            val radius = minOf((right - left), (bottom - top)) / 1f - 0f

            // Only draw if radius is positive
            if (radius > 0) {
                val rectF = RectF(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                )

                // Draw background circle
                canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

                // Draw progress arc if there's any progress
                if (progress > 0f) {
                    val sweepAngle = 360f * progress
                    canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
                }
            }
        } catch (e: Exception) {
            // Handle any drawing errors silently
        }
    }

    private fun getProgressForDate(dayText: String): Float {
        return try {
            val day = dayText.toIntOrNull() ?: return 0f

            // Create the date string for this specific day
            val dateString = LocalDate.of(displayYear, displayMonth, day)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // Find matching water records for this date
            val currentIntake = waterRecords
                .filter { it.time == dateString }
                .sumOf { it.amount?.toDouble() ?: 0.0 }
                .toInt()

            // Calculate and return progress (0.0 to 1.0)
            (currentIntake.toFloat() / goalAmount).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0f
        }
    }
}