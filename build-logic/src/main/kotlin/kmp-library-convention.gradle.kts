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

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("consumer-rules.pro")
            }
        }
    }

    jvm()
    iosArm64()
    iosSimulatorArm64()
//    js() { browser() }
//    wasmJs() { browser() }

    jvmToolchain(21)
}
