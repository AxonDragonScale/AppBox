plugins {
    alias(libs.plugins.kmp.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `DevToolsScreen`/`DevTool` expose Compose types and reuse the UI module.
            api(projects.core.ui)
            api(libs.bundles.compose.mp)
        }

        androidMain.dependencies {
            implementation(projects.core.android)
            implementation(libs.androidx.activity.compose)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.devtools"
    }
}
