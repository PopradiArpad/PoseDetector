package com.popradiarpad.example.posedetector.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.screen.PoseViewModel

class MainActivity : ComponentActivity() {
    private val poseViewModel: PoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        poseViewModel.initialize(this)

        setContent {
            App(ensureCameraPermission = ::ensureCameraPermission)
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
                // In a real app, you'd show a UI explaining why the permission is needed.
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
