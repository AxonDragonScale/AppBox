package com.axondragonscale.commons.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Platform-specific built-in dev tool content (Kill App, Clear Data, App Settings, Permissions).
 * Each platform provides its own actual implementation.
 */
@Composable
expect fun DevToolsBuiltInContent()

/**
 * A reusable developer tools screen that provides common debugging utilities
 * and supports custom tools provided by the app.
 *
 * Built-in platform tools are rendered via [DevToolsBuiltInContent].
 *
 * @param modifier Modifier for the screen
 * @param title Title displayed at the top of the screen
 * @param subtitle Optional subtitle displayed below the title
 * @param customTools List of custom DevTools to display after built-in tools
 * @param extraContent Optional composable content to display at the end
 */
@Composable
fun DevToolsScreen(
    modifier: Modifier = Modifier,
    title: String = "Developer Tools",
    subtitle: String? = "Debug utilities and settings",
    customTools: List<DevTool> = emptyList(),
    extraContent: @Composable (() -> Unit)? = null,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = statusBarPadding.calculateTopPadding() + 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header
        item {
            DevToolsHeader(
                title = title,
                subtitle = subtitle,
            )
        }

        // App Actions section header
        item {
            DevToolItem(
                tool = DevTool.Section(
                    title = "App Actions",
                    icon = Icons.Rounded.PowerSettingsNew,
                )
            )
        }

        // Platform-specific built-in tools
        item {
            DevToolsBuiltInContent()
        }

        // Custom Tools
        if (customTools.isNotEmpty()) {
            items(customTools) { tool ->
                DevToolItem(tool = tool)
            }
        }

        // Extra Content
        if (extraContent != null) {
            item {
                Column {
                    extraContent()
                }
            }
        }
    }
}

/**
 * Header component for the DevTools screen.
 */
@Composable
private fun DevToolsHeader(
    title: String,
    subtitle: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(4.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Convenience function to create a DevToolsScreen with tools defined using the builder DSL.
 *
 * Usage:
 * ```
 * DevToolsScreen(
 *     customToolsBuilder = {
 *         section("Database", Icons.Rounded.Storage)
 *         action("Clear Cache") { clearCache() }
 *         toggle("Debug Mode", isDebug) { setDebug(it) }
 *     }
 * )
 * ```
 */
@Composable
fun DevToolsScreen(
    modifier: Modifier = Modifier,
    title: String = "Developer Tools",
    subtitle: String? = "Debug utilities and settings",
    customToolsBuilder: DevToolsBuilder.() -> Unit,
    extraContent: @Composable (() -> Unit)? = null,
) {
    DevToolsScreen(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        customTools = buildDevTools(customToolsBuilder),
        extraContent = extraContent,
    )
}
