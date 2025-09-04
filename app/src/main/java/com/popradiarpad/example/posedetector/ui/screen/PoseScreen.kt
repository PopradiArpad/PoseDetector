package com.popradiarpad.example.posedetector.ui.screen

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.popradiarpad.example.posedetector.PoseLandmarkerHelper
import com.popradiarpad.example.posedetector.ui.OverlayView
import com.popradiarpad.example.posedetector.ui.theme.PoseDetectorTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun PoseScreen(
        modifier: Modifier = Modifier,
        poseViewModel: PoseViewModel, // Still takes the ViewModel
        runningMode: RunningMode,
        onFinish: () -> Unit
) {
    val cameraReady by poseViewModel.cameraProviderReady.collectAsState()
    val resultBundle by poseViewModel.poseLandmarkerResultBundle.collectAsState()

    PoseScreenInternal(
            modifier = modifier,
            cameraReady = cameraReady,
            resultBundle = resultBundle,
            runningMode = runningMode,
            onStartCameraRequest = { lifecycleOwner, surfaceProvider ->
                poseViewModel.startCamera(lifecycleOwner, surfaceProvider)
            },
            onFinish = onFinish
    )
}

@Composable
fun PoseScreenInternal(
        modifier: Modifier = Modifier,
        cameraReady: Boolean,
        resultBundle: PoseLandmarkerHelper.ResultBundle?,
        runningMode: RunningMode,
        onStartCameraRequest: (lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) -> Unit,
        onFinish: () -> Unit,
        isComposePreview: Boolean = false // Flag to handle AndroidView differently in preview
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Only set up camera interaction if not in Compose preview mode,
    // or if cameraReady is true (which it might be from a fake in a preview)
    if (!isComposePreview) {
        DisposableEffect(lifecycleOwner, previewView, cameraReady) {
            if (cameraReady) {
                onStartCameraRequest(lifecycleOwner, previewView.surfaceProvider)
            }
            onDispose {
                // ViewModel handles camera resource cleanup.
                // Or, if PoseScreenInternal needs to signal something, add a lambda.
            }
        }
    }

    val overlayView = remember { OverlayView(context) }

    LaunchedEffect(resultBundle, overlayView, runningMode) {
        resultBundle?.let { bundle ->
            if (bundle.results.isNotEmpty()) {
                overlayView.setResults(
                        bundle.results.first(), // Assuming live stream, so one result per bundle
                        bundle.inputImageHeight,
                        bundle.inputImageWidth,
                        runningMode
                )
            } else {
                overlayView.clear()
            }
        } ?: run {
            overlayView.clear()
        }
    }

    Box(modifier = modifier) {
        if (isComposePreview) {
            Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray) // Placeholder for PreviewView
            ) {
                Text("Camera Preview Area", Modifier.align(Alignment.Center))
            }
        } else {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        AndroidView(factory = { overlayView }, modifier = Modifier.fillMaxSize())

        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .padding(16.dp)
                        .height(56.dp)
            ) { Text("Finish") }
        }
    }
}

@ComposePreview(
        name = "PoseScreenInternal - Portrait", showBackground = true,
        device = "spec:width=360dp,height=640dp"
)
@ComposePreview(
        name = "PoseScreenInternal - Landscape", showBackground = true,
        device = "spec:width=640dp,height=360dp"
)
@Composable
private fun PoseScreenInternalPreview() {
    PoseDetectorTheme {
        PoseScreenInternal(
                modifier = Modifier.fillMaxSize(),
                cameraReady = true,
                resultBundle = null,
                runningMode = RunningMode.LIVE_STREAM,
                onStartCameraRequest = { _, _ -> },
                onFinish = { },
                isComposePreview = true
        )
    }
}
