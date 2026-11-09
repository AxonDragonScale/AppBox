plugins {
    alias(libs.plugins.kmp.plain.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public API exposes Flow, Json and kotlinx.datetime types, so these are `api`.
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
        }
    }
}

kotlin {
    android {
        // Keep the Android namespace distinct from :core:commons while the Kotlin package
        // stays com.axondragonscale.commons during the migration.
        namespace = "com.axondragonscale.core.common"
    }
}
