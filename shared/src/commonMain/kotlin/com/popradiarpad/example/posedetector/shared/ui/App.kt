package com.popradiarpad.example.posedetector.shared.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeScreen2
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerScreen2
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@Composable
fun App(root: RootComponent) {
    AppTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        Children(
            stack = root.childStack,
            animation = stackAnimation(slide())
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.Home -> HomeScreen2(child.component)
                is RootComponent.Child.LivePoseLandmarker -> LivePoseLandmarkerScreen2(child.component)
            }
        }
    }
}
