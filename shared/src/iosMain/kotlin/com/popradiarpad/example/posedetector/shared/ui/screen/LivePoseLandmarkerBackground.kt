package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LivePoseLandmarkerBackground(
    modifier: Modifier,
) {
    UIKitViewController(
        factory = LivePoseLandmarkerBackgroundFactory.factory,
        modifier = modifier,
    )
}

object LivePoseLandmarkerBackgroundFactory {
    lateinit var factory: () -> UIViewController
}