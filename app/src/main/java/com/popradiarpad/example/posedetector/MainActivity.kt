package com.popradiarpad.example.posedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.popradiarpad.example.posedetector.ui.screen.HomeScreen
import com.popradiarpad.example.posedetector.ui.screen.PoseScreen
import com.popradiarpad.example.posedetector.ui.theme.PoseDetectorTheme

class MainActivity : ComponentActivity() {
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoseDetectorTheme {
                var showPose by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!showPose) {
                        HomeScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                        ) { ensureCameraPermission { showPose = true } }
                    } else {
                        PoseScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                        ) { showPose = false }
                    }
                }
            }
        }
    }

    private fun ensureCameraPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}


