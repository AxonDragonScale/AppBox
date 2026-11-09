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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.PermissionState
import com.axondragonscale.commons.clearAppData
import com.axondragonscale.commons.getDangerousPermissions
import com.axondragonscale.commons.getMissingPermissions
import com.axondragonscale.commons.openAppSettings
import com.axondragonscale.commons.revokeGrantedPermissionsOnKill
import kotlin.system.exitProcess

/**
 * Android actual for [DevToolsBuiltInContent].
 *
 * Renders: Kill App, Clear Data, Open App Settings, Permissions.
 */
@Composable
actual fun DevToolsBuiltInContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KillAppTool()
        ClearDataTool()
        OpenAppSettingsTool()
        PermissionsSection()
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
