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

// We want integrate a SwiftUI component into Composable and
// iosMain things are not visible in shared (only the other way around)
// so we need storage which is
// 1. visible from both world (so it must be defined in shared, here)
// 2. can be set from iosApp
// 3. can be read from shared.
object LivePoseLandmarkerBackgroundFactory {
    lateinit var factory: () -> UIViewController
}