package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.data.formatBytes
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.PreferencesKind
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.Phase
import com.axondragonscale.storageviewer.ui.PreferenceHit
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.StorageTab
import com.axondragonscale.storageviewer.ui.StorageUiState
import com.axondragonscale.storageviewer.ui.chrome.BannerAction
import com.axondragonscale.storageviewer.ui.chrome.BannerActionVariant
import com.axondragonscale.storageviewer.ui.chrome.BannerTone
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageSegmentedTabs
import com.axondragonscale.storageviewer.ui.chrome.StorageStatusBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.chrome.TabSegment
import com.axondragonscale.storageviewer.ui.databasesMatching
import com.axondragonscale.storageviewer.ui.list.FileRow
import com.axondragonscale.storageviewer.ui.list.PreferenceResultRow
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState
import com.axondragonscale.storageviewer.ui.list.StoreRow
import com.axondragonscale.storageviewer.ui.preferencesMatching
import com.axondragonscale.storageviewer.ui.rootsMatching
import com.axondragonscale.storageviewer.ui.searching
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val SearchGap = 8.dp
private val TabRowHeight = 56.dp // design: rowStore56, the height a placeholder borrows
private val SearchRowHeight = 64.dp // design: rowData64, the result row's height

/** Said without naming a store: the results stay useful even when one read fails. */
private const val PartialFailureNote = "Some stores could not be read."

/**
 * The viewer's home: three store tabs over one search field, then whichever state the active tab
 * is in. Root surface, so the top bar has no back.
 */
@Composable
internal fun StoresScreen(
    modifier: Modifier = Modifier,
    state: StorageUiState,
    title: String = "Storage",
    onEvent: (StorageEvent) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = title,
            subtitle = "Read-only",
            onBack = null,
            onRefresh = { onEvent(StorageEvent.Refresh) },
            onOverflow = null,
        )
        StorageSegmentedTabs(
            modifier = Modifier.padding(horizontal = ScreenPadding),
            segments = state.segments(),
            selected = state.tab.ordinal,
            onSelect = { index ->
                StorageTab.entries.getOrNull(index)?.let { onEvent(StorageEvent.SelectTab(it)) }
            },
        )
        StorageSearchField(
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = SearchGap),
            query = state.query,
            placeholder = state.tab.placeholder,
            active = state.query.isNotBlank(),
            onQueryChange = { onEvent(StorageEvent.QueryChange(it)) },
            onClear = { onEvent(StorageEvent.ClearQuery) },
        )
        when (state.phase) {
            Phase.LOADING -> LoadingContent(tab = state.tab, searching = state.searching)
            Phase.CONTENT -> Content(state = state, onEvent = onEvent)
            Phase.EMPTY -> EmptyContent(tab = state.tab)
            Phase.NO_MATCHES -> NoMatchesContent(state = state)
            Phase.ERROR -> ErrorContent(
                state = state,
                onRetry = { onEvent(StorageEvent.Refresh) },
            )
        }
    }
}

// region States

@Composable
private fun LoadingContent(tab: StorageTab, searching: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = if (searching) "Results" else tab.sectionLabel)
        SkeletonList(
            count = 3,
            rowHeight = if (searching) SearchRowHeight else TabRowHeight,
        )
    }
}

@Composable
private fun Content(state: StorageUiState, onEvent: (StorageEvent) -> Unit) {
    val stores = state.stores ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        when (state.tab) {
            StorageTab.DATABASES -> {
                StorageSectionHeader(label = state.tab.sectionLabel)
                DatabaseList(rows = stores.databasesMatching(state.query), onEvent = onEvent)
            }

            StorageTab.PREFERENCES -> if (state.searching) {
                val hits = (state.searchHits as? DetailState.Content)?.value.orEmpty()
                SearchResults(
                    hits = hits,
                    partialFailure = state.searchPartialFailure,
                    onEvent = onEvent,
                )
            } else {
                StorageSectionHeader(label = state.tab.sectionLabel)
                PreferenceList(rows = stores.preferencesMatching(state.query), onEvent = onEvent)
            }

            StorageTab.FILES -> {
                StorageSectionHeader(label = state.tab.sectionLabel)
                RootList(roots = stores.rootsMatching(state.query), onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun SearchResults(
    hits: List<PreferenceHit>,
    partialFailure: Boolean,
    onEvent: (StorageEvent) -> Unit,
) {
    StorageSectionHeader(label = "Results", count = hits.size.toString())
    if (partialFailure) PartialFailureText()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = listPadding()) {
        items(hits, key = { "${it.store.path}/${it.entry.key}" }) { hit ->
            PreferenceResultRow(
                hit = hit,
                onClick = { onEvent(StorageEvent.OpenPreferences(hit.store)) },
            )
        }
    }
}

@Composable
private fun PartialFailureText() {
    Text(
        text = PartialFailureNote,
        modifier = Modifier.padding(horizontal = ScreenPadding),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun EmptyContent(tab: StorageTab) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (tab) {
            StorageTab.DATABASES -> StorageEmptyState(
                icon = Icons.Rounded.Storage,
                title = "No databases found",
                body = "This app has not created a database yet. Room creates one the first time it writes.",
            )

            StorageTab.PREFERENCES -> StorageEmptyState(
                icon = Icons.Rounded.DataObject,
                title = "No preference stores found",
                body = "Nothing has been written to DataStore or SharedPreferences yet.",
            )

            // Five roots always exist, so the files tab can never be empty.
            StorageTab.FILES -> Unit
        }
    }
}

@Composable
private fun NoMatchesContent(state: StorageUiState) {
    val noun = when (state.tab) {
        StorageTab.DATABASES -> "database"
        StorageTab.PREFERENCES -> "preference store"
        StorageTab.FILES -> "file"
    }
    val body = if (state.searching) {
        "No keys or values match \"${state.query}\"."
    } else {
        "No $noun matches \"${state.query}\"."
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StorageEmptyState(
            icon = Icons.Filled.SearchOff,
            title = "No matches",
            body = body,
            helper = if (state.searching && state.searchPartialFailure) PartialFailureNote else null,
        )
    }
}

@Composable
private fun ErrorContent(state: StorageUiState, onRetry: () -> Unit) {
    // A search whose every read failed speaks for its own tab, not for the home scan.
    val searchError = if (state.searching) (state.searchHits as? DetailState.Failed)?.error else null
    val error = searchError ?: state.error
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
    ) {
        StorageStatusBanner(
            tone = BannerTone.Error,
            title = if (searchError != null) "Couldn't scan preferences" else "Couldn't scan storage",
            body = if (searchError != null) {
                "No preference store could be read."
            } else {
                "Storage could not be read."
            },
            details = error.details(),
            action = BannerAction(
                label = "Retry",
                variant = BannerActionVariant.Secondary,
                onClick = onRetry,
            ),
        )
    }
}

private fun StorageError?.details(): String? = when (this) {
    is StorageError.Unreadable -> detail
    StorageError.Busy -> "in use"
    null -> null
}

// endregion

// region Lists

@Composable
private fun DatabaseList(rows: List<DatabaseStore>, onEvent: (StorageEvent) -> Unit) {
    val style = LocalStorageViewerStyle.current
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = listPadding()) {
        items(rows, key = { it.path }) { store ->
            StoreRow(
                name = store.name,
                meta = store.meta(),
                trailing = formatBytes(store.sizeBytes),
                color = style.storeDatabase,
                icon = Icons.Rounded.Storage,
                onClick = { onEvent(StorageEvent.OpenDatabase(store)) },
            )
        }
    }
}

@Composable
private fun PreferenceList(rows: List<PreferencesStore>, onEvent: (StorageEvent) -> Unit) {
    val style = LocalStorageViewerStyle.current
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = listPadding()) {
        items(rows, key = { it.path }) { store ->
            StoreRow(
                name = store.name,
                meta = store.meta(),
                trailing = formatBytes(store.sizeBytes),
                color = style.storePreferences,
                icon = Icons.Rounded.DataObject,
                onClick = { onEvent(StorageEvent.OpenPreferences(store)) },
            )
        }
    }
}

@Composable
private fun RootList(roots: List<FileRoot>, onEvent: (StorageEvent) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = listPadding()) {
        items(roots, key = { it.kind }) { root ->
            FileRow(
                node = FileNode(
                    name = root.label,
                    path = root.path,
                    isDirectory = true,
                    sizeBytes = 0,
                ),
                meta = root.description,
                trailing = "${root.childCount} entries",
                icon = Icons.Filled.FolderOpen,
                onClick = { onEvent(StorageEvent.OpenRoot(root)) },
            )
        }
    }
}

private fun listPadding(): PaddingValues =
    PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding)

// endregion

// region Presentation

private fun StorageUiState.segments(): List<TabSegment> {
    // Counts describe what a tab holds, not what the query leaves visible; Files stays at five.
    val stores = stores
    return listOf(
        TabSegment(label = "Databases", count = stores?.databases?.size ?: 0),
        TabSegment(label = "Preferences", count = stores?.preferences?.size ?: 0),
        TabSegment(label = "Files", count = stores?.roots?.size ?: 0),
    )
}

private fun DatabaseStore.meta(): String = buildList {
    add("Database")
    tableCount?.let { add("$it ${if (it == 1) "table" else "tables"}") }
    userVersion?.let { add("v$it") }
}.joinToString(" · ")

private fun PreferencesStore.meta(): String = when (kind) {
    PreferencesKind.DATASTORE -> "DataStore"
    PreferencesKind.SHARED_PREFERENCES -> "SharedPreferences"
}

/** The Files tab lists roots rather than entries, so its section names what the rows are. */
private val StorageTab.sectionLabel: String
    get() = when (this) {
        StorageTab.DATABASES -> "Databases"
        StorageTab.PREFERENCES -> "Preferences"
        StorageTab.FILES -> "Locations"
    }

private val StorageTab.placeholder: String
    get() = when (this) {
        StorageTab.DATABASES -> "Search databases"
        StorageTab.PREFERENCES -> "Search preference stores"
        StorageTab.FILES -> "Search files"
    }

// endregion
