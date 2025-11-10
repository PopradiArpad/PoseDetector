package com.popradiarpad.example.posedetector.shared.lifecycle

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

class LifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry()

    init {
        lifecycleRegistry.resume()
    }

    override val lifecycle: Lifecycle by ::lifecycleRegistry

    fun destroy() {
        lifecycleRegistry.destroy()
    }
}
