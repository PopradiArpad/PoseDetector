@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import co.touchlab.skie.configuration.FlowInterop

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            export(libs.decompose)
            export(libs.essenty.lifecycle)
            export(libs.essenty.stateKeeper)
            export(libs.essenty.backHandler)

            baseName = "shared"
        }
    }

    // This is a try to integrate the pod handling into gradle as it'S
    // recommended in the KMP docs. Unfortunately the direct remote pod
    // definition doesn't generate the .xcworkspace rendering
    // the whole process useless. I don't know why it doesn't work.
    // Give a try later.
    // Until that manual pod handling without gradle integration :(
    // pod update workflow quite brutal:
    // 0. close XCode
    // 1. delete Podfile.lock
    // 2. delete .xcworkspace
    // 3. update Podfile
    // 4. pod install
    // 5. Let Android Studio generate XCode stuff
    // 6. open .xcworkspace
    cocoapods {
        version = "1.0"
        summary = "Shared module for PoseDetector"
        homepage = "Link to the project homepage"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            implementation(libs.napier)

            implementation(libs.decompose)
            implementation(libs.decompose.extension.compose)

            implementation(libs.jetbrains.kotlinx.kotlinxSerializationJson)

            api(libs.decompose)
            api(libs.essenty.lifecycle)
            api(libs.essenty.stateKeeper)
            api(libs.essenty.backHandler)
            implementation("com.popradiarpad.ensurecamerapermission:ensure-camera-permission:0.1.0")
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose) // Added for permission handling
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.ui.tooling.preview)

            // CameraX
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)

            // MediaPipe Tasks Vision (Pose Landmarker)
            implementation(libs.tasks.vision)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.popradiarpad.example.posedetector.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// For higher level interop between Kotlin and Swift.
skie {
    features {
        group {
            FlowInterop.Enabled(false)
        }
        group("com.popradiarpad.example.posedetector.shared.storage") {
            FlowInterop.Enabled(true)
        }
    }
}

