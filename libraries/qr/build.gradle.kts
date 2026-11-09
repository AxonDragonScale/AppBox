plugins {
    alias(libs.plugins.android.library.convention)
}

android {
    namespace = "com.axondragonscale.library.qr"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.zxing)
}
