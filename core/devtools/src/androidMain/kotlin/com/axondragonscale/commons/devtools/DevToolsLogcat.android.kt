package com.axondragonscale.commons.devtools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.axondragonscale.logviewer.LogViewer
import com.axondragonscale.logviewer.LogcatSource

/**
 * True when the platform can show the in-app log viewer. Only Android hosts it.
 */
actual val isLogcatAvailable: Boolean = true

private fun Context.canReadLogs(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_LOGS) == PackageManager.PERMISSION_GRANTED

@Composable
private fun LogcatContents(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.canReadLogs()) }
    val source = remember(context) { LogcatSource(context.applicationContext) }
    // The reader owns a process; it goes away with the screen that opened it.
    DisposableEffect(source) { onDispose { source.close() } }

    // The grant arrives over adb while the app is away: re-check whenever the user comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = context.canReadLogs() }

    if (granted) {
        LogViewer(
            source = source,
            modifier = modifier,
            caption = "all buffers",
            title = "Logcat",
            onBack = onBack,
            onShare = { shareLogs(context, it) },
            onCopy = { copyLog(context, it) },
        )
    } else {
        LogAccessGate(
            appLabel = context.applicationInfo.loadLabel(context.packageManager).toString(),
            packageName = context.packageName,
            onBack = onBack,
            onCheckAgain = { granted = context.canReadLogs() },
            onCopyCommand = { copyLog(context, it) },
        )
    }
}

/**
 * The viewer as a plain screen, for a host that wants it as a navigation destination instead of
 * the built-in dialog.
 */
@Composable
fun LogcatDevToolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    DevToolsTheme {
        DevToolsSystemBars()
        LogcatContents(modifier = modifier.fillMaxSize(), onBack = onBack)
    }
}

/**
 * Android has a viewer, so the dialog is the whole presentation: it owns back, input blocking and
 * its own window insets, and the viewer's top bar arrow closes it through [onDismiss].
 */
@Composable
actual fun DevToolsLogcatHost(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        DevToolsTheme {
            DevToolsSystemBars()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                LogcatContents(modifier = Modifier.fillMaxSize(), onBack = onDismiss)
            }
        }
    }
}
