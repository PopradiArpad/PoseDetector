package com.popradiarpad.example.posedetector.shared.ui.screen.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.popradiarpad.example.posedetector.shared.ui.screen.HomeContent
import com.popradiarpad.example.posedetector.shared.ui.theme.AppTheme

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
        HomeContent(onStartPoseDetection = {})
    }
}
