plugins {
    alias(libs.plugins.kmp.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public composables/`UIMode` extensions expose Compose and kotlinx.datetime types.
            api(projects.core.common)
            api(libs.bundles.compose.mp)

            implementation(libs.bundles.lifecycle.compose)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.ui"
    }
}
