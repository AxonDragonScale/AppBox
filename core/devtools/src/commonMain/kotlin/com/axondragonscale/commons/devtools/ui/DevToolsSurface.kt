package com.axondragonscale.commons.devtools.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The theme, bar appearance and background every dev tool screen shares. */
@Composable
internal fun DevToolsSurface(content: @Composable () -> Unit) {
    DevToolsTheme {
        DevToolsSystemBars()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
