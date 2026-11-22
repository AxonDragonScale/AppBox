package com.axondragonscale.commons.devtools.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One viewer a platform can open from the Diagnostics section: what the list row says, whether it
 * exists yet, and the screen behind it. The list and the screen change together, so they sit here.
 */
internal class DevToolsHost(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val available: () -> Boolean,
    val content: @Composable (onBack: () -> Unit) -> Unit,
)

/** The viewers this platform can open, in the order the screen lists them. */
internal expect val devToolsHosts: List<DevToolsHost>

/**
 * Presents the chosen host: Android opens it in its own dialog window, the desktop targets draw it
 * over the list.
 */
@Composable
internal expect fun DevToolsHostWindow(host: DevToolsHost?, onClose: () -> Unit)
