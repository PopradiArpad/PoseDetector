package com.popradiarpad.example.posedetector.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.util.initLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLogger()

        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}
