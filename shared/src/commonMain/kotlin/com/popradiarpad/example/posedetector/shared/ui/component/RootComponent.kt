package com.popradiarpad.example.posedetector.shared.ui.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(config: Config, context: ComponentContext): Child =
        when (config) {
            is Config.Home -> Child.Home(HomeComponent(context) {
                navigation.push(Config.LivePoseLandmarker)
            })
            is Config.LivePoseLandmarker -> Child.LivePoseLandmarker(LivePoseLandmarkerComponent(context) {
                navigation.pop()
            })
        }

    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class LivePoseLandmarker(val component: LivePoseLandmarkerComponent) : Child()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Home : Config

        @Serializable
        data object LivePoseLandmarker : Config
    }
}
