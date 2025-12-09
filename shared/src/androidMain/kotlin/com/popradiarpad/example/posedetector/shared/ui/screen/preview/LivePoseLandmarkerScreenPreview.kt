package com.popradiarpad.example.posedetector.shared.ui.screen.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.popradiarpad.example.posedetector.shared.storage.InferenceDataPoint
import com.popradiarpad.example.posedetector.shared.storage.LocalPreviewInferenceTimeStorage
import com.popradiarpad.example.posedetector.shared.storage.PreviewInferenceTimeStorage
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerContent
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@Preview
@Composable
fun LivePoseLandmarkerScreenButtonColumnPreview() {
    AppTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
        LivePoseLandmarkerContent(
            showInfoSheet = {},
            sheetComponentOnDismiss = null,
            onFinish = {}
        )
    }
}

@Preview
@Composable
fun LivePoseLandmarkerScreenInfoSheetPreview() {
    val previewStorage =
        PreviewInferenceTimeStorage(
            dataPoints = (0..30).map { i ->
                InferenceDataPoint(
                    inferenceTimeMs = 22.0 + (-15..30).random(),
                    timestampEpochMs = 1_700_000_000_000 + i * 333
                )
            }
        )

    AppTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
        CompositionLocalProvider(
            LocalPreviewInferenceTimeStorage provides previewStorage
        ) {
            LivePoseLandmarkerContent(
                showInfoSheet = {},
                sheetComponentOnDismiss = {},
                onFinish = {}
            )
        }
    }
}