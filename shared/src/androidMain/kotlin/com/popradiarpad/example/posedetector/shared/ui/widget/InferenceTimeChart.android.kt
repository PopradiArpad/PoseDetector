package com.popradiarpad.example.posedetector.shared.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.popradiarpad.example.posedetector.shared.storage.InferenceDataPoint
import com.popradiarpad.example.posedetector.shared.storage.InferenceTimeStorage
import com.popradiarpad.example.posedetector.shared.storage.RealInferenceTimeStorage

@Composable
actual fun InferenceTimeChart(modifier: Modifier) {
    InferenceTimeChartContent(RealInferenceTimeStorage, modifier)
}

@Composable
fun InferenceTimeChartContent(
    storage: InferenceTimeStorage,
    modifier: Modifier = Modifier
) {
    // The latest window of points (last ~10 seconds)
    var points by remember { mutableStateOf(emptyList<InferenceDataPoint>()) }

    // Subscribe to the shared Flow
    LaunchedEffect(storage) {
        storage.dataPoints.collect { newPoints ->
            points = newPoints
        }
    }

    Column(modifier = modifier) {
        val surfaceColor = MaterialTheme.colorScheme.surface

        Text(
            text = "Inference Time (last 10s)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(vertical = 12.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data yet", color = Color.Gray)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val gridColor = Color.Gray.copy(alpha = 0.3f)

                    // Horizontal grid lines
                    repeat(5) { i ->
                        val y = canvasHeight * i / 4f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Vertical grid lines
                    repeat(6) { i ->
                        val x = canvasWidth * i / 5f
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 1f
                        )
                    }

                    // Data line
                    val times = points.map { it.inferenceTimeMs.toFloat() }
                    val minTime = times.minOrNull() ?: 0f
                    val maxTime = maxOf(times.maxOrNull() ?: 100f, minTime + 1f)
                    val timeRange = maxTime - minTime

                    val path = Path().apply {
                        points.forEachIndexed { index, point ->
                            val x = canvasWidth * index / (points.size - 1).coerceAtLeast(1)
                            val normalized = (point.inferenceTimeMs.toFloat() - minTime) / timeRange
                            val y = canvasHeight * (1f - normalized)
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color.Blue,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

