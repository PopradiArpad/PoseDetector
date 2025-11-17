package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

// A minimal component, will be extended when the screen get more feature.
class LivePoseLandmarkerComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext