package com.axondragonscale.commons.devtools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.axondragonscale.commons.devtools.logs.data.DEV_LOG_DB_NAME
import com.axondragonscale.commons.devtools.logs.data.DevLogDatabase
import com.axondragonscale.commons.devtools.logs.data.DevLogStore
import com.axondragonscale.logviewer.LogViewer
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

/**
 * Opens the persisted log database under the user's home and starts copying every `Logger` record
 * into it. Call once, early, and only from a dev build.
 */
internal fun installDevLogStore() {
    DevLogStore.install {
        val directory = File(System.getProperty("user.home"), ".appbox").apply { mkdirs() }
        Room.databaseBuilder<DevLogDatabase>(name = File(directory, DEV_LOG_DB_NAME).absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}

@Composable
internal actual fun DevToolsDevLogsHost(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
    val source = remember { DevLogStore.source() }
    DevToolsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            LogViewer(
                source = source,
                modifier = Modifier.fillMaxSize(),
                caption = "on device",
                onBack = onDismiss,
                onShare = { println(it) },
                onCopy = { text ->
                    runCatching {
                        Toolkit.getDefaultToolkit().systemClipboard
                            .setContents(StringSelection(text), null)
                    }
                },
            )
        }
    }
}
