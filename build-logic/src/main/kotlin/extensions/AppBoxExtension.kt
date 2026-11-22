package extensions

import org.gradle.api.Project

/**
 * An app's dev-tool policy.
 *
 * The convention plugin does the wiring — it publishes `BuildConfig.DEV_TOOLS`, swaps Chucker for
 * its no-op artifact and adds the dev-only dependencies. The app decides which build types carry
 * dev tools, so `stage` or a flavour is one line here and no change anywhere else.
 *
 * ```kotlin
 * appBox {
 *     devBuildTypes = setOf("debug", "stage")
 *     devDependencies(projects.core.devtools)
 * }
 * ```
 */
open class AppBoxExtension(private val project: Project) {

    /** Build types that keep dev tools, persisted logs and the real Chucker interceptor. */
    var devBuildTypes: Set<String> = setOf("debug")

    private val notations = mutableListOf<Any>()

    /** Adds [dependencies] to the `*Implementation` configurations of [devBuildTypes] only. */
    fun devDependencies(vararg dependencies: Any) {
        notations.addAll(dependencies)
    }

    internal fun dependencyNotations(): List<Any> = notations.toList()

    internal fun requireBuildType(name: String) {
        require(project.configurations.findByName("${name}Implementation") != null) {
            "appBox: '${project.path}' has no '$name' build type"
        }
    }
}
