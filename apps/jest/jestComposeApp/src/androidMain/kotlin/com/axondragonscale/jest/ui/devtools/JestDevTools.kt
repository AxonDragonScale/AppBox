package com.axondragonscale.jest.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.jest.BuildConfig

/**
 * Jest-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Jest-specific tools.
 */
@Composable
fun JestDevTools(
    modifier: Modifier = Modifier,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Jest Dev Tools",
        subtitle = "Debug utilities for Jest",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)
        }
    )
}
