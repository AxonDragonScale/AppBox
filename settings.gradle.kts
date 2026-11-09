enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AppBox"

// Core modules (libraries)
include(":core:android")
include(":core:common")
include(":core:ui")
include(":core:devtools")
include(":core:datastore")
include(":core:room")
include(":core:network")

// App modules (applications)
include(":apps:jest:jestComposeApp")
include(":apps:jest:jestAndroid")
include(":apps:wallmatic")
include(":apps:topty")
include(":apps:npinner")
include(":apps:qrqanvas")
include(":apps:devinator")
include(":apps:limitless:limitlessComposeApp")
include(":apps:limitless:limitlessAndroid")

// Library modules (KMP)
include(":libraries:tlv")
include(":libraries:bitops")
include(":libraries:logcat-viewer")
include(":libraries:qr")
include(":apps:cardy")
