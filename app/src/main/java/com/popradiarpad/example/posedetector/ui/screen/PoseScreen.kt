package com.popradiarpad.example.posedetector.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

@Composable
fun PoseScreen(modifier: Modifier = Modifier, onFinish: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }
    val overlayView = remember { PoseOverlayView(context) }

    var landmarker by remember { mutableStateOf<PoseLandmarker?>(null) }

    // Download model and init landmarker once.
    LaunchedEffect(Unit) {
        landmarker = withContext(Dispatchers.IO) { buildPoseLandmarker(context, context.filesDir) }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val analysisExecutor = Executors.newSingleThreadExecutor()

        var bound = false

        fun bindCamera() {
            val provider = cameraProviderFuture.get()
            provider.unbindAll()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val lm = landmarker
                if (lm != null) {
                    processImageProxy(
                            lm, imageProxy
                    ) { result, imageWidth, imageHeight, rotationDegrees ->
                        overlayView.onPoseResult(result, imageWidth, imageHeight, rotationDegrees)
                    }
                } else {
                    imageProxy.close()
                }
            }

            try {
                provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                )
                bound = true
            } catch (_: Exception) {
            }
        }

        val listener = Runnable { bindCamera() }
        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            try {
                val provider = cameraProviderFuture.get()
                if (bound) provider.unbindAll()
            } catch (_: Exception) {
            }
            analysisExecutor.shutdown()
            landmarker?.close()
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.Companion.fillMaxSize())
        AndroidView(factory = { overlayView }, modifier = Modifier.Companion.fillMaxSize())
        Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.BottomCenter
        ) {
            Button(
                    onClick = onFinish,
                    modifier = Modifier.Companion
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
            ) { Text("Finish") }
        }
    }
}

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

private fun buildPoseLandmarker(context: Context, filesDir: File): PoseLandmarker {
    val modelFile = File(filesDir, "pose_landmarker_lite.task")
    if (!modelFile.exists()) {
        downloadModel(modelFile)
    }
    val baseOptions = BaseOptions.builder()
        .setModelAssetPath(modelFile.absolutePath)
        .build()
    val options = PoseLandmarker.PoseLandmarkerOptions.builder()
        .setBaseOptions(baseOptions)
        .setRunningMode(RunningMode.IMAGE)
        .setNumPoses(1)
        .build()
    return PoseLandmarker.createFromOptions(context, options)
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