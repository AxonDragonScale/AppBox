plugins {
    id("android-app-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.axondragonscale.qrqanvas"

    defaultConfig {
        applicationId = "com.axondragonscale.qrqanvas"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(projects.core.commons)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.bundles.compose.debug)

    // CameraX and Barcode Scanning
    implementation(libs.bundles.camerax)
    implementation(libs.mlkit.barcode.scanner)

    // Coil
    implementation(libs.bundles.coil)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlin
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.guava)
    implementation(libs.accompanist.permissions)
    implementation(libs.zxing)

    // Datastore
    implementation(libs.bundles.datastore)
    implementation(libs.protobuf.kotlin)
}
