import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kmp.app.convention)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.axondragonscale.limitless"
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "LimitlessComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.core.android)

            implementation(libs.androidx.splashscreen)

            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            // Room
            implementation(libs.androidx.room.ktx)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)

            // Coroutines
            implementation(libs.kotlinx.coroutines.android)
        }

        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.devtools)
            implementation(projects.core.datastore)
            implementation(projects.core.room)

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

            // Lifecycle
            implementation(libs.bundles.lifecycle.compose)

            implementation(libs.navigation.compose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin.common)

            // MaterialKolor
            implementation(libs.materialkolor)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
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
        mainClass = "com.axondragonscale.limitless.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.axondragonscale.limitless"
            packageVersion = "1.0.0"
        }
    }
}

compose.resources {
    packageOfResClass = "com.axondragonscale.limitless.resources"
}

room {
    schemaDirectory("$projectDir/schemas")
}
