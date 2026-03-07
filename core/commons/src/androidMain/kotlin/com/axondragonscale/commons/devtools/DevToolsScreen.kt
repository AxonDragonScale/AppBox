package com.axondragonscale.commons.devtools

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShieldMoon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.PermissionState
import com.axondragonscale.commons.clearAppData
import com.axondragonscale.commons.getDangerousPermissions
import com.axondragonscale.commons.getMissingPermissions
import com.axondragonscale.commons.openAppSettings
import com.axondragonscale.commons.revokeGrantedPermissionsOnKill
import kotlin.system.exitProcess

/**
 * A reusable developer tools screen that provides common debugging utilities
 * and supports custom tools provided by the app.
 *
 * Built-in tools include:
 * - Kill App: Force stops the application
 * - Clear Data: Clears all app data and restarts
 * - Permissions: Shows all dangerous permissions and their status
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
    extraContent: @Composable (ColumnScope.() -> Unit)? = null,
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

        // App Actions Section
        item {
            DevToolItem(
                tool = DevTool.Section(
                    title = "App Actions",
                    icon = Icons.Rounded.PowerSettingsNew,
                )
            )
        }

        item { KillAppTool() }
        item { ClearDataTool() }
        item { OpenAppSettingsTool() }

        // Permissions Section
        item {
            PermissionsSection()
        }

        // Custom Tools Section
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Kill App tool - force stops the application.
 */
@Composable
private fun KillAppTool() {
    val context = LocalContext.current

    DevToolActionItem(
        tool = DevTool.Action(
            title = "Kill App",
            subtitle = "Force stop the application",
            icon = Icons.Rounded.PowerSettingsNew,
            destructive = true,
            onClick = {
                (context as? Activity)?.finishAffinity()
                exitProcess(0)
            },
        )
    )
}

/**
 * Open App Settings tool - opens the system app info page.
 */
@Composable
private fun OpenAppSettingsTool() {
    val context = LocalContext.current

    DevToolActionItem(
        tool = DevTool.Action(
            title = "Open App Settings",
            subtitle = "Manage permissions and app info",
            icon = Icons.Rounded.Settings,
            onClick = { context.openAppSettings() },
        )
    )
}

/**
 * Clear Data tool - clears all app data and restarts.
 */
@Composable
private fun ClearDataTool() {
    val context = LocalContext.current

    DevToolActionItem(
        tool = DevTool.Action(
            title = "Clear App Data",
            subtitle = "Delete all local data and caches",
            icon = Icons.Rounded.DeleteForever,
            destructive = true,
            onClick = { context.clearAppData() },
        )
    )
}

/**
 * Permissions section showing all dangerous permissions and their status.
 */
@Composable
private fun PermissionsSection() {
    val context = LocalContext.current
    var permissions by remember { mutableStateOf<List<PermissionState>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        permissions = context.getDangerousPermissions()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissions = context.getDangerousPermissions()
    }

    val grantedCount = permissions.count { it.granted }
    val totalCount = permissions.size

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        DevToolItem(
            tool = DevTool.Section(
                title = "Permissions ($grantedCount/$totalCount)",
                icon = Icons.Rounded.Security,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Permission items
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (permissions.isEmpty()) {
                    Text(
                        text = "No dangerous permissions requested",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                } else {
                    permissions.forEach { permission ->
                        PermissionItem(permission = permission)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val missingPermissions = permissions.getMissingPermissions()
                    if (missingPermissions.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { launcher.launch(missingPermissions) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Missing Permissions")
                        }
                    }

                    if (grantedCount > 0) {
                        FilledTonalButton(
                            onClick = {
                                context.revokeGrantedPermissionsOnKill(permissions)
                                exitProcess(0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShieldMoon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Revoke Runtime Permissions & Kill")
                        }
                    }


                }
            }
        }
    }
}

/**
 * Individual permission item showing name and grant status.
 */
@Composable
private fun PermissionItem(permission: PermissionState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = permission.name.substringAfterLast("."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (permission.granted) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (permission.granted) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = if (permission.granted) "Granted" else "Denied",
                tint = if (permission.granted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp),
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
    extraContent: @Composable (ColumnScope.() -> Unit)? = null,
) {
    DevToolsScreen(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        customTools = buildDevTools(customToolsBuilder),
        extraContent = extraContent,
    )
}
