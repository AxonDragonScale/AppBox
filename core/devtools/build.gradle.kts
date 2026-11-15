plugins {
    alias(libs.plugins.kmp.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `DevToolsScreen`/`DevTool` expose Compose types.
            api(libs.bundles.compose.mp)
        }

        androidMain.dependencies {
            implementation(projects.core.android)
            implementation(projects.libraries.logcatViewer)
            implementation(projects.libraries.storageViewer)
            implementation(libs.androidx.lifecycle.viewmodel.compose.android)
            implementation(libs.androidx.activity.compose)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.devtools"
    }
}
