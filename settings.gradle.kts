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
include(":core:commons")

// App modules (applications)
include(":apps:jest")
include(":apps:wallmatic")
include(":apps:topty")
include(":apps:npinner")
include(":apps:qrqanvas")
include(":apps:limitless")

// Library modules (KMP)
include(":libraries:tlv")
