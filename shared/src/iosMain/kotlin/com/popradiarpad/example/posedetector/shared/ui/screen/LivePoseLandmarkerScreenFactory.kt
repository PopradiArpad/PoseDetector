package com.popradiarpad.example.posedetector.shared.ui.screen

import platform.UIKit.UIViewController

interface LivePoseLandmarkerScreenFactory {
    fun create(): UIViewController
}

object LivePoseLandmarkerScreenFactoryProvider {
    lateinit var factory: LivePoseLandmarkerScreenFactory
}