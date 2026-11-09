plugins {
    alias(libs.plugins.kmp.plain.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `createHttpClient` returns Ktor's HttpClient.
            api(libs.ktor.client.core)

            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(projects.core.common)

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.chucker)
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
        namespace = "com.axondragonscale.core.network"
    }
}
