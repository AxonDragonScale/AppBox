package com.axondragonscale.logcatviewer.ui

import android.content.ClipData
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.FilterRule
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.model.SavedView
import com.axondragonscale.logcatviewer.ui.chrome.AddRuleDialog
import com.axondragonscale.logcatviewer.ui.chrome.LogcatDeleteDialog
import com.axondragonscale.logcatviewer.ui.chrome.LogcatTopBar
import com.axondragonscale.logcatviewer.ui.chrome.NameDialog
import com.axondragonscale.logcatviewer.ui.chrome.SaveViewDialog
import com.axondragonscale.logcatviewer.ui.filter.FilterDraft
import com.axondragonscale.logcatviewer.ui.filter.LogcatFilterBar
import com.axondragonscale.logcatviewer.ui.list.EmptyState
import com.axondragonscale.logcatviewer.ui.list.LogcatEntryList
import com.axondragonscale.logcatviewer.ui.list.PausedBanner
import com.axondragonscale.logcatviewer.ui.preview.LogcatPreviewTheme
import com.axondragonscale.logcatviewer.ui.preview.PreviewLogEntries
import com.axondragonscale.logcatviewer.ui.sheets.EntrySheet
import com.axondragonscale.logcatviewer.ui.sheets.FilterSheet
import com.axondragonscale.logcatviewer.ui.sheets.SavedViewsSheet
import com.axondragonscale.logcatviewer.ui.style.LogcatDivider
import kotlinx.coroutines.launch

/** Created by Ronak on 09/11/26 */

@Composable
internal fun LogcatScreen(
    modifier: Modifier = Modifier,
    uiState: LogcatUiState,
    title: String,
    snackbarHostState: SnackbarHostState,
    onEvent: (LogcatUiEvent) -> Unit,
    onBack: (() -> Unit)?,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val copy = rememberLogcatClipboard()

    var filtersOpen by remember { mutableStateOf(false) }
    var viewsOpen by remember { mutableStateOf(false) }
    var nameDialog by remember { mutableStateOf<NameDialog?>(null) }
    var deleteTarget by remember { mutableStateOf<SavedView?>(null) }
    var addFilter by remember { mutableStateOf<FilterDraft?>(null) }

    fun jumpToLatest() {
        scope.launch {
            val index = uiState.entries.lastIndex
            if (index >= 0) listState.scrollToItem(index)
        }
    }

    LaunchedEffect(uiState.topBar.paused, uiState.topBar.autoscroll, uiState.entries.lastOrNull()?.id) {
        // Follow only while the list is at the bottom, so scrolling up while live is not undone.
        if (!uiState.topBar.paused && uiState.topBar.autoscroll && !listState.canScrollForward) jumpToLatest()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { LogcatSnackbarHost(snackbarHostState) },
        topBar = {
            LogcatTopBar(
                state = uiState.topBar,
                title = title,
                onBack = onBack,
                onEvent = onEvent,
                onJump = ::jumpToLatest,
                onOpenSavedViews = { viewsOpen = true },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            LogcatFilterBar(
                filter = uiState.filter,
                onEvent = onEvent,
                onOpenFilters = { filtersOpen = true },
            )
            if (uiState.topBar.paused) {
                PausedBanner(
                    pending = uiState.topBar.pendingCount,
                    onResume = { onEvent(LogcatUiEvent.TogglePause) },
                )
            }
            LogcatDivider()
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.entries.isEmpty()) {
                    EmptyState(
                        rawCount = uiState.rawCount,
                        hasFilters = uiState.filter.hasFilters,
                        onClearFilters = { onEvent(LogcatUiEvent.ClearFilters) },
                    )
                } else {
                    LogcatEntryList(
                        listState = listState,
                        entries = uiState.entries,
                        wrap = uiState.topBar.wrap,
                        onSelect = { onEvent(LogcatUiEvent.SelectEntry(it)) },
                    )
                }
            }
        }
    }

    uiState.selected?.let { selection ->
        EntrySheet(
            selection = selection,
            onDismiss = { onEvent(LogcatUiEvent.ClearSelection) },
            onCopy = { copy("Log entry", selection.entry.asLine()) },
            onShare = { onEvent(LogcatUiEvent.ShareSelected) },
            onIncludeTag = { onEvent(LogcatUiEvent.SetSelectedTag(include = true)) },
            onExcludeTag = { onEvent(LogcatUiEvent.SetSelectedTag(include = false)) },
        )
    }

    if (filtersOpen) {
        FilterSheet(
            filter = uiState.filter,
            resultCount = uiState.entries.size,
            onQueryChange = { onEvent(LogcatUiEvent.SetQuery(it)) },
            onRegexChange = { onEvent(LogcatUiEvent.SetRegex(it)) },
            onRemoveRule = { onEvent(LogcatUiEvent.RemoveRule(it)) },
            onAddFilter = { addFilter = it },
            onClose = { filtersOpen = false },
        )
    }

    if (viewsOpen) {
        SavedViewsSheet(
            filter = uiState.filter,
            views = uiState.savedViews,
            onApply = {
                onEvent(LogcatUiEvent.ApplyView(it))
                viewsOpen = false
            },
            onSaveCurrent = {
                nameDialog = NameDialog.New("View ${uiState.savedViews.size + 1}")
            },
            onRename = { nameDialog = NameDialog.Rename(it) },
            onDelete = { deleteTarget = it },
            onClose = { viewsOpen = false },
        )
    }

    nameDialog?.let { dialog ->
        SaveViewDialog(
            dialog = dialog,
            onConfirm = { name ->
                when (dialog) {
                    is NameDialog.New -> onEvent(LogcatUiEvent.SaveView(name))
                    is NameDialog.Rename -> onEvent(LogcatUiEvent.RenameView(dialog.view.id, name))
                }
                nameDialog = null
            },
            onDismiss = { nameDialog = null },
        )
    }

    deleteTarget?.let { view ->
        LogcatDeleteDialog(
            viewName = view.name,
            onConfirm = {
                onEvent(LogcatUiEvent.DeleteView(view.id))
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }

    addFilter?.let { draft ->
        AddRuleDialog(
            draft = draft,
            onConfirm = { value ->
                onEvent(LogcatUiEvent.AddRule(FilterRule(draft.kind, value, draft.include)))
                addFilter = null
            },
            onDismiss = { addFilter = null },
        )
    }
}

@Composable
private fun LogcatSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(snackbarData = data, shape = RoundedCornerShape(12.dp))
    }
}

/**
 * Copies text and, below Android 13, confirms it with a toast. Android 13+ draws its own clipboard
 * confirmation, so a second one would be noise.
 */
@Composable
private fun rememberLogcatClipboard(): (label: String, text: String) -> Unit {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(clipboard, context, scope) {
        { label, text ->
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogcatPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogcatScreen(
                uiState = LogcatUiState(entries = PreviewLogEntries, rawCount = PreviewLogEntries.size),
                title = "Logcat",
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogcatIdlePreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogcatScreen(
                uiState = LogcatUiState(entries = emptyList(), rawCount = 0),
                title = "Logcat",
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogcatEmptyPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogcatScreen(
                uiState = LogcatUiState(
                    entries = emptyList(),
                    rawCount = 40,
                    filter = LogFilter(includePackages = setOf("com.axondragonscale.devinator")),
                ),
                title = "Logcat",
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogcatPausedPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogcatScreen(
                uiState = LogcatUiState(
                    entries = PreviewLogEntries,
                    rawCount = PreviewLogEntries.size,
                    topBar = LogcatTopBarState(paused = true, pendingCount = 3),
                ),
                title = "Logcat",
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {},
            )
        }
    }
}
