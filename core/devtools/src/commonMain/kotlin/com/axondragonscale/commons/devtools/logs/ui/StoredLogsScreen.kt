package com.axondragonscale.commons.devtools.logs.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.logs.data.DevLogStore
import com.axondragonscale.logviewer.LogViewer

/** The persisted dev log store as the shared log viewer reads it. */
@Composable
internal fun StoredLogsScreen(
    onBack: () -> Unit,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogViewer(
        // No store owner of its own: the reader's ViewModel lives in the host's, so a recreation
        // keeps the filters and the loaded rows.
        source = remember { DevLogStore.source() },
        modifier = modifier.fillMaxSize(),
        caption = "on device",
        onBack = onBack,
        onShare = onShare,
        onCopy = onCopy,
    )
}
