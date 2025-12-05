package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import platform.UIKit.UIViewController

@Composable
actual fun LivePoseLandmarkerBackground(
    modifier: Modifier,
) {
    UIKitViewController(
        factory = LivePoseLandmarkerBackgroundFactory.factory,
        modifier = modifier,
    )
}

// We want to integrate a SwiftUI component (of iosApp)
// into the Composable above defined here in shared/iosMain,
// but iosApp things are not visible here (shared/iosMain),
// only the other way around.
// So we need a storage which is
// 1. visible from both world (so it must be defined in shared, here)
// 2. can be set from iosApp
// 3. can be read from shared.
object LivePoseLandmarkerBackgroundFactory {
    lateinit var factory: () -> UIViewController
}