plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `DevToolsScreen`/`DevTool` expose Compose types.
            api(libs.bundles.compose.mp)

            implementation(projects.core.common)
            implementation(projects.libraries.logViewer)
            implementation(libs.bundles.lifecycle.compose)

            // The persisted log store; see `logs/data/DevLogStore`.
            implementation(libs.bundles.room.kmp)
        }

        androidMain.dependencies {
            implementation(projects.core.android)
            implementation(projects.libraries.storageViewer)
            implementation(libs.androidx.lifecycle.viewmodel.compose.android)
            implementation(libs.androidx.activity.compose)
        }
    }
}

dependencies {
    listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64").forEach {
        add(it, libs.androidx.room.compiler)
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.devtools"
    }
}
