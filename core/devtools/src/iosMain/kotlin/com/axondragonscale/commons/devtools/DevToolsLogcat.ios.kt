package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable

actual val isLogcatAvailable: Boolean = false

@Composable
actual fun DevToolsLogcatHost(visible: Boolean, onDismiss: () -> Unit) = Unit
