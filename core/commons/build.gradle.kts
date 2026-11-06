
plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.bundles.compose.mp)

            // Kotlin
            implementation(libs.bundles.kotlinx)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin.common)

            // Ktor
            implementation(libs.bundles.ktor.common)

            // Lifecycle
            implementation(libs.bundles.lifecycle.compose)

            implementation(libs.datastore.prefs)

            // Room
            implementation(libs.bundles.room.kmp)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.lifecycle.runtime.ktx)

            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.androidx.compose.ui.test.manifest)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.androidx.compose.navigation)

            implementation(libs.ktor.client.okhttp)
            implementation(libs.chucker)

            implementation(libs.androidx.room.ktx)

            implementation(libs.zxing)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.commons"
    }
}
