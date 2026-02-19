package com.axondragonscale.topty.ui.backuprestore

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.compose.Dropdown
import com.axondragonscale.commons.compose.DropdownItem
import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import com.axondragonscale.topty.util.generateSuggestedFileName
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 27/01/25
 */

@Composable
fun BackupRestore(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: BackupRestoreVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    BackupRestore(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                BackupRestoreUiEvent.NavigateBack -> navController.popBackStack()

                else -> vm.onEvent(event)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupRestore(
    modifier: Modifier = Modifier,
    uiState: BackupRestoreUiState,
    onEvent: (BackupRestoreUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(BackupRestoreUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            InfoCard(tokenCount = uiState.tokenCount)

            // Export section
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("*/*")
            ) { uri: Uri? ->
                uri?.let { onEvent(BackupRestoreUiEvent.ExportBackup(it)) }
            }

            BackupActionSection(
                modifier = modifier,
                title = "Export Backup",
                icon = Icons.Default.CloudUpload,
                description = "Save your tokens to a file in selected format",
                buttonText = "Export Backup",
                selectedFormat = uiState.selectedExportFormat,
                isLoading = uiState.isLoading,
                onFormatSelected = { onEvent(BackupRestoreUiEvent.SelectExportFormat(it)) },
                onAction = { exportLauncher.launch(uiState.selectedExportFormat.generateSuggestedFileName()) }
            )

            // Import section
            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let { onEvent(BackupRestoreUiEvent.ImportBackup(it)) }
            }

            BackupActionSection(
                modifier = modifier,
                title = "Import Backup",
                icon = Icons.Default.CloudDownload,
                description = "Restore tokens from a file of the selected format",
                buttonText = "Import Backup",
                selectedFormat = uiState.selectedImportFormat,
                isLoading = uiState.isLoading,
                onFormatSelected = { onEvent(BackupRestoreUiEvent.SelectImportFormat(it)) },
                onAction = { importLauncher.launch("*/*") }
            )
        }
    }

    // Success dialog
    if (uiState.exportSuccess) {
        SuccessDialog(
            title = "Backup Exported",
            message = "Your backup has been saved successfully.",
            onDismiss = { onEvent(BackupRestoreUiEvent.DismissSuccess) }
        )
    }

    if (uiState.importSuccess) {
        SuccessDialog(
            title = "Import Complete",
            message = "Tokens imported successfully.",
            onDismiss = { onEvent(BackupRestoreUiEvent.DismissSuccess) }
        )
    }

    // Error dialog
    uiState.errorMessage?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { onEvent(BackupRestoreUiEvent.DismissError) }
        )
    }

    // Import confirmation dialog
    if (uiState.pendingImportTokens != null) {
        ImportConfirmationDialog(
            tokenCount = uiState.pendingImportTokens.size,
            onConfirm = { onEvent(BackupRestoreUiEvent.ConfirmImport(uiState.pendingImportTokens)) },
            onDismiss = { onEvent(BackupRestoreUiEvent.DismissError) }
        )
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    tokenCount: Int,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = DefaultShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Current Tokens: $tokenCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Export your tokens for safekeeping or import from another app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun BackupActionSection(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    description: String,
    buttonText: String,
    selectedFormat: BackupFormat,
    isLoading: Boolean,
    onFormatSelected: (BackupFormat) -> Unit,
    onAction: () -> Unit,
) {
    SectionCard(
        modifier = modifier,
        title = title,
        icon = icon,
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Format selector
        Dropdown(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            selectedItem = DropdownItem(selectedFormat.displayName, selectedFormat),
            items = BackupFormat.entries.map {
                DropdownItem(name = it.displayName, value = it)
            },
            onItemSelected = { onFormatSelected(it.value) },
            shape = DefaultShape,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Format description
        Text(
            text = selectedFormat.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action button
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = DefaultShape,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(buttonText)
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = DefaultShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}

@Composable
private fun ImportConfirmationDialog(
    tokenCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text("Confirm Import") },
        text = {
            Text(
                text = "Import $tokenCount token(s) from the backup file?\n\nThis will add them to your existing tokens.",
                textAlign = TextAlign.Start,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = DefaultShape,
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        val uiState = remember {
            BackupRestoreUiState(
                tokenCount = 12,
                selectedExportFormat = BackupFormat.TOPTY,
                selectedImportFormat = BackupFormat.AEGIS,
            )
        }
        BackupRestore(
            uiState = uiState,
            onEvent = { },
        )
    }
}
