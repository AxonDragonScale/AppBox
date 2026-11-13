plugins {
    alias(libs.plugins.android.library.convention)
}

android {
    namespace = "com.axondragonscale.core.android"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
}
