package com.popradiarpad.example.posedetector.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.popradiarpad.example.posedetector.shared.ui.App
import com.popradiarpad.example.posedetector.shared.ui.component.RootComponent

@OptIn(ExperimentalDecomposeApi::class)
fun MainViewController(rootComponent: RootComponent) =
    ComposeUIViewController {
        // This is needed on all non-Android platform to get
        // the predictive back gesture work.
        PredictiveBackGestureOverlay(
            backDispatcher = rootComponent.backHandler as BackDispatcher,
            backIcon = { progress, _ ->
                PredictiveBackGestureIcon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    progress = progress,
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            App(
                rootComponent = rootComponent,
                modifier = Modifier.fillMaxSize()
            )
        }
    }