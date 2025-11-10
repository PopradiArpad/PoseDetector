package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

class LivePoseLandmarkerComponent(
    componentContext: ComponentContext,
    private val onNavigateBack: () -> Unit
) : ComponentContext by componentContext {

    fun onBackClick() {
        onNavigateBack()
    }
}
