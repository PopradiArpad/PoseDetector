package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

// A minimal component, will be extended when the screen get more feature.
class HomeComponent(
    componentContext: ComponentContext,
    val onStartPoseDetection: () -> Unit
) : ComponentContext by componentContext