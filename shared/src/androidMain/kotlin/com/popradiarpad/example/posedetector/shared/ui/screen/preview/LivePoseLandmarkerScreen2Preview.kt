package com.popradiarpad.example.posedetector.shared.ui.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.arkivanov.decompose.DefaultComponentContext
import com.popradiarpad.example.posedetector.shared.ui.component.LivePoseLandmarkerComponent
import com.popradiarpad.example.posedetector.shared.ui.screen.LivePoseLandmarkerScreen2

@Preview
@Composable
fun LivePoseLandmarkerScreen2Preview() {
    // A fake ComponentContext is created for the preview
    val component = LivePoseLandmarkerComponent(
        componentContext = DefaultComponentContext(FakeLifecycleOwner().lifecycle),
        onNavigateBack = {}
    )
    LivePoseLandmarkerScreen2(component)
}

private class FakeLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}
