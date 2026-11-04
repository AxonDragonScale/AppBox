// Compiler flags shared by the pure-Android modules. KMP modules get the same
// flags from the root build script's `org.jetbrains.kotlin.multiplatform` hook.
internal object AppBoxCompilerArgs {
    val optIns = listOf(
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        "-Xexpect-actual-classes",
        "-opt-in=androidx.compose.ui.layout.ExperimentalLayoutApi",
        "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
        "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
    )
}
