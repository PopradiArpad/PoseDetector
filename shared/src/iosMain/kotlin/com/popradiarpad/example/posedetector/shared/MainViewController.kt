package com.popradiarpad.example.posedetector.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.popradiarpad.example.posedetector.shared.lifecycle.LifecycleOwner
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent

fun MainViewController() = ComposeUIViewController {
    val lifecycle = remember { LifecycleOwner() }
    val root = remember {
        RootComponent(
            componentContext = DefaultComponentContext(lifecycle.lifecycle)
        )
    }
    App(root)
}
