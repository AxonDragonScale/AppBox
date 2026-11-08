package com.axondragonscale.devinator.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.ui.components.BackTopBar
import com.axondragonscale.devinator.ui.components.CodeBlock
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.rememberCopyToClipboard
import com.axondragonscale.devinator.ui.theme.DevinatorTheme

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

@Composable
private fun PermissionScreen(
    modifier: Modifier = Modifier,
    grantCommand: String,
    revokeCommand: String,
    onBack: () -> Unit,
) {
    val copy = rememberCopyToClipboard()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { BackTopBar(title = "Grant or revoke", onBack = onBack) },
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
                text = "Plug the phone in, then run this on the computer that has adb installed:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CodeBlock(
                command = grantCommand,
                onCopy = { copy("Grant command", grantCommand) },
            )

            DevinatorDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                inset = false,
            )

            SectionTitle(text = "Revoke")
            Text(
                text = "With the phone plugged in, run this to take the permission back:",
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
