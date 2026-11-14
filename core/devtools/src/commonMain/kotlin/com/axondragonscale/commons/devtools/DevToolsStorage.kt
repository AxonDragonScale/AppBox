package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable

/**
 * True when the platform can show the in-app storage viewer. Only Android hosts it.
 */
internal expect val isStorageViewerAvailable: Boolean

/**
 * Presents the platform storage viewer while [visible]. JVM and iOS have no viewer, so they ignore it.
 */
@Composable
internal expect fun DevToolsStorageHost(visible: Boolean, onDismiss: () -> Unit)
