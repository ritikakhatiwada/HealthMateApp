package com.example.healthmate.presentation.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Weekly appointment bar chart component.
 *
 * Displays appointment counts for the last 7 days in a simple bar chart format.
 *
 * @param data List of daily appointment counts
 * @param modifier Modifier for the chart container
 */
@Composable
fun WeeklyAppointmentChart(
    data: List<DayAppointmentCount>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val maxCount = (data.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "chart_animation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (data.isEmpty()) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = (size.width - (data.size - 1) * 8.dp.toPx()) / data.size
                val maxBarHeight = size.height - 20.dp.toPx()

                data.forEachIndexed { index, dayData ->
                    val barHeight = if (maxCount > 0) {
                        (dayData.count.toFloat() / maxCount) * maxBarHeight * animatedProgress
                    } else {
                        0f
                    }

                    val x = index * (barWidth + 8.dp.toPx())
                    val y = size.height - barHeight - 20.dp.toPx()

                    // Draw bar
                    drawRoundRect(
                        color = if (dayData.count > 0) primaryColor else surfaceVariant,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }
        }

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { dayData ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dayData.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dayData.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Simple stat card with trend indicator.
 *
 * @param title Title of the stat
 * @param value Current value
 * @param trend Trend text (e.g., "+5%")
 * @param isPositive Whether the trend is positive
 * @param modifier Modifier for the card
 */
@Composable
fun StatCardWithTrend(
    title: String,
    value: String,
    trend: String? = null,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (trend != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositive) Color(0xFF22C55E) else Color(0xFFEF4444)
            )
        }
    }
}

/**
 * Horizontal progress bar for utilization display.
 *
 * @param label Label for the progress bar
 * @param progress Progress value (0.0 to 1.0)
 * @param value Display value
 * @param color Bar color
 * @param modifier Modifier for the component
 */
@Composable
fun UtilizationBar(
    label: String,
    progress: Float,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "utilization_animation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            // Background
            drawRoundRect(
                color = backgroundColor,
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            // Progress
            drawRoundRect(
                color = color,
                size = Size(size.width * animatedProgress, size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
