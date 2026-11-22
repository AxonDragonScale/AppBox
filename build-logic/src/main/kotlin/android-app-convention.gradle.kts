import com.android.build.api.variant.BuildConfigField
import extensions.AppBoxExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val appBox = extensions.create("appBox", AppBoxExtension::class.java, project)

// Substitution works on the whole graph, including a library module's own Chucker dependency.
val libsCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
fun catalogModule(alias: String): String =
    libsCatalog.findLibrary(alias).get().get().let { "${it.module}:${it.versionConstraint.requiredVersion}" }

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

androidComponents {
    onVariants { variant ->
        val hasDevTools = variant.buildType in appBox.devBuildTypes
        variant.buildConfigFields?.put(
            "DEV_TOOLS",
            BuildConfigField("boolean", hasDevTools.toString(), "Keep dev tools, persisted logs and Chucker"),
        )

        // A single-variant library cannot pick a variant for its own Chucker dependency, so the
        // app swaps the artifact for every build type that is not a dev build.
        if (!hasDevTools) {
            configurations
                .matching { it.name.startsWith(variant.name) }
                .configureEach {
                    resolutionStrategy.dependencySubstitution {
                        substitute(module(catalogModule("chucker")))
                            .using(module(catalogModule("chucker-noop")))
                    }
                }
        }
    }
}

afterEvaluate {
    appBox.devBuildTypes.forEach { buildType ->
        appBox.requireBuildType(buildType)
        appBox.dependencyNotations().forEach { dependencies.add("${buildType}Implementation", it) }
    }
}
