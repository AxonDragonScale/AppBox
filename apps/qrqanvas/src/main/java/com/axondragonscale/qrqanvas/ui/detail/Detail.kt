package com.axondragonscale.qrqanvas.ui.detail

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun Detail(
    modifier: Modifier = Modifier,
    qrCodeId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (Long) -> Unit,
) {
    val viewModel: DetailVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load entity on first composition
    LaunchedEffect(qrCodeId) {
        viewModel.loadQRCode(qrCodeId)
    }

    // Refresh when returning from the editor (recomposition after pop)
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Handle one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DetailUiEffect.NavigateToEditor -> onNavigateToEditor(effect.qrCodeId)
                is DetailUiEffect.NavigateBack -> onNavigateBack()
                is DetailUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Detail(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Detail(
    modifier: Modifier = Modifier,
    uiState: DetailUiState = DetailUiState(),
    onEvent: (DetailUiEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.title.ifEmpty { "QR Code" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(DetailUiEvent.ExportClicked) },
                        enabled = uiState.qrBitmap != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Save to Downloads",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Large QR Preview
                QRPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 16.dp),
                    bitmap = uiState.qrBitmap,
                )

                // Content Summary
                ContentSummary(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    content = uiState.content,
                )

                // Timestamps
                TimestampInfo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    createdAt = uiState.createdAt,
                    modifiedAt = uiState.modifiedAt,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                ActionButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onEditClick = { onEvent(DetailUiEvent.EditClicked) },
                    onShareClick = { onEvent(DetailUiEvent.ShareClicked) },
                    onDeleteClick = { onEvent(DetailUiEvent.DeleteClicked) },
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { onEvent(DetailUiEvent.ConfirmDelete) },
            onDismiss = { onEvent(DetailUiEvent.DismissDelete) },
        )
    }
}

@Composable
private fun QRPreview(
    modifier: Modifier = Modifier,
    bitmap: Bitmap?,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                )
                Text(
                    text = "Rendering…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun ContentSummary(
    modifier: Modifier = Modifier,
    content: QRContent?,
) {
    if (content == null) return

    val typeLabel = contentTypeLabel(content)
    val typeIcon = contentTypeIcon(content)
    val displayValue = contentDisplayValue(content)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Content",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = typeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TimestampInfo(
    modifier: Modifier = Modifier,
    createdAt: Long,
    modifiedAt: Long,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Created: ${formatTimestamp(createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Modified: ${formatTimestamp(modifiedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = onEditClick,
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(text = "Edit")
        }

        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = onShareClick,
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(text = "Share")
        }

        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onDeleteClick,
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = "Delete",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(text = "Delete QR Code")
        },
        text = {
            Text(text = "This QR code will be permanently deleted. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

// --- Helper functions ---

private fun contentTypeLabel(content: QRContent): String = when (content) {
    is QRContent.Text -> "Text"
    is QRContent.Url -> "URL"
    is QRContent.WiFi -> "Wi-Fi"
    is QRContent.Email -> "Email"
    is QRContent.Phone -> "Phone"
    is QRContent.Sms -> "SMS"
    is QRContent.Contact -> "Contact"
    is QRContent.Location -> "Location"
}

private fun contentTypeIcon(content: QRContent): ImageVector = when (content) {
    is QRContent.Text -> Icons.Filled.TextFields
    is QRContent.Url -> Icons.Filled.Link
    is QRContent.WiFi -> Icons.Filled.Wifi
    is QRContent.Email -> Icons.Filled.Email
    is QRContent.Phone -> Icons.Filled.Phone
    is QRContent.Sms -> Icons.Filled.Sms
    is QRContent.Contact -> Icons.Filled.TextFields
    is QRContent.Location -> Icons.Filled.TextFields
}

private fun contentDisplayValue(content: QRContent): String = when (content) {
    is QRContent.Text -> content.value
    is QRContent.Url -> content.url
    is QRContent.WiFi -> buildString {
        appendLine("SSID: ${content.ssid}")
        append("Security: ${content.encryptionType.protocol}")
        if (content.hidden) append("\nHidden network")
    }
    is QRContent.Email -> buildString {
        appendLine(content.address)
        content.subject?.let { appendLine("Subject: $it") }
        content.body?.let { append("Body: $it") }
    }.trimEnd()
    is QRContent.Phone -> content.number
    is QRContent.Sms -> buildString {
        appendLine(content.number)
        content.message?.let { append(it) }
    }.trimEnd()
    is QRContent.Contact -> buildString {
        appendLine(content.name)
        content.phone?.let { appendLine("Phone: $it") }
        content.email?.let { appendLine("Email: $it") }
        content.organization?.let { append("Org: $it") }
    }.trimEnd()
    is QRContent.Location -> buildString {
        appendLine("${content.latitude}, ${content.longitude}")
        content.label?.let { append(it) }
    }.trimEnd()
}

private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "—"
    return dateFormat.format(Date(millis))
}

// --- Previews ---

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailPreview() {
    QRQanvasTheme {
        Detail(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            uiState = DetailUiState(
                isLoading = false,
                title = "My Website",
                content = QRContent.Url(url = "https://example.com"),
                createdAt = System.currentTimeMillis() - 86400000,
                modifiedAt = System.currentTimeMillis(),
            ),
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun DetailLoadingPreview() {
    QRQanvasTheme {
        Detail(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            uiState = DetailUiState(isLoading = true),
        )
    }
}

@Preview(name = "Delete Dialog", showBackground = true)
@Composable
private fun DetailDeleteDialogPreview() {
    QRQanvasTheme {
        Detail(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            uiState = DetailUiState(
                isLoading = false,
                title = "Test QR",
                content = QRContent.Text("Hello World"),
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                showDeleteDialog = true,
            ),
        )
    }
}
