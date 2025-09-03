package com.popradiarpad.example.posedetector.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory // Keep for ImageProxy.toBitmap if still used by PoseLandmarkerHelper later
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat // Keep for ImageProxy.toBitmap
import android.graphics.Paint
import android.graphics.Rect // Keep for ImageProxy.toBitmap
import android.graphics.YuvImage // Keep for ImageProxy.toBitmap
import android.view.View
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage // Keep for ImageProxy.toBitmap
import androidx.camera.core.ImageProxy // Keep for ImageProxy.toBitmap and PoseLandmarkerHelper
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
import androidx.compose.runtime.collectAsState // Added import
import androidx.compose.runtime.getValue // Added import
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder // Keep for PoseLandmarkerHelper
import com.google.mediapipe.framework.image.MPImage // Keep for PoseLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker // Keep for PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.popradiarpad.example.posedetector.PoseLandmarkerHelper
import com.popradiarpad.example.posedetector.ui.OverlayView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream // Keep for ImageProxy.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


@Composable
fun PoseScreen(
    modifier: Modifier = Modifier,
    poseViewModel: PoseViewModel, // Added ViewModel parameter
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // For PoseLandmarkerHelper callbacks

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            // Consider scaleType for proper video aspect ratio, e.g.:
            // scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Observe camera readiness state from ViewModel
    val cameraReady by poseViewModel.cameraProviderReady.collectAsState()

    // Effect to start the camera when ready and lifecycle is appropriate
    DisposableEffect(lifecycleOwner, previewView, cameraReady) {
        if (cameraReady) {
            poseViewModel.startCamera(lifecycleOwner, previewView.surfaceProvider)
        }
        onDispose {
            // ViewModel's onCleared and the unbindAll() in startCamera
            // should handle resource cleanup tied to the camera.
            // If PoseViewModel needs an explicit "stopCamera" for this screen, add it.
        }
    }

    // Keep PoseLandmarkerHelper and OverlayView setup for now.
    // This will need further integration with ImageAnalysis from the ViewModel.
    val overlayView = remember { OverlayView(context) }
    val poseLandmarkerHelperListener = remember(scope, overlayView, context) {
        object : PoseLandmarkerHelper.LandmarkerListener {
            override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
                scope.launch {
                    overlayView.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM // This indicates it expects live frames
                    )
                    overlayView.invalidate()
                }
            }

            override fun onError(error: String, errorCode: Int) {
                scope.launch {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DisposableEffect(context, poseLandmarkerHelperListener) {
        val poseLandmarkerHelper = PoseLandmarkerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM, // Important for next steps
            poseLandmarkerHelperListener = poseLandmarkerHelperListener
        )
        // Launch the setup on a background thread
        scope.launch(Dispatchers.IO) { // Changed to IO dispatcher for model loading
            poseLandmarkerHelper.setupPoseLandmarker()
        }
        onDispose {
            scope.launch(Dispatchers.IO) { // Also clear on IO dispatcher
                 poseLandmarkerHelper.clearPoseLandmarker()
            }
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

private class PoseOverlayView(context: Context) : View(context) {
    private val pointPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 8f
        isAntiAlias = true
    }
    private val linePaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private var result: PoseLandmarkerResult? = null
    private var imgWidth: Int = 1
    private var imgHeight: Int = 1
    private var rotation: Int = 0

    fun onPoseResult(
            result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int, rotationDegrees: Int
    ) {
        this.result = result
        this.imgWidth = imageWidth
        this.imgHeight = imageHeight
        this.rotation = rotationDegrees
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val res = result ?: return
        val poses = res.landmarks()
        if (poses.isEmpty()) return
        val points = poses[0]

        fun mapPoint(lm: NormalizedLandmark): Pair<Float, Float> {
            // Landmarks are normalized [0,1], map to view size, accounting for rotation if needed
            val x = lm.x() * width
            val y = lm.y() * height
            return x to y
        }

        // Draw connections (simple subset for minimal example)
        val indices = listOf(
                11 to 13, 13 to 15, // left arm
                12 to 14, 14 to 16, // right arm
                11 to 12, // shoulders
                23 to 24, // hips
                11 to 23, 12 to 24 // torso
        )
        for ((a, b) in indices) {
            if (a < points.size && b < points.size) {
                val (x1, y1) = mapPoint(points[a])
                val (x2, y2) = mapPoint(points[b])
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
        }
        // Draw all keypoints
        for (p in points) {
            val (x, y) = mapPoint(p)
            canvas.drawCircle(x, y, 6f, pointPaint)
        }
    }
}

// Process ImageProxy
// ==================
private fun processImageProxy(
        landmarker: PoseLandmarker,
        imageProxy: ImageProxy,
        onResult: (PoseLandmarkerResult, Int, Int, Int) -> Unit
) {
    try {
        val bitmap = imageProxy.toBitmap() ?: run { imageProxy.close(); return }
        val mpImage: MPImage = BitmapImageBuilder(bitmap).build()
        val result = landmarker.detect(mpImage)
        onResult(result, bitmap.width, bitmap.height, imageProxy.imageInfo.rotationDegrees)
    } catch (_: Throwable) {
        // ignore frame errors
    } finally {
        imageProxy.close()
    }
}

@ExperimentalGetImage
private fun ImageProxy.toBitmap(): Bitmap? {
    val image = this.image ?: return null
    // Convert YUV_420_888 to NV21
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            this.width,
            this.height,
            null
    )
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 80, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

// Build Pose Landmarker
// =====================
private fun buildPoseLandmarker(context: Context, filesDir: File): PoseLandmarker {
    val modelFile = File(filesDir, "pose_landmarker_lite.task")
    if (!modelFile.exists()) {
        downloadModel(modelFile)
    }
    val baseOptions = BaseOptions.builder()
        .setModelAssetPath(modelFile.absolutePath) // Ensure this is an absolute path
        .setDelegate(Delegate.GPU)
        .build()
    val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
        .setBaseOptions(baseOptions)
        .setRunningMode(RunningMode.IMAGE)
        .setNumPoses(1)
        .build()
    return PoseLandmarker.createFromOptions(context, optionsBuilder)
}

private fun downloadModel(outFile: File) {
    // Lightweight model from MediaPipe (public GitHub raw). You can replace with a local asset if desired.
    val url =
        URL("https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/${outFile.name}")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 15000
        readTimeout = 15000
    }
    connection.inputStream.use { input ->
        FileOutputStream(outFile).use { output ->
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }
}
