plugins {
    alias(libs.plugins.android.app.convention)
}

android {
    // Distinct from the KMP library namespace (AGP 9 requires unique package names).
    namespace = "com.axondragonscale.jest.android"

    defaultConfig {
        applicationId = "com.axondragonscale.jest"
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
    implementation(project(":apps:jest:jestComposeApp"))
}
