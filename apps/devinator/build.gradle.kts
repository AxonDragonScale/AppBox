plugins {
    alias(libs.plugins.android.app.convention)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.devinator"

    defaultConfig {
        applicationId = "com.axondragonscale.devinator"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(projects.core.commons)
    implementation(projects.libraries.logcatViewer)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // DataStore
    implementation(libs.datastore.prefs)

    // Kotlin
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
}
