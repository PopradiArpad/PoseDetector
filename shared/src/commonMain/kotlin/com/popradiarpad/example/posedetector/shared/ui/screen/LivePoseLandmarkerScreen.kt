package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LivePoseLandmarkerScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LivePoseLandmarkerBackground(
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
expect fun LivePoseLandmarkerBackground(
    modifier: Modifier,
)
