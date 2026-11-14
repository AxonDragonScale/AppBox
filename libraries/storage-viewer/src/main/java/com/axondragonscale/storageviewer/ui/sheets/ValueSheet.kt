package com.axondragonscale.storageviewer.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.storageviewer.data.formatBytes
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val BlockRadius = 12.dp // design: tile12
private val BlockPadding = 16.dp
private val LineGap = 8.dp
private val ActionGap = 8.dp
private val MaxSheetTitleSize = 18f

/**
 * One key's value (design `Sheet · Value · preferences`): the key and its meta, the value verbatim
 * in a mono block that scrolls both ways, then the two copy actions. A truncated value says what it lost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ValueSheet(
    modifier: Modifier = Modifier,
    entry: PreferenceEntry,
    keyCount: Int,
    onEvent: (StorageEvent) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = { onEvent(StorageEvent.DismissSheet) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        dragHandle = null,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.heightIn(max = sheetMaxHeight())) {
                SheetHeader(
                    entry = entry,
                    keyCount = keyCount,
                    onClose = { onEvent(StorageEvent.DismissSheet) },
                )
                ValueBlock(
                    modifier = Modifier.weight(1f, fill = false),
                    entry = entry,
                )
                if (entry.truncated) TruncationLine(entry = entry)
                SheetActions(entry = entry, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun SheetHeader(entry: PreferenceEntry, keyCount: Int, onClose: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetHandle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SheetPadding, top = SheetHandleGap, end = SheetPadding / 2, bottom = SheetPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Value · ${entry.key}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = MaxSheetTitleSize.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.meta(keyCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            SheetCloseButton(onClose = onClose)
        }
    }
}

/**
 * The value as the row holds it. A long line scrolls sideways rather than wrapping, and a multi-line
 * value scrolls down, capped by the parent's weight so the actions keep their place.
 */
@Composable
private fun ValueBlock(modifier: Modifier = Modifier, entry: PreferenceEntry) {
    val scheme = MaterialTheme.colorScheme
    val style = LocalStorageViewerStyle.current
    val shape = RoundedCornerShape(BlockRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SheetPadding)
            .clip(shape)
            .background(scheme.surface)
            .border(1.dp, scheme.outline, shape)
            .padding(BlockPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        SelectionContainer {
            Text(
                text = entry.display,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                style = style.monoValue,
                color = scheme.onSurface,
                softWrap = false,
            )
        }
    }
}

/** A capped value names what it kept and what it held, so the cut is never silent. */
@Composable
private fun TruncationLine(entry: PreferenceEntry) {
    Text(
        text = "First ${entry.display.toByteArray(Charsets.UTF_8).size} of ${entry.byteCount} bytes",
        modifier = Modifier.padding(start = SheetPadding, end = SheetPadding, top = LineGap),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun SheetActions(entry: PreferenceEntry, onEvent: (StorageEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SheetPadding),
        horizontalArrangement = Arrangement.spacedBy(ActionGap),
    ) {
        SheetAction(
            modifier = Modifier.weight(1f),
            label = "Copy key",
            primary = false,
            onClick = { onEvent(StorageEvent.CopyValue(label = "key", text = entry.key)) },
        )
        SheetAction(
            modifier = Modifier.weight(1f),
            label = "Copy value",
            primary = true,
            onClick = { onEvent(StorageEvent.CopyValue(label = "value", text = entry.display)) },
        )
    }
}

// region Presentation

private fun PreferenceEntry.meta(keyCount: Int): String =
    "${type.name} · ${formatBytes(byteCount.toLong())} · $keyCount keys"

// endregion
