plugins {
    alias(libs.plugins.android.library.convention)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.storageviewer"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // The source seam exposes Result and StorageError.
    api(projects.core.common)

    // The viewer pins itself to the shared dev-tools theme.
    implementation(projects.core.ui)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // The source seam exposes Flow and the model types are @Serializable.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose.android)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.datastore.prefs)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
