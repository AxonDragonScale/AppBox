package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.SchemaColumn
import com.axondragonscale.storageviewer.model.SchemaIndex
import com.axondragonscale.storageviewer.model.TableSchema
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.SectionAction
import com.axondragonscale.storageviewer.ui.chrome.StorageErrorBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val CardRadius = 12.dp // design: tile12
private val CardPadding = 16.dp
private val CardBorder = 1.dp
private val IndexHeight = 40.dp // design: header40
private val IndexGap = 8.dp
private val SkeletonRowHeight = 48.dp
private val Hairline = 1.dp

private val GridPadding = 12.dp
private val GridHeaderHeight = 40.dp // design: header40
private val GridRowHeight = 44.dp
private val NameWidth = 150.dp
private val TypeWidth = 96.dp
private val NullWidth = 64.dp
private val KeyWidth = 56.dp

private val ViewRowsHeight = 40.dp
private val ViewRowsRadius = 12.dp // design: tile12
private val ViewRowsIconSize = 18.dp
private val ViewRowsIconGap = 8.dp
private val SectionGap = 12.dp

/**
 * One table's definition (design 10): the DDL as it is stored, then the columns and the indexes.
 * The DDL card scrolls sideways rather than wrapping, so SQL keeps its shape.
 */
@Composable
internal fun SchemaScreen(
    modifier: Modifier = Modifier,
    store: DatabaseStore,
    table: String,
    schema: DetailState<TableSchema>,
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
        when (schema) {
            DetailState.Loading -> Column(modifier = Modifier.fillMaxSize()) {
                StorageSectionHeader(label = "Definition")
                SkeletonList(count = 3, rowHeight = SkeletonRowHeight)
            }

            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StorageErrorBanner(
                    error = schema.error,
                    onRetry = { onEvent(StorageEvent.RefreshDetail) },
                )
            }

            is DetailState.Content -> SchemaContent(
                schema = schema.value,
                table = table,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SchemaContent(
    schema: TableSchema,
    table: String,
    onEvent: (StorageEvent) -> Unit,
) {
    if (schema.columns.isEmpty()) {
        StorageEmptyState(
            icon = Icons.Rounded.Description,
            title = "No columns found",
            body = "This table has no readable columns. It may have been dropped after the list was read.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ScreenPadding),
    ) {
        item(key = "definition-header") {
            StorageSectionHeader(
                label = "Definition",
                action = schema.sql?.let { sql ->
                    SectionAction(label = "Copy DDL", onClick = { onEvent(StorageEvent.CopyValue("DDL", sql)) })
                },
            )
        }
        item(key = "definition") {
            DefinitionCard(sql = schema.sql ?: "No DDL is stored for this table.")
        }
        item(key = "view-rows") {
            ViewRowsButton(onClick = { onEvent(StorageEvent.OpenRows(table)) })
        }
        item(key = "columns-header") {
            StorageSectionHeader(label = "Columns", count = schema.columns.size.toString())
        }
        item(key = "columns-grid-header") {
            ColumnsHeader()
        }
        items(schema.columns, key = { "column-${it.name}" }) { column ->
            ColumnGridRow(column = column)
        }
        if (schema.indexes.isNotEmpty()) {
            item(key = "indexes-header") {
                StorageSectionHeader(label = "Indexes", count = schema.indexes.size.toString())
            }
            items(schema.indexes, key = { "index-${it.name}" }) { index ->
                IndexRow(index = index)
            }
        }
    }
}

@Composable
private fun DefinitionCard(sql: String) {
    val style = LocalStorageViewerStyle.current
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(CardRadius))
            .background(scheme.surface)
            .border(CardBorder, scheme.outline, RoundedCornerShape(CardRadius))
            .horizontalScroll(rememberScrollState())
            .padding(CardPadding),
    ) {
        SelectionContainer {
            Text(
                text = sql,
                style = style.monoValue,
                color = scheme.onSurface,
                softWrap = false,
            )
        }
    }
}

/** The one way into the records: a full-width action under the definition, not a text link. */
@Composable
private fun ViewRowsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = SectionGap)
            .height(ViewRowsHeight),
        shape = RoundedCornerShape(ViewRowsRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = ScreenPadding),
    ) {
        Text(text = "View rows", style = MaterialTheme.typography.labelLarge, maxLines = 1)
        Spacer(Modifier.width(ViewRowsIconGap))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(ViewRowsIconSize),
        )
    }
}

/** The column header for the schema grid; the same four widths every row below repeats. */
@Composable
private fun ColumnsHeader() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GridHeaderHeight)
                .padding(horizontal = GridPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GridHeading(text = "Name", width = NameWidth)
            GridHeading(text = "Type", width = TypeWidth)
            GridHeading(text = "Null", width = NullWidth)
            GridHeading(text = "Key", width = KeyWidth)
        }
        HorizontalDivider(thickness = Hairline, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun GridHeading(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = LocalStorageViewerStyle.current.monoChip,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** One column of the definition: name, declared type, nullability, and the key or default flag. */
@Composable
private fun ColumnGridRow(column: SchemaColumn) {
    val style = LocalStorageViewerStyle.current
    val scheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GridRowHeight)
                .padding(horizontal = GridPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = column.name,
                modifier = Modifier.width(NameWidth),
                style = style.monoIdentifier,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = column.type.ifBlank { "—" },
                modifier = Modifier.width(TypeWidth),
                style = style.monoKey,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (column.notNull) "NOT NULL" else "",
                modifier = Modifier.width(NullWidth),
                style = style.monoChip,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = column.keyFlags(),
                modifier = Modifier.width(KeyWidth),
                style = style.monoChip,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider(thickness = Hairline, color = scheme.outlineVariant)
    }
}

/** One index: its name, whether it is unique, and the columns it covers. */
@Composable
private fun IndexRow(index: SchemaIndex) {
    val style = LocalStorageViewerStyle.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = IndexHeight)
            .padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(IndexGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.name,
            modifier = Modifier.weight(1f),
            style = style.monoKey,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = index.meta(),
            style = style.monoChip,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// region Presentation

private fun SchemaColumn.keyFlags(): String = buildList {
    if (primaryKey) add("PK")
    default?.let { add("= $it") }
}.joinToString(separator = " ")

private fun SchemaIndex.meta(): String = buildList {
    if (unique) add("UNIQUE")
    add(columns.joinToString(separator = ", "))
}.joinToString(separator = " · ")

// endregion
