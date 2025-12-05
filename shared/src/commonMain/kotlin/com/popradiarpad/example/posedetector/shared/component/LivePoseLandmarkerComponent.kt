package com.popradiarpad.example.posedetector.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class LivePoseLandmarkerComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext {
    // Info BottomSheet
    // ================
    private val infoSheetNavigation = SlotNavigation<InfoSheetConfig>()

    val childSlot: Value<ChildSlot<*, InfoSheetComponent>> =
        childSlot(
            source = infoSheetNavigation,
            serializer = InfoSheetConfig.serializer(), // Or null to disable navigation state saving
            handleBackButton = true, // Close the dialog on back button press
        ) { _, childComponentContext ->
            InfoSheetComponent(
                componentContext = childComponentContext,
                onDismissClicked = infoSheetNavigation::dismiss,
            )
        }

    fun showInfoSheet() {
        infoSheetNavigation.activate(InfoSheetConfig())
    }

    @Serializable
    private class InfoSheetConfig
}