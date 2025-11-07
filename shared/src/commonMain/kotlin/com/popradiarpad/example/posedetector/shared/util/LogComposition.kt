package com.popradiarpad.example.posedetector.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.aakira.napier.Napier

@Composable
fun LogComposition(tag: String) {
    DisposableEffect(Unit) {
        Napier.d("$tag: Composed")
        onDispose {
            Napier.d("$tag: Disposed")
        }
    }
}
