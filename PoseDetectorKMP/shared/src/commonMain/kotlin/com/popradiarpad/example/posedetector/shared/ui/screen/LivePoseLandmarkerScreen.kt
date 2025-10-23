package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LivePoseLandmarkerScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
)
