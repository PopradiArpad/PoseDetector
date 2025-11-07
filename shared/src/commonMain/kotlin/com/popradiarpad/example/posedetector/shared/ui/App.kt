package com.popradiarpad.example.posedetector.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme
import com.popradiarpad.example.posedetector.shared.util.LogComposition

@Composable
fun App() {
    AppTheme(
        darkTheme = isSystemInDarkTheme(),
        dynamicColor = true // This will be ignored on non-Android platforms
    ) {
        LogComposition(tag = "App")

        Navigator(screen = HomeScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
