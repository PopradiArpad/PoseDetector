package com.popradiarpad.example.posedetector.shared.ui.theme

import androidx.compose.runtime.Composable

@Composable
expect fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
)
