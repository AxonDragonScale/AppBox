// Applied by the root project: KMP modules that bypass the other conventions still get the shared flags.
subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            compilerOptions {
                freeCompilerArgs.addAll(AppBoxCompilerArgs.optIns)
            }
        }
    }
}
