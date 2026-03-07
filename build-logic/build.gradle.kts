plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.compose.multiplatform.gradle.plugin)
}
