package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LivePoseLandmarkerBackground(
    modifier: Modifier,
) {
    // This factory is now simpler as it doesn't need the onFinish callback
    val factory = {
        LivePoseLandmarkerScreenFactoryProvider.factory.create()// { /* The finish button is now in common code */ }
    }

    UIKitViewController(
        factory = factory,
        modifier = modifier,
    )
}
