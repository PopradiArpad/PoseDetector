package com.popradiarpad.example.posedetector.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle

fun MainViewController() = ComposeUIViewController {
    val root = remember {
        RootComponent(
            componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle())
        )
    }
    App(root)
}
