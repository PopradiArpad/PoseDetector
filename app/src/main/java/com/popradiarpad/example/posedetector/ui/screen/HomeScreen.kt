package com.popradiarpad.example.posedetector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.popradiarpad.example.posedetector.ui.theme.PoseDetectorTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onStart: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Companion.Center) {
        Button(
                onClick = onStart, modifier = Modifier.Companion
            .fillMaxWidth(0.8f)
            .height(72.dp)
        ) {
            Text("Start Pose Detection", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    PoseDetectorTheme { HomeScreen { } }
}