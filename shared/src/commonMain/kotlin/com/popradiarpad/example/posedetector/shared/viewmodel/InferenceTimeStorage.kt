package com.popradiarpad.example.posedetector.shared.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Singleton to be a data storage between platform specific modules and common UI.
object InferenceTimeStorage {
    // Data read in common UI
    private val _inferenceTimeMs: MutableStateFlow<Double?> = MutableStateFlow(null)
    val inferenceTimeMs = _inferenceTimeMs.asStateFlow()

    // Data is set in the platform specific modules
    fun setInferenceTimeMs(value: Double) {
        _inferenceTimeMs.value = value
    }

}
π