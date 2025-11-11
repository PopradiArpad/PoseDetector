package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

class HomeComponent(
    componentContext: ComponentContext,
    val onStartPoseDetection: () -> Unit
) : ComponentContext by componentContext