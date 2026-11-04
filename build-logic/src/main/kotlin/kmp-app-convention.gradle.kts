import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    // The Android-KMP plugin is single-variant: no buildTypes/proguardFiles here.
    // Modules still set their own `namespace` inside `kotlin { android { } }`.
    android {
        compileSdk { version = release(37) }
        minSdk = 31
        androidResources { enable = true }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    jvmToolchain(21)
}
