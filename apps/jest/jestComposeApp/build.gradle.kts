import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kmp.app.convention)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.axondragonscale.jest"
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "JestComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.splashscreen)

            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            // Room
            implementation(libs.androidx.room.ktx)

            // Glance
            implementation(libs.bundles.glance)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)

            // Ktor
            implementation(libs.ktor.client.okhttp)
            implementation(libs.chucker)

            // Coroutines
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(projects.core.commons)

            // DataStore
            implementation(libs.datastore.prefs)

            // Room
            implementation(libs.bundles.room.kmp)

            // Ktor
            implementation(libs.bundles.ktor.common)

            // Kotlin
            implementation(libs.bundles.kotlinx)

            // Compose
            implementation(libs.bundles.compose.mp)
            implementation(libs.material3.adaptive)

            // Lifecycle
            implementation(libs.bundles.lifecycle.compose)

            implementation(libs.navigation.compose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin.common)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // Ktor
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    listOf("kspAndroid", "kspIosSimulatorArm64", "kspIosArm64", "kspJvm").forEach {
        add(it, libs.androidx.room.compiler)
    }

    // The Android-KMP plugin has a single variant, so there is no debugImplementation;
    // keep Compose tooling on the runtime classpath only.
    "androidRuntimeClasspath"(libs.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "com.axondragonscale.jest.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.axondragonscale.jest"
            packageVersion = "1.0.0"
        }
    }
}

compose.resources {
    packageOfResClass = "com.axondragonscale.jest.resources"
}

room {
    schemaDirectory("$projectDir/schemas")
}
