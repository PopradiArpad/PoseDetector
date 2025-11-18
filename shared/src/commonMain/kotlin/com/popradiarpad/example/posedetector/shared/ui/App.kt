package com.popradiarpad.example.posedetector.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerScreen
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun App(
    root: RootComponent,
    modifier: Modifier = Modifier
) {
    AppTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        // Decompose helper: UI of a Child Stack.
        Children(
            stack = root.childStack,
            modifier = modifier,
            animation = predictiveBackAnimation(
                backHandler = root.backHandler,
                fallbackAnimation = stackAnimation(fade() + scale()),
                onBack = root::onBackClicked,
            )
        ) {
            // App specific UIs of the children BLoCs.
            when (val child = it.instance) {
                is RootComponent.Child.Home -> HomeScreen(child.component)
                is RootComponent.Child.LivePoseLandmarker -> LivePoseLandmarkerScreen(child.component)
            }
        }
    }
}
