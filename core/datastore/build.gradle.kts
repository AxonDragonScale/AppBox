plugins {
    alias(libs.plugins.kmp.plain.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public API exposes DataStore, Flow and Json types.
            api(libs.datastore.prefs)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)

            implementation(projects.core.common)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.datastore"
    }
}
