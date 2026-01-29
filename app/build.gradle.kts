plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.appdump"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.axondragonscale.appdump"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("AppDumpSigningConfig") {
            storeFile = file("$rootDir/keystore/AppDumpKeystore.jks")
            storePassword = "AppDumpStorePass"
            keyAlias = "AppDumpKey"
            keyPassword = "AppDumpKeyPass"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("AppDumpSigningConfig")
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs.getByName("AppDumpSigningConfig")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Local project modules
    implementation(projects.wallmatic)
    implementation(projects.jest)
    implementation(projects.devButler)
    implementation(projects.topty)
    implementation(projects.commons)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.datastore.prefs)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}