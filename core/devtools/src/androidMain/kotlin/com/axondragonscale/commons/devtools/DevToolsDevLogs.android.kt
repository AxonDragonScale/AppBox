package com.axondragonscale.commons.devtools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.room.Room
import com.axondragonscale.commons.devtools.logs.data.DEV_LOG_DB_NAME
import com.axondragonscale.commons.devtools.logs.data.DevLogDatabase
import com.axondragonscale.commons.devtools.logs.data.DevLogStore
import com.axondragonscale.logviewer.LogViewer

/**
 * Opens the persisted log database and starts copying every `Logger` record into it. Call once,
 * early, and only from a build type that keeps dev tools.
 */
internal fun installDevLogStore(context: Context) {
    val appContext = context.applicationContext
    DevLogStore.install {
        Room.databaseBuilder(appContext, DevLogDatabase::class.java, DEV_LOG_DB_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

@Composable
internal actual fun DevToolsDevLogsHost(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
    val context = LocalContext.current
    // No store owner of its own: the reader's ViewModel lives in the host's, so a recreation keeps
    // the filters and the loaded rows.
    val source = remember { DevLogStore.source() }

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
                LogViewer(
                    source = source,
                    modifier = Modifier.fillMaxSize(),
                    caption = "on device",
                    onBack = onDismiss,
                    onShare = { shareLogs(context, it) },
                    onCopy = { copyLog(context, it) },
                )
            }
        }
    }
}

internal fun shareLogs(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share logs"))
}

internal fun copyLog(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Logs", text))
}
