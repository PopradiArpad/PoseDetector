package com.popradiarpad.example.posedetector.shared.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.popradiarpad.example.posedetector.shared.PoseLandmarkerHelper
import com.popradiarpad.example.posedetector.shared.ui.OverlayView
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
actual fun LivePoseLandmarkerBackground(
    modifier: Modifier,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    Log.d("LivePoseLandmarkerScreen", "hasPermission: $hasPermission")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        val poseViewModel: PoseViewModel = viewModel()
        poseViewModel.initializeIfNeeded(context)

        PoseScreen(
            modifier = modifier,
            poseViewModel = poseViewModel,
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to continue.")
        }
    }
}

@Composable
private fun PoseScreen(
    modifier: Modifier = Modifier,
    poseViewModel: PoseViewModel
) {
    val cameraReady by poseViewModel.cameraProviderReady.collectAsState()
    val resultBundle by poseViewModel.poseLandmarkerResultBundle.collectAsState()

    PoseScreenInternal(
        modifier = modifier,
        cameraReady = cameraReady,
        resultBundle = resultBundle,
        runningMode = poseViewModel.runningMode,
        onStartCameraRequest = { lifecycleOwner, surfaceProvider ->
            poseViewModel.startCamera(lifecycleOwner, surfaceProvider)
        },
    )
}

@Composable
private fun PoseScreenInternal(
    modifier: Modifier = Modifier,
    cameraReady: Boolean,
    resultBundle: PoseLandmarkerHelper.ResultBundle?,
    runningMode: RunningMode,
    onStartCameraRequest: (lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) -> Unit,
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

    if (!isComposePreview) {
        DisposableEffect(lifecycleOwner, previewView, cameraReady) {
            if (cameraReady) {
                onStartCameraRequest(lifecycleOwner, previewView.surfaceProvider)
            }
            onDispose {}
        }
    }

    val overlayView = remember { OverlayView(context) }

    LaunchedEffect(resultBundle, overlayView, runningMode) {
        resultBundle?.let { bundle ->
            if (bundle.results.isNotEmpty()) {
                overlayView.setResults(
                    bundle.results.first(),
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
                    .background(Color.Gray)
            ) {
                Text("Camera Preview Area", Modifier.align(Alignment.Center))
            }
        } else {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        AndroidView(factory = { overlayView }, modifier = Modifier.fillMaxSize())
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
    AppTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
        PoseScreenInternal(
            modifier = Modifier.fillMaxSize(),
            cameraReady = true,
            resultBundle = null,
            runningMode = RunningMode.LIVE_STREAM,
            onStartCameraRequest = { _, _ -> },
            isComposePreview = true
        )
    }
}
