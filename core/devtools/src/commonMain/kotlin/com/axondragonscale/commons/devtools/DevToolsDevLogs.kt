package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable
import com.axondragonscale.commons.devtools.logs.data.DevLogStore

/**
 * True when the host installed the persisted log store, so the reader has something to show. The
 * store itself is shared; only opening the database is per platform.
 */
internal val isDevLogsAvailable: Boolean get() = DevLogStore.isInstalled

/**
 * Presents the persisted log reader while [visible]. Each platform owns the container; the reader
 * inside is shared.
 */
@Composable
internal expect fun DevToolsDevLogsHost(visible: Boolean, onDismiss: () -> Unit)
