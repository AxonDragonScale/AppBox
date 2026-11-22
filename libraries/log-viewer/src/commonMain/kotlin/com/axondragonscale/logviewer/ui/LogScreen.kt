package com.axondragonscale.logviewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.ui.chrome.LogTopBar
import com.axondragonscale.logviewer.ui.filter.LogFilterBar
import com.axondragonscale.logviewer.ui.list.LogEmptyState
import com.axondragonscale.logviewer.ui.list.LogEntryList
import com.axondragonscale.logviewer.ui.sheets.LogEntrySheet
import com.axondragonscale.logviewer.ui.sheets.LogFilterSheet
import com.axondragonscale.logviewer.ui.style.LogDivider

/** Created by Ronak Harkhani on 11/22/26 */

@Composable
internal fun LogScreen(
    modifier: Modifier = Modifier,
    state: LogUiState,
    entries: List<LogRecord>,
    listState: LazyListState,
    title: String,
    caption: String,
    paused: Boolean,
    pendingCount: Int,
    wrap: Boolean,
    snackbarHostState: SnackbarHostState,
    onEvent: (LogUiEvent) -> Unit,
    onLoadOlder: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleWrap: (Boolean) -> Unit,
    onJumpToNewest: () -> Unit,
    onBack: (() -> Unit)?,
    onShare: ((String) -> Unit)?,
    onCopy: ((String) -> Unit)?,
) {
    var selected by remember { mutableStateOf<LogRecord?>(null) }
    var filtersOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LogTopBar(
                    title = title,
                    caption = caption,
                    paused = paused,
                    pendingCount = pendingCount,
                    wrap = wrap,
                    canShare = onShare != null,
                    canClear = state.capabilities.clear,
                    onBack = onBack,
                    onTogglePause = onTogglePause,
                    onJumpToNewest = onJumpToNewest,
                    onToggleWrap = onToggleWrap,
                    onShare = { onEvent(LogUiEvent.Share) },
                    onClear = { confirmClear = true },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                LogFilterBar(
                    query = state.text,
                    levels = state.levels,
                    tags = state.tags,
                    excludedTags = state.excludedTags,
                    packages = state.packages,
                    excludedPackages = state.excludedPackages,
                    onQueryChange = { onEvent(LogUiEvent.SetText(it)) },
                    onToggleLevel = { level, enabled -> onEvent(LogUiEvent.SetLevel(level, enabled)) },
                    onAllLevels = { onEvent(LogUiEvent.SelectAllLevels) },
                    onRemoveTag = { onEvent(LogUiEvent.RemoveTag(it)) },
                    onRemovePackage = { onEvent(LogUiEvent.RemovePackage(it)) },
                    onOpenFilters = { filtersOpen = true },
                )

                LogDivider()

                state.notice?.let { notice ->
                    Text(
                        text = notice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        !state.hasLoaded -> LogEmptyState(
                            title = "Reading logs…",
                            body = "The source is opening.",
                            modifier = Modifier.align(Alignment.Center),
                        )

                        entries.isEmpty() && state.isFiltered -> LogEmptyState(
                            title = "No entries match the filter",
                            body = "Loosen a level, a tag or the search text.",
                            modifier = Modifier.align(Alignment.Center),
                        )

                        entries.isEmpty() -> LogEmptyState(
                            title = "No logs yet",
                            body = if (state.capabilities.live) {
                                "New records appear here as they are written. If nothing arrives, grant this app the Read Logs permission."
                            } else {
                                "This source has no records."
                            },
                            modifier = Modifier.align(Alignment.Center),
                        )

                        else -> LogEntryList(
                            listState = listState,
                            entries = entries,
                            hasOlder = state.hasOlder,
                            wrap = wrap,
                            onSelect = { selected = it },
                            onLoadOlder = onLoadOlder,
                        )
                    }
                }
            }
        }

        selected?.let { record ->
            LogEntrySheet(
                record = record,
                capabilities = state.capabilities,
                onDismiss = { selected = null },
                onCopy = onCopy,
                onShare = onShare,
                onIncludeTag = { onEvent(LogUiEvent.IncludeTag(it)) },
                onExcludeTag = { onEvent(LogUiEvent.ExcludeTag(it)) },
            )
        }

        if (filtersOpen) {
            LogFilterSheet(
                state = state,
                availableTags = remember(entries, state.availableTags) {
                    (state.availableTags + entries.map { it.tag }).distinct().sorted()
                },
                availablePackages = remember(entries) {
                    entries.mapNotNull { it.meta.packageName }.distinct().sorted()
                },
                lineCount = entries.size,
                onIncludeTag = { onEvent(LogUiEvent.IncludeTag(it)) },
                onExcludeTag = { onEvent(LogUiEvent.ExcludeTag(it)) },
                onRemoveTag = { onEvent(LogUiEvent.RemoveTag(it)) },
                onIncludePackage = { onEvent(LogUiEvent.IncludePackage(it)) },
                onExcludePackage = { onEvent(LogUiEvent.ExcludePackage(it)) },
                onRemovePackage = { onEvent(LogUiEvent.RemovePackage(it)) },
                onDismiss = { filtersOpen = false },
            )
        }

    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear logs?") },
            text = { Text("Every record at the source is deleted, including previous sessions.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onEvent(LogUiEvent.Clear)
                    }
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}
