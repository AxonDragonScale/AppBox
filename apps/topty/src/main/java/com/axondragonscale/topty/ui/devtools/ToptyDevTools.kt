package com.axondragonscale.topty.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.topty.BuildConfig

/**
 * Topty-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Topty-specific tools.
 */
@Composable
fun ToptyDevTools(
    modifier: Modifier = Modifier,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Topty Dev Tools",
        subtitle = "Debug utilities for Topty",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)
        }
    )
}
