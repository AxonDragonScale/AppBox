plugins {
    alias(libs.plugins.android.library.convention)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.logcatviewer"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.common)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.lifecycle.viewmodel.compose.android)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.datastore.prefs)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
