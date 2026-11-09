plugins {
    alias(libs.plugins.kmp.plain.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `createDatabase` returns RoomDatabase; converters expose kotlinx.datetime types.
            api(libs.androidx.room.runtime)
            api(libs.kotlinx.datetime)

            implementation(libs.kotlinx.serialization.json)
        }

        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

kotlin {
    android {
        namespace = "com.axondragonscale.core.room"
    }
}
