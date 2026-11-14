package com.axondragonscale.storageviewer.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.storageviewer.model.Cell
import com.axondragonscale.storageviewer.model.TableRow
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.TypeChip
import com.axondragonscale.storageviewer.ui.chrome.kindColor
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Created by Ronak on 14/11/26 */

private val KeyWidth = 150.dp // design: key column 150
private val TypeWidth = 64.dp // design: type column 64
private val CellGap = 8.dp
private val CellMinHeight = 48.dp
private val CellMaxLines = 3
private const val MaxSheetTitleSize = 18f

/**
 * One row, every cell (design `Sheet · Row detail`): the column grid of the list, held in a sheet
 * whose list scrolls under a capped height so the copy actions never leave the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RowSheet(
    modifier: Modifier = Modifier,
    row: TableRow,
    table: String,
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
                SheetHeader(row = row, table = table, onClose = { onEvent(StorageEvent.DismissSheet) })
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SheetPadding),
                ) {
                    row.cells.forEach { cell -> CellRow(cell = cell) }
                }
                SheetActions(row = row, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun SheetHeader(row: TableRow, table: String, onClose: () -> Unit) {
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
                    text = "Row ${row.id} · $table",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = MaxSheetTitleSize.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${row.cells.size} ${if (row.cells.size == 1) "column" else "columns"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            SheetCloseButton(onClose = onClose)
        }
    }
}

/** One cell on the list's column grid; a long value stops at three lines and stays selectable. */
@Composable
private fun CellRow(cell: Cell) {
    val style = LocalStorageViewerStyle.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CellMinHeight)
            .padding(vertical = CellGap),
        horizontalArrangement = Arrangement.spacedBy(CellGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = cell.column,
            modifier = Modifier.width(KeyWidth),
            style = style.monoKey,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.width(TypeWidth), contentAlignment = Alignment.CenterStart) {
            TypeChip(text = cell.kind.name, color = kindColor(cell.kind))
        }
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = cell.display,
                style = style.monoValue,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = CellMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SheetActions(row: TableRow, onEvent: (StorageEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SheetPadding),
        horizontalArrangement = Arrangement.spacedBy(CellGap),
    ) {
        SheetAction(
            modifier = Modifier.weight(1f),
            label = "Copy as TSV",
            primary = false,
            onClick = { onEvent(StorageEvent.CopyValue(label = "row", text = row.tsv())) },
        )
        SheetAction(
            modifier = Modifier.weight(1f),
            label = "Copy row as JSON",
            primary = true,
            onClick = { onEvent(StorageEvent.CopyValue(label = "row", text = row.json())) },
        )
    }
}

// region Copy

/** Header names, then displays, both tab separated; a tab or newline inside a value would break it. */
internal fun TableRow.tsv(): String = buildList {
    add(cells.joinToString(separator = "\t") { it.column })
    add(cells.joinToString(separator = "\t") { it.oneLineDisplay() })
}.joinToString(separator = "\n")

/** One object keyed by column, every value the display the viewer shows — a BLOB stays its hex. */
internal fun TableRow.json(): String = buildJsonObject {
    cells.forEach { cell -> put(cell.column, cell.display) }
}.toString()

private fun Cell.oneLineDisplay(): String = display.replace('\t', ' ').replace('\n', ' ')

// endregion
