plugins {
    id("android-app-convention")
}

android {
    // Distinct from the KMP library namespace (AGP 9 requires unique package names).
    namespace = "com.axondragonscale.limitless.android"

    defaultConfig {
        applicationId = "com.axondragonscale.limitless"
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":apps:limitless:limitlessComposeApp"))
}
