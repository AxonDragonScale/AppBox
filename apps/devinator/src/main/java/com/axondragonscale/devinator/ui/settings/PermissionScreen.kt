package com.axondragonscale.devinator.ui.settings

import android.content.ClipData
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.MonoCommandTextStyle
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */

private const val PREVIEW_PACKAGE = "com.axondragonscale.devinator"

/**
 * The one permission the app needs, in both directions: the same `pm grant` Setup shows, and the
 * `pm revoke` that takes it back. Neither command can be run from inside the app, so this page is
 * reference — which is why it is a page and not a sheet.
 */
@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val packageName = LocalContext.current.packageName

    PermissionScreen(
        modifier = modifier,
        grantCommand = SecureSettingsPermission.grantCommand(packageName),
        revokeCommand = SecureSettingsPermission.revokeCommand(packageName),
        onBack = { navController.popBackStack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionScreen(
    modifier: Modifier = Modifier,
    grantCommand: String,
    revokeCommand: String,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun copy(label: String, command: String) {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, command)))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Permission", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Devinator needs the Write Secure Settings permission to change Developer " +
                    "options.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            SectionTitle(text = "Grant")
            Text(
                text = "Run this on a computer that has adb installed:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CodeBlock(
                command = grantCommand,
                onCopy = { copy("Grant command", grantCommand) },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            )

            SectionTitle(text = "Revoke")
            Text(
                text = "To take the permission back — before uninstalling, for example:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CodeBlock(
                command = revokeCommand,
                onCopy = { copy("Revoke command", revokeCommand) },
            )

            Text(
                text = "The grant survives reboots and app updates. Revoking it leaves Devinator " +
                    "unable to change anything until it is granted again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun CodeBlock(
    modifier: Modifier = Modifier,
    command: String,
    onCopy: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        // The block grows downwards, so the copy button stays at its top-right corner.
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = command,
            modifier = Modifier.weight(1f),
            style = MonoCommandTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy command",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        PermissionScreen(
            grantCommand = SecureSettingsPermission.grantCommand(PREVIEW_PACKAGE),
            revokeCommand = SecureSettingsPermission.revokeCommand(PREVIEW_PACKAGE),
            onBack = {},
        )
    }
}
