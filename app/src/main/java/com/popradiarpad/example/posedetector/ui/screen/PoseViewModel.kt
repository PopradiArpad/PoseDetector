package com.popradiarpad.example.posedetector.ui.screen

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
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

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null

    /**
     * We need a separate executor for CameraX binding, as it shouldn't run on the main thread.
     */
    private lateinit var cameraExecutor: ExecutorService

    fun initializeCamera(context: Context) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewModelScope.launch {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).awaitInternal()
                _cameraProviderReady.value = true
                Log.d("PoseViewModel", "CameraProvider initialized")
            } catch (e: Exception) {
                Log.e("PoseViewModel", "Error initializing CameraProvider: ${e.message}", e)
            }
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val cameraProvider = cameraProvider ?: run {
            Log.e("PoseViewModel", "CameraProvider not initialized yet.")
            return
        }

        if (!_cameraProviderReady.value) {
            Log.e("PoseViewModel", "CameraProvider not ready yet.")
            return
        }

        // Must re-create the Preview use case every time, as it can only be bound once.
        previewUseCase = Preview.Builder().build().also {
            it.surfaceProvider = surfaceProvider
        }

        try {
            // Unbind all previous use cases before binding again.
            cameraProvider.unbindAll()

            // Bind the Preview use case to the camera.
            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase
            )
            Log.d("PoseViewModel", "Camera bound to lifecycle")
        } catch (exc: Exception) {
            Log.e("PoseViewModel", "Use case binding failed", exc)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraProvider?.unbindAll() // Unbind use cases
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        Log.d("PoseViewModel", "ViewModel cleared and camera resources released.")
    }

    // Extension function to await ListenableFuture with coroutines
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun <T> ListenableFuture<T>.awaitInternal(): T =
        suspendCancellableCoroutine { continuation ->
            addListener({
                            try {
                                continuation.resume(get(), null)
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        }, cameraExecutor) // Using the ViewModel's cameraExecutor

            continuation.invokeOnCancellation {
                cancel(true)
            }
        }
}
