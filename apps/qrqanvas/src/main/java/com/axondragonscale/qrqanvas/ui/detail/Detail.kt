package com.axondragonscale.qrqanvas.ui.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.ui.common.QRPreview
import com.axondragonscale.qrqanvas.ui.common.QRPreviewStyle
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import com.axondragonscale.qrqanvas.util.displayValue
import com.axondragonscale.qrqanvas.util.typeIcon
import com.axondragonscale.qrqanvas.util.typeLabel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Instant

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
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is DetailUiEffect.NavigateToEditor -> onNavigateToEditor(effect.qrCodeId)
            is DetailUiEffect.NavigateBack -> onNavigateBack()
            is DetailUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
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
                    style = QRPreviewStyle.Loading,
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
private fun ContentSummary(
    modifier: Modifier = Modifier,
    content: QRContent?,
) {
    if (content == null) return

    val label = content.typeLabel()
    val icon = content.typeIcon()
    val value = content.displayValue()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = value,
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

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "—"
    val instant = Instant.fromEpochMilliseconds(millis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val day = localDateTime.date.day
    val year = localDateTime.year
    val hour = if (localDateTime.hour % 12 == 0) 12 else localDateTime.hour % 12
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val amPm = if (localDateTime.hour < 12) "AM" else "PM"
    return "$month $day, $year at $hour:$minute $amPm"
}

// --- Previews ---

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailPreview() {
    QRQanvasTheme {
        Surface {
            Detail(
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
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun DetailLoadingPreview() {
    QRQanvasTheme {
        Surface {
            Detail(
                uiState = DetailUiState(isLoading = true),
            )
        }
    }
}

@Preview(name = "Delete Dialog", showBackground = true)
@Composable
private fun DetailDeleteDialogPreview() {
    QRQanvasTheme {
        Surface {
            Detail(
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
}
