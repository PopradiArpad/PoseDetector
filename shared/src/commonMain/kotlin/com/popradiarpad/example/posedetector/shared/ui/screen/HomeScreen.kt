package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.popradiarpad.example.posedetector.shared.ui.component.HomeComponent

@Composable
fun HomeScreen(component: HomeComponent) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = component::onStartPoseDetectionClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(72.dp)
        ) {
            Text("Start Pose Detection", style = MaterialTheme.typography.titleLarge)
        }
    }
}
