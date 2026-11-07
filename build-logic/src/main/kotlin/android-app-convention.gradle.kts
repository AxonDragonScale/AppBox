plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 31
        targetSdk = 37
    }

    signingConfigs {
        create("release") {
            storeFile = file("$rootDir/keystore/AppBoxKeystore.jks")
            storePassword = "AppBoxStorePass"
            keyAlias = "AppBoxKey"
            keyPassword = "AppBoxKeyPass"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")
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

extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension>("kotlin") {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(AppBoxCompilerArgs.optIns)
    }
}
