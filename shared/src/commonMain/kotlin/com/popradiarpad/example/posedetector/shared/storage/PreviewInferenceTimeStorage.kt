package com.popradiarpad.example.posedetector.shared.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewInferenceTimeStorage(
    dataPoints: List<InferenceDataPoint> = emptyList()
) : InferenceTimeStorage {

    private val initialLatestMs = dataPoints.firstOrNull()?.inferenceTimeMs

    private val _inferenceTimeMs = MutableStateFlow(initialLatestMs)
    override val inferenceTimeMs = _inferenceTimeMs.asStateFlow()

    private val _dataPoints = MutableStateFlow(dataPoints)
    override val dataPoints = _dataPoints.asStateFlow()

    override fun setInferenceTimeMs(value: Double) {
        // Unneeded in preview.
    }
}
