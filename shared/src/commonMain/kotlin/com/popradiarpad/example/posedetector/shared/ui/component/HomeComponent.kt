package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

class HomeComponent(
    componentContext: ComponentContext,
    private val onNavigate: () -> Unit
) : ComponentContext by componentContext {

    fun onStartPoseDetectionClick() {
        onNavigate()
    }
}
