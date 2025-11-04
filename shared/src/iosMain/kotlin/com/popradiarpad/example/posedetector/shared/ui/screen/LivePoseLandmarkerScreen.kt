package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LivePoseLandmarkerScreen(
    modifier: Modifier,
    onFinish: () -> Unit
) {
    UIKitViewController(
        factory = {
            LivePoseLandmarkerScreenFactoryProvider.factory.create(onFinish)
        },
        modifier = modifier.fillMaxSize(),
    )
}

