package com.popradiarpad.example.posedetector.shared.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Singleton to be a data storage between platform specific modules and common UI.
// Singleton to be a data storage between platform specific
// data producers and common or platform-specific data consumers.
object RealInferenceTimeStorage : InferenceTimeStorage {

    private val _inferenceTimeMs = MutableStateFlow<Double?>(null)
    override val inferenceTimeMs = _inferenceTimeMs.asStateFlow()

    private val _dataPoints = MutableStateFlow<List<InferenceDataPoint>>(emptyList())
    override val dataPoints = _dataPoints.asStateFlow()

    private val history = ArrayDeque<InferenceDataPoint>()

    @OptIn(ExperimentalTime::class)
    override fun setInferenceTimeMs(value: Double) {
        _inferenceTimeMs.value = value
        val nowEpochMs = Clock.System.now().toEpochMilliseconds()

        history.addFirst(InferenceDataPoint(value, nowEpochMs))

        val tenSecondsAgoEpochMs = nowEpochMs - 10_000
        while (history.isNotEmpty() && history.last().timestampEpochMs < tenSecondsAgoEpochMs) {
            history.removeLast()
        }

        _dataPoints.value = history.toList()
    }
}