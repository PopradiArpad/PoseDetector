package com.popradiarpad.example.posedetector.shared.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Singleton to be a data storage between platform specific modules and common UI.
// Singleton to be a data storage between platform specific
// data producers and common or platform-specific data consumers.
object InferenceTimeStorage {
    data class DataPoint(val inferenceTimeMs: Double, val timestampEpochMs: Long)

    // Data read in common UI
    // ======================
    private val _inferenceTimeMs: MutableStateFlow<Double?> = MutableStateFlow(null)
    val inferenceTimeMs = _inferenceTimeMs.asStateFlow()

    // Data read in platform specific UI
    // =================================
    private val _dataPoints = MutableStateFlow<List<DataPoint>>(emptyList())
    val dataPoints = _dataPoints.asStateFlow()

    private val dataPointsList = ArrayDeque<DataPoint>()

    // Data is set in the platform specific modules
    @OptIn(ExperimentalTime::class)
    fun setInferenceTimeMs(value: Double) {
        _inferenceTimeMs.value = value

        val now = Clock.System.now().toEpochMilliseconds()
        dataPointsList.addFirst(DataPoint(inferenceTimeMs = value, timestampEpochMs = now))

        val tenSecondsAgo = now - 10_000
        while (dataPointsList.isNotEmpty() && dataPointsList.last().timestampEpochMs < tenSecondsAgo) {
            dataPointsList.removeLast()
        }
        
        _dataPoints.value = dataPointsList.toList()
    }
}
