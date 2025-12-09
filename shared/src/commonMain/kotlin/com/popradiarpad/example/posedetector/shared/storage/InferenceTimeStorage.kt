package com.popradiarpad.example.posedetector.shared.storage

import kotlinx.coroutines.flow.StateFlow

// Should be a subclass in InferenceTimeStorage, but SKIE doesn't support it.
data class InferenceDataPoint(val inferenceTimeMs: Double, val timestampEpochMs: Long)

interface InferenceTimeStorage {
    val inferenceTimeMs: StateFlow<Double?>
    val dataPoints: StateFlow<List<InferenceDataPoint>>


    fun setInferenceTimeMs(value: Double)
}


