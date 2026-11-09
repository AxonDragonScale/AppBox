package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable

/**
 * True when the platform can show the in-app log viewer. Only Android hosts it.
 */
expect val isLogcatAvailable: Boolean

/**
 * Presents the platform log viewer while [visible]. JVM and iOS have no viewer, so they ignore it.
 */
@Composable
expect fun DevToolsLogcatHost(visible: Boolean, onDismiss: () -> Unit)
