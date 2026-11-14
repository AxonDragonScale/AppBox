package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.StorageErrorBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState
import com.axondragonscale.storageviewer.ui.list.StorageTableRow
import com.axondragonscale.storageviewer.ui.matching

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val FilterGap = 8.dp
private val TableRowHeight = 48.dp // design: rowList48

/**
 * One database's tables (design 07-09): the store as the title, a search over the table names, then
 * the table rows. A table opens its schema.
 */
@Composable
internal fun TablesScreen(
    modifier: Modifier = Modifier,
    store: DatabaseStore,
    tables: DetailState<List<DatabaseTable>>,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = store.name,
            subtitle = store.subtitle,
            onBack = { onEvent(StorageEvent.Back) },
            onRefresh = { onEvent(StorageEvent.RefreshDetail) },
            onOverflow = null,
        )
        StorageSearchField(
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = FilterGap),
            query = query,
            placeholder = "Search tables",
            active = query.isNotBlank(),
            onQueryChange = { onEvent(StorageEvent.DetailQueryChange(it)) },
            onClear = { onEvent(StorageEvent.DetailQueryChange("")) },
        )
        when (tables) {
            DetailState.Loading -> LoadingContent()
            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StorageErrorBanner(
                    error = tables.error,
                    onRetry = { onEvent(StorageEvent.RefreshDetail) },
                )
            }

            is DetailState.Content -> TablesContent(
                tables = tables.value,
                query = query,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Tables", count = "…")
        SkeletonList(count = 4, rowHeight = TableRowHeight)
    }
}

@Composable
private fun TablesContent(
    tables: List<DatabaseTable>,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    if (tables.isEmpty()) {
        StorageEmptyState(
            icon = Icons.Rounded.Description,
            title = "No tables yet",
            body = "This database has no user tables. Room creates them on the first write.",
            helper = "SQLite system tables are hidden.",
        )
        return
    }
    val visible = tables.matching(query)
    if (visible.isEmpty()) {
        StorageEmptyState(
            icon = Icons.Rounded.SearchOff,
            title = "No tables match \"$query\"",
            body = "Clear the search to list every table.",
        )
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Tables", count = visible.size.toString())
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = ScreenPadding),
        ) {
            items(visible, key = { it.name }) { table ->
                StorageTableRow(
                    name = table.name,
                    counts = table.counts(),
                    icon = Icons.Rounded.Description,
                    onClick = { onEvent(StorageEvent.OpenTable(table.name)) },
                    onDdl = { onEvent(StorageEvent.OpenTable(table.name)) },
                    onRows = { onEvent(StorageEvent.OpenRows(table.name)) },
                )
            }
        }
    }
}

// region Presentation

/** `v7 · Read-only`; a store whose version could not be read drops that segment. */
internal val DatabaseStore.subtitle: String
    get() = userVersion?.let { "v$it · Read-only" } ?: "Read-only"

private fun DatabaseTable.counts(): String {
    val rows = if (rowCount < 0) "—" else countLabel(rowCount, "row")
    return "$rows · ${countLabel(columnCount.toLong(), "column")}"
}

internal fun countLabel(count: Long, noun: String): String =
    if (count == 1L) "1 $noun" else "$count ${noun}s"

// endregion
