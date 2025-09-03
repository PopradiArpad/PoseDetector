package com.popradiarpad.example.posedetector.ui.screen

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.popradiarpad.example.posedetector.ui.OverlayView


@Composable
fun PoseScreen(
        modifier: Modifier = Modifier,
        poseViewModel: PoseViewModel,
        runningMode: RunningMode,
        onFinish: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            // scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val cameraReady by poseViewModel.cameraProviderReady.collectAsState()

    DisposableEffect(lifecycleOwner, previewView, cameraReady) {
        if (cameraReady) {
            poseViewModel.startCamera(lifecycleOwner, previewView.surfaceProvider)
        }
        onDispose {
            // ViewModel handles camera resource cleanup.
        }
    }

    val overlayView = remember { OverlayView(context) }
    val resultBundle by poseViewModel.poseLandmarkerResultBundle.collectAsState()

    LaunchedEffect(resultBundle, overlayView, runningMode) { // Added runningMode as a key
        resultBundle?.let { bundle ->
            if (bundle.results.isNotEmpty()) {
                overlayView.setResults(
                        bundle.results.first(),
                        bundle.inputImageHeight,
                        bundle.inputImageWidth,
                        runningMode // Use the passed-in runningMode
                )
            } else {
                overlayView.clear() // Clear if bundle has no results
            }
        } ?: run {
            overlayView.clear() // Clear if bundle is null
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        AndroidView(factory = { overlayView }, modifier = Modifier.fillMaxSize())
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
            ) { Text("Finish") }
        }
    }
}
