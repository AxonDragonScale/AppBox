package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.CellKind
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.SchemaColumn
import com.axondragonscale.storageviewer.model.TableRow
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.StorageErrorBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState
import com.axondragonscale.storageviewer.ui.matching
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val FilterGap = 8.dp
private val HeaderHeight = 40.dp // design: header40
private val RowHeight = 44.dp
private val Hairline = 1.dp
private val SelectedBar = 2.dp
private val CellPadding = 12.dp
private val MinColumnWidth = 88.dp
private val MaxColumnWidth = 240.dp
private val MinTableWidth = 380.dp

/** Mono 12 is 8 dp a character at this size; the padding is the cell's two 12 dp insets. */
private const val CharWidthDp = 8
private const val CellInsetsDp = 24
private const val MaxSampleChars = 24

/**
 * One table's records (design 11-13): a client-side filter over the loaded page, then a data grid.
 * The column header and the rows scroll sideways together; the header stays put vertically. A row
 * opens the sheet over this list.
 */
@Composable
internal fun RowsScreen(
    modifier: Modifier = Modifier,
    store: DatabaseStore,
    table: String,
    schema: DetailState<TableSchema>,
    rows: DetailState<TableRows>,
    query: String,
    selectedRowId: Int?,
    onEvent: (StorageEvent) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = table,
            subtitle = "${store.name} · Read-only",
            onBack = { onEvent(StorageEvent.Back) },
            onRefresh = { onEvent(StorageEvent.RefreshDetail) },
            onOverflow = null,
        )
        StorageSearchField(
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = FilterGap),
            query = query,
            placeholder = "Filter rows",
            active = query.isNotBlank(),
            onQueryChange = { onEvent(StorageEvent.DetailQueryChange(it)) },
            onClear = { onEvent(StorageEvent.DetailQueryChange("")) },
        )
        when (rows) {
            DetailState.Loading -> LoadingContent(columns = schema.columns())
            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StorageErrorBanner(
                    error = rows.error,
                    onRetry = { onEvent(StorageEvent.RefreshDetail) },
                )
            }

            is DetailState.Content -> RowsContent(
                columns = schema.columns(),
                rows = rows.value,
                query = query,
                selectedRowId = selectedRowId,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadingContent(columns: List<SchemaColumn>) {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Rows", count = "…")
        if (columns.isNotEmpty()) {
            val grid = remember(columns) { rowGrid(columns = columns, rows = emptyList()) }
            GridHeader(columns = columns, grid = grid)
        }
        SkeletonList(count = 4, rowHeight = RowHeight)
    }
}

@Composable
private fun RowsContent(
    columns: List<SchemaColumn>,
    rows: TableRows,
    query: String,
    selectedRowId: Int?,
    onEvent: (StorageEvent) -> Unit,
) {
    val visible = rows.matching(query)
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Rows", count = visible.size.toString())
        if (columns.isEmpty()) {
            StorageEmptyState(
                icon = Icons.Rounded.DataObject,
                title = "No columns found",
                body = "This table's columns could not be read, so its records have no grid.",
            )
        } else {
            RowsGrid(
                columns = columns,
                rows = rows,
                visible = visible,
                query = query,
                selectedRowId = selectedRowId,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun RowsGrid(
    columns: List<SchemaColumn>,
    rows: TableRows,
    visible: List<TableRow>,
    query: String,
    selectedRowId: Int?,
    onEvent: (StorageEvent) -> Unit,
) {
    val grid = remember(columns, rows.rows) { rowGrid(columns = columns, rows = rows.rows) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
    ) {
        GridHeader(columns = columns, grid = grid)
        when {
            rows.rows.isEmpty() -> GridMessage(
                width = grid.width,
                icon = Icons.Rounded.DataObject,
                title = "This table has no rows.",
                body = "Records appear here once the app writes to this table.",
            )

            visible.isEmpty() -> GridMessage(
                width = grid.width,
                icon = Icons.Rounded.SearchOff,
                title = "No rows match \"$query\"",
                body = "Clear the filter to show every row.",
            )

            else -> LazyColumn(
                modifier = Modifier
                    .width(grid.width)
                    .weight(1f),
                contentPadding = PaddingValues(bottom = ScreenPadding),
            ) {
                items(visible, key = { it.id }) { row ->
                    GridRow(
                        row = row,
                        grid = grid,
                        selected = row.id == selectedRowId,
                        onClick = { onEvent(StorageEvent.SelectRow(row)) },
                    )
                }
            }
        }
    }
}

/** The sticky header: one name per column, on the same widths the rows below repeat. */
@Composable
private fun GridHeader(columns: List<SchemaColumn>, grid: RowGrid) {
    val style = LocalStorageViewerStyle.current
    Column(modifier = Modifier.width(grid.width)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight),
        ) {
            columns.forEachIndexed { index, column ->
                GridCell(
                    text = column.name,
                    width = grid.widths[index],
                    style = style.monoChip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    endAligned = grid.endAligned[index],
                )
            }
        }
        GridDivider()
    }
}

/** One record: a cell per column, the selected one carrying the leading bar and the tint. */
@Composable
private fun GridRow(
    row: TableRow,
    grid: RowGrid,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalStorageViewerStyle.current
    val scheme = MaterialTheme.colorScheme
    val tint = if (selected) scheme.surfaceContainerHigh else Color.Transparent
    val bar = scheme.primary
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .background(tint)
                .drawBehind {
                    if (selected) drawRect(color = bar, size = Size(SelectedBar.toPx(), size.height))
                }
                .clickable(role = Role.Button, onClick = onClick),
        ) {
            grid.widths.forEachIndexed { index, width ->
                val cell = row.cells.getOrNull(index)
                GridCell(
                    text = cell?.display.orEmpty(),
                    width = width,
                    style = style.monoKey,
                    color = if (cell?.kind == CellKind.NULL) scheme.onSurfaceVariant else scheme.onSurface,
                    endAligned = grid.endAligned[index],
                )
            }
        }
        GridDivider()
    }
}

@Composable
private fun GridCell(
    text: String,
    width: Dp,
    style: TextStyle,
    color: Color,
    endAligned: Boolean,
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = CellPadding),
        contentAlignment = if (endAligned) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A state message held inside the grid's width, so it stays centred under its own header. */
@Composable
private fun ColumnScope.GridMessage(
    width: Dp,
    icon: ImageVector,
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier
            .width(width)
            .weight(1f),
    ) {
        StorageEmptyState(icon = icon, title = title, body = body)
    }
}

@Composable
private fun GridDivider() {
    HorizontalDivider(thickness = Hairline, color = MaterialTheme.colorScheme.outlineVariant)
}

// region Column widths

/** The grid's fixed geometry: one width and one alignment per column, and the whole table's width. */
private data class RowGrid(
    val widths: List<Dp>,
    val endAligned: List<Boolean>,
    val width: Dp,
)

/**
 * Widths come from the header and a sample of the loaded page, so a column is wide enough for the
 * data it holds without letting one long value push the rest off screen.
 */
private fun rowGrid(columns: List<SchemaColumn>, rows: List<TableRow>): RowGrid {
    val widths = columns.mapIndexed { index, column ->
        val longest = rows.maxOfOrNull { row -> row.cells.getOrNull(index)?.display?.length ?: 0 } ?: 0
        val chars = maxOf(column.name.length, longest.coerceAtMost(MaxSampleChars))
        (chars * CharWidthDp + CellInsetsDp).dp.coerceIn(MinColumnWidth, MaxColumnWidth)
    }
    val total = widths.fold(0.dp) { sum, width -> sum + width }
    return RowGrid(
        widths = widths,
        endAligned = columns.map { it.isNumeric() },
        width = maxOf(total, MinTableWidth),
    )
}

/** Numeric columns end-align, so figures line up on their last digit. */
private fun SchemaColumn.isNumeric(): Boolean =
    type.contains("INT", ignoreCase = true) || type.contains("REAL", ignoreCase = true)

// endregion

// region Presentation

private fun DetailState<TableSchema>.columns(): List<SchemaColumn> = when (this) {
    DetailState.Loading -> emptyList()
    is DetailState.Failed -> emptyList()
    is DetailState.Content -> value.columns
}

// endregion
