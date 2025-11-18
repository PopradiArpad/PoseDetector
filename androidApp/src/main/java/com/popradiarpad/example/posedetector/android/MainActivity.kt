package com.popradiarpad.example.posedetector.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent
import com.popradiarpad.example.posedetector.shared.util.initLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLogger()

        enableEdgeToEdge()

        // The root BLoC containing all other ones.
        val root = RootComponent(
            // This component context wires up everything
            // Decompose needs from the Activity:
            // ◦ Lifecycle
            // ◦ SavedStateRegistry (for saving state across process death)
            // ◦ ViewModelStore (for retaining component instances across configuration changes)
            // ◦ OnBackPressedDispatcher (for handling back-press events)
            componentContext = defaultComponentContext()
        )

        setContent {
            // The main UI.
            App(root, modifier = Modifier.fillMaxSize())
        }
    }
}
