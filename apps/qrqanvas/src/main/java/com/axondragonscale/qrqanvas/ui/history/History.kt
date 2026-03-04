package com.axondragonscale.qrqanvas.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import com.axondragonscale.commons.collectWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun History(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val viewModel: HistoryVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle one-shot effects
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is HistoryUiEffect.CopyToClipboard -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", effect.text))
                snackbarHostState.showSnackbar("Copied to clipboard")
            }

            is HistoryUiEffect.ShareText -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Share").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }

            is HistoryUiEffect.OpenUrl -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Cannot open this content")
                }
            }

            is HistoryUiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    History(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun History(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState = HistoryUiState(),
    onEvent: (HistoryUiEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Scan History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState.scanHistory.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                )
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Clear All") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteSweep,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEvent(HistoryUiEvent.ClearAll)
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.scanHistory.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            ScanHistoryList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                entries = uiState.scanHistory,
                expandedItemId = uiState.expandedItemId,
                onEvent = onEvent,
            )
        }
    }

    // Clear All confirmation dialog
    if (uiState.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(HistoryUiEvent.DismissClearAll) },
            title = { Text(text = "Clear Scan History") },
            text = { Text(text = "This will permanently delete all scan history entries. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onEvent(HistoryUiEvent.ConfirmClearAll) }) {
                    Text(
                        text = "Clear All",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HistoryUiEvent.DismissClearAll) }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Suppress("DEPRECATION")
@Composable
private fun ScanHistoryList(
    modifier: Modifier = Modifier,
    entries: List<ScanHistoryEntity>,
    expandedItemId: Long?,
    onEvent: (HistoryUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = entries,
            key = { it.id },
        ) { entity ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onEvent(HistoryUiEvent.DeleteItem(entity.id))
                        true
                    } else {
                        false
                    }
                },
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    SwipeDeleteBackground(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                    )
                },
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
            ) {
                ScanHistoryItem(
                    entity = entity,
                    isExpanded = expandedItemId == entity.id,
                    onClick = { onEvent(HistoryUiEvent.ItemClicked(entity.id)) },
                    onCopy = { onEvent(HistoryUiEvent.CopyItem(entity.rawValue)) },
                    onShare = { onEvent(HistoryUiEvent.ShareItem(entity.rawValue)) },
                    onOpen = { onEvent(HistoryUiEvent.OpenItem(entity.rawValue)) },
                )
            }
        }
    }
}

@Composable
private fun SwipeDeleteBackground(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            modifier = Modifier.padding(end = 20.dp),
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun ScanHistoryItem(
    modifier: Modifier = Modifier,
    entity: ScanHistoryEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
) {
    val content = entity.content

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick),
    ) {
        // Main row: icon, value, timestamp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = contentTypeIcon(content),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = contentTypeLabel(content),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = entity.rawValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = formatTimestamp(entity.scannedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        // Expanded actions
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Copy", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Share", style = MaterialTheme.typography.labelMedium)
                }

                if (isOpenable(content)) {
                    FilledTonalButton(
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = openLabel(content),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "No scan history yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Scanned QR codes will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Returns an icon representing the [QRContent] type.
 */
private fun contentTypeIcon(content: QRContent?): ImageVector = when (content) {
    is QRContent.Url -> Icons.Filled.Link
    is QRContent.WiFi -> Icons.Filled.Wifi
    is QRContent.Email -> Icons.Filled.Email
    is QRContent.Phone -> Icons.Filled.Phone
    is QRContent.Sms -> Icons.Filled.Sms
    is QRContent.Contact -> Icons.Filled.Person
    is QRContent.Location -> Icons.Filled.LocationOn
    is QRContent.Text -> Icons.Filled.TextFields
    null -> Icons.Filled.TextFields
}

/**
 * Returns a human-readable label for the [QRContent] type.
 */
private fun contentTypeLabel(content: QRContent?): String = when (content) {
    is QRContent.Url -> "URL"
    is QRContent.WiFi -> "Wi-Fi"
    is QRContent.Email -> "Email"
    is QRContent.Phone -> "Phone"
    is QRContent.Sms -> "SMS"
    is QRContent.Contact -> "Contact"
    is QRContent.Location -> "Location"
    is QRContent.Text -> "Text"
    null -> "Text"
}

/**
 * Whether the content type supports an "Open" action.
 */
private fun isOpenable(content: QRContent?): Boolean = when (content) {
    is QRContent.Url, is QRContent.Phone, is QRContent.Email,
    is QRContent.Sms, is QRContent.Location -> true
    else -> false
}

/**
 * Returns the label for the "Open" action button depending on content type.
 */
private fun openLabel(content: QRContent?): String = when (content) {
    is QRContent.Url -> "Open"
    is QRContent.Phone -> "Call"
    is QRContent.Email -> "Email"
    is QRContent.Sms -> "SMS"
    is QRContent.Location -> "Map"
    else -> "Open"
}

/**
 * Formats a timestamp into a human-readable date/time string.
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            "${month} ${localDateTime.date.day}"
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryEmptyPreview() {
    QRQanvasTheme {
        Surface {
            History()
        }
    }
}

@Preview(name = "Light Mode - List", showBackground = true)
@Preview(name = "Dark Mode - List", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryListPreview() {
    QRQanvasTheme {
        Surface {
            History(
                uiState = HistoryUiState(
                scanHistory = listOf(
                    ScanHistoryEntity(
                        id = 1,
                        rawValue = "https://example.com",
                        content = QRContent.Url(url = "https://example.com"),
                        scannedAt = System.currentTimeMillis() - 300_000,
                    ),
                    ScanHistoryEntity(
                        id = 2,
                        rawValue = "Hello World",
                        content = QRContent.Text(value = "Hello World"),
                        scannedAt = System.currentTimeMillis() - 7_200_000,
                    ),
                    ScanHistoryEntity(
                        id = 3,
                        rawValue = "tel:+1234567890",
                        content = QRContent.Phone(number = "+1234567890"),
                        scannedAt = System.currentTimeMillis() - 90_000_000,
                    ),
                ),
                expandedItemId = 1,
                ),
            )
        }
    }
}
