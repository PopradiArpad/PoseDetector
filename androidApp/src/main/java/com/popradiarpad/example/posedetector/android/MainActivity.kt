package com.popradiarpad.example.posedetector.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent
import com.popradiarpad.example.posedetector.shared.util.initLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLogger()

        enableEdgeToEdge()

        val root = RootComponent(
            componentContext = DefaultComponentContext(lifecycle)
        )

        setContent {
            App(root)
        }
    }
}
