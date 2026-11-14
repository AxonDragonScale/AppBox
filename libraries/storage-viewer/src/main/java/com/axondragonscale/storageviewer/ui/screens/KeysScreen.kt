package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.BannerAction
import com.axondragonscale.storageviewer.ui.chrome.BannerActionVariant
import com.axondragonscale.storageviewer.ui.chrome.BannerTone
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageStatusBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.entriesMatching
import com.axondragonscale.storageviewer.ui.list.KeyRow
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val FilterGap = 8.dp
private val KeyRowHeight = 48.dp // design: rowList48
private const val SkeletonRows = 6

/**
 * One preference store's keys (design 15-17, 19): the store as the title, a filter over the loaded
 * keys, then the key rows. A row opens the value sheet.
 */
@Composable
internal fun KeysScreen(
    modifier: Modifier = Modifier,
    store: PreferencesStore,
    entries: DetailState<List<PreferenceEntry>>,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = store.name,
            subtitle = "Read-only",
            onBack = { onEvent(StorageEvent.Back) },
            onRefresh = { onEvent(StorageEvent.RefreshDetail) },
            onOverflow = null,
        )
        // The board's `View raw bytes` action is left out: the source has no raw-bytes read behind it.
        StorageSearchField(
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = FilterGap),
            query = query,
            placeholder = "Search this store",
            active = query.isNotBlank(),
            onQueryChange = { onEvent(StorageEvent.DetailQueryChange(it)) },
            onClear = { onEvent(StorageEvent.DetailQueryChange("")) },
        )
        when (entries) {
            DetailState.Loading -> LoadingContent()
            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StoreErrorBanner(
                    error = entries.error,
                    onRetry = { onEvent(StorageEvent.RefreshDetail) },
                )
            }

            is DetailState.Content -> KeysContent(
                entries = entries.value,
                query = query,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Keys", count = "…")
        SkeletonList(count = SkeletonRows, rowHeight = KeyRowHeight)
    }
}

@Composable
private fun KeysContent(
    entries: List<PreferenceEntry>,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    if (entries.isEmpty()) {
        StorageEmptyState(
            icon = Icons.Rounded.DataObject,
            title = "No keys found",
            body = "Nothing has been written to this store yet.",
        )
        return
    }
    val visible = entries.entriesMatching(query)
    if (visible.isEmpty()) {
        StorageEmptyState(
            icon = Icons.Rounded.Search,
            title = "No keys match \"$query\"",
            body = "Keys and values are searched. Case-insensitive.",
        )
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Keys", count = visible.size.toString())
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = ScreenPadding),
        ) {
            items(visible, key = { it.key }) { entry ->
                KeyRow(
                    entry = entry,
                    onClick = { onEvent(StorageEvent.SelectEntry(entry)) },
                )
            }
        }
    }
}

/**
 * A preference store's one failure statement: a writer's lock reads as `in use`, everything else as
 * a file the viewer could not read, with the exception only in Details.
 */
@Composable
private fun StoreErrorBanner(error: StorageError, onRetry: () -> Unit) {
    val busy = error is StorageError.Busy
    StorageStatusBanner(
        tone = if (busy) BannerTone.Busy else BannerTone.Error,
        title = if (busy) "Store is in use" else "Couldn't read this store",
        body = if (busy) {
            "Close the screen that is writing, then retry."
        } else {
            "The file could not be read."
        },
        details = (error as? StorageError.Unreadable)?.detail,
        action = BannerAction(
            label = "Retry",
            variant = BannerActionVariant.Secondary,
            onClick = onRetry,
        ),
    )
}

// region Presentation

// endregion
