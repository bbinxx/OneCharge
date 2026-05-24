package com.bbinxx.onecharge.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 10.sp, color = textColor)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (data.isEmpty()) return@Canvas

        val maxVal = data.maxOfOrNull { it.second } ?: 10f
        val yMax = if (maxVal > 0) maxVal * 1.1f else 10f

        // Draw horizontal grid lines (4 levels)
        for (i in 0..3) {
            val y = paddingTop + chartHeight * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
            // Label
            val gridValStr = String.format("%.1fh", yMax * (3 - i) / 3f)
            val textLayoutResult = textMeasurer.measure(gridValStr, textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 5.dp.toPx(), y - textLayoutResult.size.height / 2f)
            )
        }

        // Draw bars
        val barCount = data.size
        val spacing = chartWidth / (barCount * 1.5f + 1)
        val barWidth = spacing * 1.2f

        data.forEachIndexed { index, pair ->
            val value = pair.second * animationProgress.value
            val barHeight = (value / yMax) * chartHeight
            val x = paddingLeft + spacing + index * (barWidth + spacing)
            val y = paddingTop + chartHeight - barHeight

            // Draw Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw X Axis Labels
            val xLabelLayout = textMeasurer.measure(pair.first, textStyle)
            drawText(
                textLayoutResult = xLabelLayout,
                topLeft = Offset(x + (barWidth - xLabelLayout.size.width) / 2f, paddingTop + chartHeight + 5.dp.toPx())
            )
        }
    }
}

@Composable
fun SotLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    // Capture surface color before Canvas lambda — cannot call @Composable inside Canvas
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 10.sp, color = textColor)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40.dp.toPx()
        val paddingRight = 20.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (data.isEmpty()) return@Canvas

        val maxVal = data.maxOrNull() ?: 10f
        val yMax = if (maxVal > 0) maxVal * 1.1f else 10f

        // Draw horizontal grid lines
        for (i in 0..3) {
            val y = paddingTop + chartHeight * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
            // Label
            val gridValStr = String.format("%.1fh", yMax * (3 - i) / 3f)
            val textLayoutResult = textMeasurer.measure(gridValStr, textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 5.dp.toPx(), y - textLayoutResult.size.height / 2f)
            )
        }

        // Draw Line Path
        val points = mutableListOf<Offset>()
        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

        data.forEachIndexed { index, valFloat ->
            val valAnimated = valFloat * animationProgress.value
            val x = paddingLeft + index * stepX
            val y = paddingTop + chartHeight - (valAnimated / yMax) * chartHeight
            points.add(Offset(x, y))

            // X Label
            if (index < labels.size) {
                val labelLayout = textMeasurer.measure(labels[index], textStyle)
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(x - labelLayout.size.width / 2f, paddingTop + chartHeight + 5.dp.toPx())
                )
            }
        }

        if (points.size > 1) {
            val strokePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val conX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val conY1 = pPrev.y
                    val conX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val conY2 = pCurr.y
                    cubicTo(conX1, conY1, conX2, conY2, pCurr.x, pCurr.y)
                }
            }

            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Area Fill beneath the line
            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, paddingTop + chartHeight)
                lineTo(points.first().x, paddingTop + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = points.minOf { it.y },
                    endY = paddingTop + chartHeight
                )
            )
        }

        // Draw points — use captured surfaceColor instead of calling @Composable MaterialTheme inside Canvas
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = surfaceColor,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun AppUsageBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.secondaryContainer
    val primaryBarColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 12.sp, color = textColor)
    val durationStyle = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val rowHeight = 36.dp.toPx()
        val spacing = 12.dp.toPx()

        if (data.isEmpty()) return@Canvas

        val maxVal = data.maxOfOrNull { it.second } ?: 1f

        data.forEachIndexed { index, pair ->
            val y = index * (rowHeight + spacing)
            val barMaxWidth = width - 120.dp.toPx()
            val animatedProgress = (pair.second / maxVal) * animationProgress.value
            val barWidth = barMaxWidth * animatedProgress

            // Draw label
            val labelLayout = textMeasurer.measure(pair.first, labelStyle)
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(0f, y + (rowHeight - labelLayout.size.height) / 2f)
            )

            // Draw full background bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(120.dp.toPx(), y + 4.dp.toPx()),
                size = Size(barMaxWidth, rowHeight - 8.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw progress bar
            drawRoundRect(
                color = primaryBarColor,
                topLeft = Offset(120.dp.toPx(), y + 4.dp.toPx()),
                size = Size(barWidth.coerceAtLeast(0f), rowHeight - 8.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw duration text
            val hours = pair.second.toInt() / 60
            val minutes = pair.second.toInt() % 60
            val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            val durationLayout = textMeasurer.measure(durationStr, durationStyle)
            drawText(
                textLayoutResult = durationLayout,
                topLeft = Offset(120.dp.toPx() + barWidth + 8.dp.toPx(), y + (rowHeight - durationLayout.size.height) / 2f)
            )
        }
    }
}
