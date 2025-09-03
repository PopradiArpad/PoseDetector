package com.popradiarpad.example.posedetector.ui.screen

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.popradiarpad.example.posedetector.PoseLandmarkerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resumeWithException


class PoseViewModel : ViewModel() {

    private val _cameraProviderReady = MutableStateFlow(false)
    val cameraProviderReady: StateFlow<Boolean> = _cameraProviderReady

    private val _poseLandmarkerResultBundle =
        MutableStateFlow<PoseLandmarkerHelper.ResultBundle?>(null)
    val poseLandmarkerResultBundle: StateFlow<PoseLandmarkerHelper.ResultBundle?> =
        _poseLandmarkerResultBundle

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var imageAnalysisUseCase: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService
    private var poseLandmarkerHelper: PoseLandmarkerHelper? = null
    private var currentRunningMode: RunningMode? = null // Store the running mode

    private val landmarkerListener = object : PoseLandmarkerHelper.LandmarkerListener {
        override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
            _poseLandmarkerResultBundle.value = resultBundle
        }

        override fun onError(error: String, errorCode: Int) {
            Log.e("PoseViewModel", "PoseLandmarkerHelper Error: $error (Code: $errorCode)")
            _poseLandmarkerResultBundle.value = null
        }
    }

    // Modified initialize to accept MediaPipeRunningMode
    fun initialize(context: Context, runningMode: RunningMode) {
        this.currentRunningMode = runningMode // Store it
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewModelScope.launch {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).awaitInternal()
                _cameraProviderReady.value = true
                Log.d("PoseViewModel", "CameraProvider initialized")
                initializePoseLandmarker(
                        context.applicationContext, runningMode
                ) // Pass it to helper init
            } catch (e: Exception) {
                Log.e("PoseViewModel", "Error initializing ViewModel: ${e.message}", e)
            }
        }
    }

    // Modified to accept and use MediaPipeRunningMode for PoseLandmarkerHelper
    private fun initializePoseLandmarker(appContext: Context, runningMode: RunningMode) {
        poseLandmarkerHelper = PoseLandmarkerHelper(
                context = appContext,
                // This now assumes PoseLandmarkerHelper's constructor or a builder
                // can accept com.google.mediapipe.tasks.vision.core.RunningMode
                runningMode = runningMode,
                poseLandmarkerHelperListener = landmarkerListener
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                poseLandmarkerHelper?.setupPoseLandmarker()
                Log.d("PoseViewModel", "PoseLandmarkerHelper setup complete.")
            } catch (e: Exception) {
                Log.e("PoseViewModel", "Error setting up PoseLandmarkerHelper: ${e.message}", e)
            }
        }
    }


    fun startCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val currentCameraProvider = cameraProvider ?: run {
            Log.e("PoseViewModel", "CameraProvider not initialized yet.")
            return
        }
        if (poseLandmarkerHelper == null) {
            Log.e(
                    "PoseViewModel",
                    "PoseLandmarkerHelper not initialized yet. Mode: $currentRunningMode"
            )
            // If helper setup failed, this could be null.
            if (currentRunningMode != null) {
                // Retry initialization if it failed, or handle error
                // For now, just log and exit.
                Log.e(
                        "PoseViewModel",
                        "Attempting to re-initialize landmarker due to being null at startCamera."
                )
                // initializePoseLandmarker(context, currentRunningMode!!) // Careful with context here
            }
            return
        }
        if (!_cameraProviderReady.value) {
            Log.e("PoseViewModel", "CameraProvider not ready yet.")
            return
        }

        previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        imageAnalysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    poseLandmarkerHelper?.detectLiveStream(imageProxy)
                }
            }

        try {
            currentCameraProvider.unbindAll()
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(previewUseCase!!)
                .addUseCase(imageAnalysisUseCase!!)
                .build()
            currentCameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    useCaseGroup
            )
            Log.d("PoseViewModel", "Camera bound to lifecycle with Preview and ImageAnalysis")
        } catch (exc: Exception) {
            Log.e("PoseViewModel", "Use case binding failed", exc)
            _poseLandmarkerResultBundle.value = null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun <T> ListenableFuture<T>.awaitInternal(): T =
        suspendCancellableCoroutine { continuation ->
            addListener({
                            try {
                                continuation.resume(get(), null)
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        }, cameraExecutor)

            continuation.invokeOnCancellation {
                cancel(true)
            }
        }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            poseLandmarkerHelper?.clearPoseLandmarker()
        }
        cameraProvider?.unbindAll()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        Log.d("PoseViewModel", "ViewModel cleared and resources released.")
    }
}
