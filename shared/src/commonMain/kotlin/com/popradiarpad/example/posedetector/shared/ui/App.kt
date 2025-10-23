package com.popradiarpad.example.posedetector.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerScreen
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@Composable
fun App(ensureCameraPermission: (onGranted: () -> Unit) -> Unit) {
    AppTheme(
        darkTheme = isSystemInDarkTheme(),
        dynamicColor = true // This will be ignored on non-Android platforms
    ) {
        var showPoseScreen by remember { mutableStateOf(false) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            if (showPoseScreen) {
                LivePoseLandmarkerScreen(
                    modifier = modifier,
                    onFinish = { showPoseScreen = false })
            } else {
                HomeScreen(modifier = Modifier.fillMaxSize()) {
                    ensureCameraPermission {
                        showPoseScreen = true
                    }
                }
            }
        }
    }
}
