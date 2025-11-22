package com.popradiarpad.example.posedetector.shared

import com.arkivanov.essenty.backhandler.BackDispatcher

/*
Why this extra complication to create BackDispatcher on iOS?

Arkadi:
“On iOS, you should never be able to create a BackDispatcher without also intending to drive the gesture.
If you can just write BackDispatcher() in Swift, 99% of developers will create one, pass it to the component context,
and then forget to add PredictiveBackGestureOverlay. Result: back handling works on Android,
but completely broken on iOS (no swipe-back at all).”
*/
object IOSBackHandlerProvider {
    fun provide(): BackDispatcher = BackDispatcher()
}