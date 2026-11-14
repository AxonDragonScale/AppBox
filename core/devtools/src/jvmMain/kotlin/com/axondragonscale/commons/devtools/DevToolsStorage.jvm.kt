package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable

internal actual val isStorageViewerAvailable: Boolean = false

@Composable
internal actual fun DevToolsStorageHost(visible: Boolean, onDismiss: () -> Unit) = Unit
