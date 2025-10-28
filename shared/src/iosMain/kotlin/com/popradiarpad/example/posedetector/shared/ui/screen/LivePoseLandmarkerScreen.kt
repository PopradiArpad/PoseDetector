package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun LivePoseLandmarkerScreen(
    modifier: Modifier,
    onFinish: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("iOS Pose Landmarker Screen")
        Button(onClick = onFinish) {
            Text("Finish")
        }
    }
}
