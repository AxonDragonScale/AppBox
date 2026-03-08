// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

val compilerArgs = listOf(
    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
    "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
    "-Xexpect-actual-classes",
    "-opt-in=androidx.compose.ui.layout.ExperimentalLayoutApi",
    "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
    "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
)

subprojects {
    // Kotlin Multiplatform modules
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            compilerOptions {
                freeCompilerArgs.addAll(compilerArgs)
            }
        }
    }

    // Pure Kotlin Android modules
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                freeCompilerArgs.addAll(compilerArgs)
            }
        }
    }
}
