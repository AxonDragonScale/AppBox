package com.axondragonscale.cardy.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.repository.CardRepository
import com.axondragonscale.cardy.ui.theme.CardyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Created by Ronak on 05/30/26 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val nfcManager: NfcManager = koinInject()
    val cardRepository: CardRepository = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SettingsScreen(
        modifier = modifier,
        isNfcSupported = nfcManager.isNfcSupported,
        isNfcEnabled = nfcManager.isNfcEnabled,
        onOpenNfcSettings = {
            context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        },
        onClearAllCards = {
            scope.launch(Dispatchers.IO) {
                cardRepository.deleteAllCards()
            }
        },
        onBack = { navController.popBackStack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    isNfcSupported: Boolean,
    isNfcEnabled: Boolean,
    onOpenNfcSettings: () -> Unit,
    onClearAllCards: () -> Unit,
    onBack: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // NFC Section
            SectionHeader(title = "NFC")

            SettingsRow(
                icon = Icons.Default.Nfc,
                title = if (isNfcSupported) "NFC Status" else "NFC Not Available",
                subtitle = when {
                    !isNfcSupported -> "This device does not have NFC hardware"
                    isNfcEnabled -> "Enabled and ready"
                    else -> "Tap to open NFC settings"
                },
                onClick = if (isNfcSupported && !isNfcEnabled) onOpenNfcSettings else null,
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Data Section
            SectionHeader(title = "Data")

            SettingsRow(
                icon = Icons.Default.Delete,
                title = "Clear All Saved Cards",
                subtitle = "Permanently delete all cards from vault",
                onClick = { showClearDialog = true },
                isDestructive = true,
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            )

            // About Section
            SectionHeader(title = "About")

            SettingsRow(
                icon = Icons.Default.Info,
                title = "Cardy",
                subtitle = "Version 1.0.0",
                onClick = null,
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Cards") },
            text = { Text("This will permanently delete all saved cards from the vault. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearAllCards()
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    CardyTheme {
        SettingsScreen(
            isNfcSupported = true,
            isNfcEnabled = true,
            onOpenNfcSettings = {},
            onClearAllCards = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenNfcDisabledPreview() {
    CardyTheme {
        SettingsScreen(
            isNfcSupported = true,
            isNfcEnabled = false,
            onOpenNfcSettings = {},
            onClearAllCards = {},
            onBack = {},
        )
    }
}
