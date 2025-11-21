package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext

class InfoSheetComponent(
    componentContext: ComponentContext,
    private val onDismissClicked: () -> Unit
) : ComponentContext by componentContext {
    fun onDismiss() {
        onDismissClicked()
    }
}