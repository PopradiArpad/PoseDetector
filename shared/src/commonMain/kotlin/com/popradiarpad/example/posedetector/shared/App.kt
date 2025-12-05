package com.popradiarpad.example.posedetector.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.popradiarpad.example.posedetector.shared.ui.animation.backAnimation
import com.popradiarpad.example.posedetector.shared.component.RootComponent
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerScreen
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun App(
    rootComponent: RootComponent,
    modifier: Modifier = Modifier
) {
    AppTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        // Decompose helper: UI of a Child Stack.
        Children(
            stack = rootComponent.childStack,
            modifier = modifier,
            animation = backAnimation(
                backHandler = rootComponent.backHandler,
                onBack = rootComponent::onBackClicked,
            ),
        ) {
            // App specific UIs of the children BLoCs.
            when (val child = it.instance) {
                is RootComponent.Child.Home -> HomeScreen(child.component)
                is RootComponent.Child.LivePoseLandmarker -> LivePoseLandmarkerScreen(child.component)
            }
        }
    }
}
