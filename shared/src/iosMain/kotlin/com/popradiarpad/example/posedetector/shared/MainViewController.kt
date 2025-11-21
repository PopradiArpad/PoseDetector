package com.popradiarpad.example.posedetector.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent

fun MainViewController(rootComponent: RootComponent) = ComposeUIViewController {
    val root = remember {
        rootComponent
    }
    App(root)
}
