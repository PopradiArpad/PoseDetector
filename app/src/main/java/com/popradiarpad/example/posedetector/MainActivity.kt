package com.popradiarpad.example.posedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.popradiarpad.example.posedetector.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.ui.screen.PoseScreen
import com.popradiarpad.example.posedetector.ui.screen.PoseViewModel
import com.popradiarpad.example.posedetector.ui.theme.PoseDetectorTheme

class MainActivity : ComponentActivity() {
    private val poseViewModel: PoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val runningMode = RunningMode.LIVE_STREAM
        poseViewModel.initialize(this, runningMode)

        setContent {
            PoseDetectorTheme {
                var showPose by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    if (!showPose) {
                        HomeScreen(modifier = modifier) {
                            ensureCameraPermission { showPose = true }
                        }
                    } else {
                        PoseScreen(
                                modifier = modifier,
                                poseViewModel = poseViewModel,
                                runningMode = runningMode,
                                onFinish = { showPose = false }
                        )
                    }
                }
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { /* Optionally handle */ }

    private fun ensureCameraPermission(onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
