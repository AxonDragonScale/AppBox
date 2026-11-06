// The one definition of the compiler flags shared by every module; applied by the convention plugins.
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
