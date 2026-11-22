package com.axondragonscale.commons.devtools.host

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import com.axondragonscale.commons.devtools.logs.data.DevLogStore
import com.axondragonscale.commons.devtools.logs.ui.StoredLogsScreen
import com.axondragonscale.commons.devtools.ui.DevToolsSurface
import platform.UIKit.UIPasteboard

internal actual val devToolsHosts: List<DevToolsHost> = listOf(
    DevToolsHost(
        id = "logs",
        title = "Open Logs",
        subtitle = "Read the logs kept on this device",
        icon = Icons.Rounded.History,
        available = { DevLogStore.isInstalled },
        content = { onBack ->
            StoredLogsScreen(
                onBack = onBack,
                onShare = { UIPasteboard.generalPasteboard.string = it },
                onCopy = { UIPasteboard.generalPasteboard.string = it },
            )
        },
    ),
)

/** One window, so the chosen tool draws over the list. */
@Composable
internal actual fun DevToolsHostWindow(host: DevToolsHost?, onClose: () -> Unit) {
    if (host == null) return
    DevToolsSurface {
        host.content(onClose)
    }
}
