plugins {
    id("app-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.cardy"

    defaultConfig {
        applicationId = "com.axondragonscale.cardy"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(projects.core.commons)
    implementation(projects.libraries.tlv)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.biometric)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlin
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
}
