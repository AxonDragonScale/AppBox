package com.axondragonscale.limitless.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.limitless.BuildConfig

/**
 * Limitless-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Limitless-specific tools.
 */
@Composable
fun LimitlessDevTools(
    modifier: Modifier = Modifier,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Limitless Dev Tools",
        subtitle = "Debug utilities for Limitless",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)
        }
    )
}
