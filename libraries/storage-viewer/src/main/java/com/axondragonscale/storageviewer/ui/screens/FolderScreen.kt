package com.axondragonscale.storageviewer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.data.formatRelativeTime
import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.chrome.BannerAction
import com.axondragonscale.storageviewer.ui.chrome.BannerActionVariant
import com.axondragonscale.storageviewer.ui.chrome.BannerTone
import com.axondragonscale.storageviewer.ui.chrome.StoragePathLine
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageStatusBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.chrome.rootPath
import com.axondragonscale.storageviewer.ui.list.FileRow
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState

/** Created by Ronak on 14/11/26 */

private val ScreenPadding = 16.dp
private val FilterGap = 8.dp
private val FileRowHeight = 52.dp // design: rowFile52
private const val SkeletonRows = 6

/**
 * One folder inside a files root (design 20-22, 29): the path the reader is at, a filter over the
 * loaded listing, then folders and files. A folder opens one level deeper; a file opens its preview.
 */
@Composable
internal fun FolderScreen(
    modifier: Modifier = Modifier,
    root: FileRoot,
    path: String,
    listing: DetailState<FolderListing>,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    val shownPath = rootPath(root, path)
    Column(modifier = modifier.fillMaxSize()) {
        StorageTopBar(
            title = folderTitle(root = root, path = path),
            subtitle = "Read-only",
            onBack = { onEvent(StorageEvent.Back) },
            onRefresh = { onEvent(StorageEvent.RefreshDetail) },
            onOverflow = null,
        )
        StoragePathLine(
            path = shownPath,
            modifier = Modifier.padding(horizontal = ScreenPadding),
        )
        StorageSearchField(
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = FilterGap),
            query = query,
            placeholder = "Search files",
            active = query.isNotBlank(),
            onQueryChange = { onEvent(StorageEvent.DetailQueryChange(it)) },
            onClear = { onEvent(StorageEvent.DetailQueryChange("")) },
        )
        when (listing) {
            DetailState.Loading -> LoadingContent()
            is DetailState.Failed -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScreenPadding),
            ) {
                StorageStatusBanner(
                    tone = BannerTone.Error,
                    title = "Couldn't read this folder",
                    body = "It may have been cleared while you were looking.",
                    details = (listing.error as? StorageError.Unreadable)?.detail,
                    action = BannerAction(
                        label = "Retry",
                        variant = BannerActionVariant.Secondary,
                        onClick = { onEvent(StorageEvent.RefreshDetail) },
                    ),
                )
            }

            is DetailState.Content -> FolderContent(
                listing = listing.value,
                query = query,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        StorageSectionHeader(label = "Files", count = "…")
        SkeletonList(count = SkeletonRows, rowHeight = FileRowHeight)
    }
}

@Composable
private fun FolderContent(
    listing: FolderListing,
    query: String,
    onEvent: (StorageEvent) -> Unit,
) {
    val folders = listing.folders.matching(query)
    val files = listing.files.matching(query)
    val nothingVisible = folders.isEmpty() && files.isEmpty()
    when {
        nothingVisible && query.isNotBlank() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            StorageEmptyState(
                icon = Icons.Rounded.Search,
                title = "No files match \"$query\"",
                body = "Clear the search to show every entry in this folder.",
            )
        }

        nothingVisible -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            StorageEmptyState(
                icon = Icons.Filled.Folder,
                title = "This folder is empty",
                body = "Files appear here when the app writes to it.",
            )
        }

        else -> FolderSections(
            folders = folders,
            files = files,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun FolderSections(
    folders: List<FileNode>,
    files: List<FileNode>,
    onEvent: (StorageEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ScreenPadding),
    ) {
        if (folders.isNotEmpty()) {
            item(key = "folders-header") {
                StorageSectionHeader(label = "Folders", count = folders.size.toString())
            }
            items(folders, key = { "folder:${it.path}" }) { node ->
                FileRow(node = node, onClick = { onEvent(StorageEvent.OpenFolder(node)) })
            }
        }
        if (files.isNotEmpty()) {
            item(key = "files-header") {
                StorageSectionHeader(label = "Files", count = files.size.toString())
            }
            items(files, key = { "file:${it.path}" }) { node ->
                FileRow(
                    node = node,
                    meta = formatRelativeTime(node.lastModified),
                    onClick = { onEvent(StorageEvent.OpenFile(node)) },
                )
            }
        }
    }
}

// region Presentation

// The board's `Calculate size` action is omitted: recursive sizing is not implemented.
internal fun List<FileNode>.matching(query: String): List<FileNode> =
    if (query.isBlank()) this else filter { it.name.contains(query, ignoreCase = true) }

private fun folderTitle(root: FileRoot, path: String): String =
    if (path == root.path) root.label else path.trimEnd('/').substringAfterLast('/')

// endregion
