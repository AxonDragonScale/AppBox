plugins {
    id("app-convention")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)
}
