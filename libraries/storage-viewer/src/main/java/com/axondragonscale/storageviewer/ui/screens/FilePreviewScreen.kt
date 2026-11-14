package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.data.formatBytes
import com.axondragonscale.storageviewer.data.formatRelativeTime
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.PreviewKind
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.BannerAction
import com.axondragonscale.storageviewer.ui.chrome.BannerActionVariant
import com.axondragonscale.storageviewer.ui.chrome.BannerTone
import com.axondragonscale.storageviewer.ui.chrome.SectionAction
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageStatusBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.chrome.rootPath
import com.axondragonscale.storageviewer.ui.list.SkeletonRow
import com.axondragonscale.storageviewer.ui.sheets.SheetAction
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val CardRadius = 12.dp // design: tile12
private val CardPadding = 16.dp
private val CardOutline = 1.dp
private val MetaGap = 8.dp
private val ActionGap = 8.dp
private val LoadingCardHeight = 160.dp

/**
 * One file's capped contents (design 23-25): the byte count, the truncation warning when the read
 * hit its cap, then a mono card the reader can select. Hex and text come from the same preview, so
 * the mode toggle never reads the file again.
 */
@Composable
internal fun FilePreviewScreen(
    modifier: Modifier = Modifier,
    root: FileRoot,
    path: String,
    preview: DetailState<FilePreview>,
    asHex: Boolean,
    onEvent: (StorageEvent) -> Unit,
) {
    val fileName = path.trimEnd('/').substringAfterLast('/')
    val shownPath = rootPath(root, path)
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = fileName,
            subtitle = shownPath,
            onBack = { onEvent(StorageEvent.Back) },
            onRefresh = { onEvent(StorageEvent.RefreshDetail) },
            onOverflow = null,
        )
        when (preview) {
            DetailState.Loading -> LoadingContent()
            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StorageStatusBanner(
                    tone = BannerTone.Error,
                    title = "Couldn't read this file",
                    body = "The file may have been deleted or is no longer readable.",
                    details = (preview.error as? StorageError.Unreadable)?.detail,
                    action = BannerAction(
                        label = "Retry",
                        variant = BannerActionVariant.Secondary,
                        onClick = { onEvent(StorageEvent.RefreshDetail) },
                    ),
                )
            }

            is DetailState.Content -> PreviewContent(
                fileName = fileName,
                preview = preview.value,
                asHex = asHex,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun PreviewContent(
    fileName: String,
    preview: FilePreview,
    asHex: Boolean,
    onEvent: (StorageEvent) -> Unit,
) {
    val style = LocalStorageViewerStyle.current
    val text = preview.text
    // A binary preview has no text, so it renders hex whether or not the toggle moved.
    val showHex = asHex || text == null
    val hexText = remember(preview.hexLines) { preview.hexLines.joinToString("\n") }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${formatBytes(preview.shownBytes.toLong())} of ${formatBytes(preview.totalBytes)}" +
                " · ${formatRelativeTime(preview.lastModified)}",
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = MetaGap),
            style = style.monoKey,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        if (preview.kind == PreviewKind.TRUNCATED) {
            StorageStatusBanner(
                modifier = Modifier.padding(horizontal = ScreenPadding),
                tone = BannerTone.Warn,
                title = "Large file",
                body = "Only the first ${formatBytes(preview.shownBytes.toLong())} is shown.",
            )
        }
        StorageSectionHeader(
            label = "Contents",
            action = if (text != null) {
                SectionAction(
                    label = if (asHex) "Show text" else "Show hex",
                    onClick = { onEvent(StorageEvent.TogglePreviewMode) },
                )
            } else {
                null
            },
        )
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding)
                .clip(RoundedCornerShape(CardRadius))
                .background(MaterialTheme.colorScheme.surface)
                .border(CardOutline, MaterialTheme.colorScheme.outline, RoundedCornerShape(CardRadius)),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(CardPadding),
            ) {
                SelectionContainer {
                    if (showHex) {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Text(
                                text = hexText,
                                style = style.monoValue,
                                color = MaterialTheme.colorScheme.onSurface,
                                softWrap = false,
                            )
                        }
                    } else {
                        Text(
                            text = text.orEmpty(),
                            style = style.monoValue,
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = true,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(ActionGap),
        ) {
            if (text != null) {
                SheetAction(
                    modifier = Modifier.weight(1f),
                    label = "Copy as hex",
                    primary = false,
                    onClick = { onEvent(StorageEvent.CopyValue(label = fileName, text = hexText)) },
                )
                SheetAction(
                    modifier = Modifier.weight(1f),
                    label = "Copy contents",
                    primary = true,
                    onClick = { onEvent(StorageEvent.CopyValue(label = fileName, text = text)) },
                )
            } else {
                SheetAction(
                    modifier = Modifier.weight(1f),
                    label = "Copy as hex",
                    primary = true,
                    onClick = { onEvent(StorageEvent.CopyValue(label = fileName, text = hexText)) },
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Contents", count = "…")
        SkeletonRow(
            modifier = Modifier.padding(horizontal = ScreenPadding),
            height = LoadingCardHeight,
        )
    }
}
