package com.popradiarpad.example.posedetector.shared.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun InferenceTimeChart(modifier: Modifier) {
    Box(
        modifier = modifier.background(
            color = Color.Red,
        )
    )
}