package com.popradiarpad.example.posedetector.shared.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock.*
import kotlin.time.ExperimentalTime

// Singleton to be a data storage between platform specific modules and common UI.
object InferenceTimeStorage {
    data class DataPoint(val inferenceTimeMs: Double, val timestampEpochMs: Long)

    // Data read in common UI
    private val _inferenceTimeMs: MutableStateFlow<Double?> = MutableStateFlow(null)
    val inferenceTimeMs = _inferenceTimeMs.asStateFlow()

    private val dataPoints = ArrayDeque<DataPoint>()

    // Data is set in the platform specific modules
    @OptIn(ExperimentalTime::class)
    fun setInferenceTimeMs(value: Double) {
        _inferenceTimeMs.value = value

        val now = System.now().toEpochMilliseconds()
        dataPoints.addFirst(DataPoint(inferenceTimeMs = value, timestampEpochMs = now))

        val tenSecondsAgo = now - 10_000
        while (dataPoints.isNotEmpty() && dataPoints.last().timestampEpochMs < tenSecondsAgo) {
            dataPoints.removeLast()
        }
    }
}
