package com.popradiarpad.example.posedetector.shared.ui.screen.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
    AppTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
        LivePoseLandmarkerContent(
            showInfoSheet = {},
            sheetComponentOnDismiss = {},
            onFinish = {}
        )
    }
}
