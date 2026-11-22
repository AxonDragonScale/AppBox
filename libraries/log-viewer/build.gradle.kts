plugins {
    alias(libs.plugins.kmp.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `LogViewer`/`LogSource` expose Compose and Instant types.
            api(libs.bundles.compose.mp)
            api(libs.kotlinx.datetime)

            implementation(libs.bundles.lifecycle.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.logviewer"
    }
}
