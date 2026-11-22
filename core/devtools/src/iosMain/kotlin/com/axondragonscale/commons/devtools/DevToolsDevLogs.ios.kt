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
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIPasteboard

/**
 * Opens the persisted log database in the documents directory and starts copying every `Logger`
 * record into it. Call once, early, and only from a dev build.
 */
internal fun installDevLogStore() {
    DevLogStore.install {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(directory?.path) { "Could not resolve NSDocumentDirectory" }
        Room.databaseBuilder<DevLogDatabase>(name = "$path/$DEV_LOG_DB_NAME")
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
                onShare = { UIPasteboard.generalPasteboard.string = it },
                onCopy = { UIPasteboard.generalPasteboard.string = it },
            )
        }
    }
}
