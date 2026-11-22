package com.axondragonscale.logviewer

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.ui.LogScreen
import com.axondragonscale.logviewer.ui.LogUiEffect
import com.axondragonscale.logviewer.ui.LogUiEvent
import com.axondragonscale.logviewer.ui.LogViewModel
import kotlinx.coroutines.launch

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * A log console over any [LogSource]: logcat, a database, a file, a remote device.
 *
 * The list follows the newest records while it is at the top. Scroll away and it holds the row you
 * are reading; new records land above and the jump button offers the way back. Older records append
 * below as you reach the end. Pausing freezes the list and counts what arrives.
 *
 * It renders in the ambient `MaterialTheme`, so a host gives it the theme it wants (DevTools hosts
 * it inside `DevToolsTheme`).
 *
 * @param caption the status line's tail, e.g. "all buffers"; empty shows just "Live".
 */
@Composable
fun LogViewer(
    source: LogSource,
    modifier: Modifier = Modifier,
    caption: String = "",
    title: String = "Logs",
    onBack: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
    onCopy: ((String) -> Unit)? = null,
) {
    val viewModel: LogViewModel = viewModel(
        // Keyed by the source's own key, so two sources never share a viewer. A host whose source
        // dies with the screen scopes the store to the screen too (`LogcatScreen` does).
        key = "log-viewer:${source.key}",
        factory = LogViewModel.factory(source),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var shown by remember { mutableStateOf(emptyList<LogRecord>()) }
    var lastFeed by remember { mutableStateOf(emptyList<LogRecord>()) }
    var newCount by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var pendingCount by remember { mutableIntStateOf(0) }
    var wrap by remember { mutableStateOf(false) }
    val pausedNow by rememberUpdatedState(paused)

    // A closed viewer releases its stream but keeps its filters; a logcat source outlives it.
    LaunchedEffect(viewModel) { viewModel.start() }
    DisposableEffect(viewModel) { onDispose { viewModel.stop() } }

    // A filter change is a new view: follow the newest again.
    LaunchedEffect(state.query) {
        paused = false
        pendingCount = 0
    }

    fun apply(incoming: List<LogRecord>) {
        // Reverse layout: index 0 is the newest row, so this means the user is following the tail.
        val atNewest = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        val offset = listState.firstVisibleItemScrollOffset
        val anchorKey = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key
        val previous = shown
        shown = incoming

        val headMovedTo = newRowsAbove(previous, incoming)

        when {
            headMovedTo < 0 -> {
                newCount = 0
                listState.requestScrollToItem(0)
            }

            headMovedTo == 0 -> if (atNewest) newCount = 0

            atNewest && !listState.isScrollInProgress -> {
                newCount = 0
                listState.requestScrollToItem(0)
            }

            else -> {
                newCount += headMovedTo
                val index = anchorKey?.let { key -> incoming.indexOfFirst { it.id.value == key } } ?: -1
                if (index >= 0) listState.requestScrollToItem(index, offset)
            }
        }
    }

    // Read the viewport before applying the list: the only reliable way to know whether the user is
    // following the newest records or reading history.
    LaunchedEffect(viewModel) {
        viewModel.entries.collect { incoming ->
            if (pausedNow) {
                // Each delivery is counted against the previous one, so a bounded source that drops
                // its tail still counts; -1 means the whole list turned over (a clear, or more rows
                // than the buffer holds between two flushes).
                val arrivals = if (lastFeed.isEmpty()) incoming.size else newRowsAbove(lastFeed, incoming)
                pendingCount = if (arrivals >= 0) pendingCount + arrivals else incoming.size
            } else {
                apply(incoming)
            }
            lastFeed = incoming
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LogUiEffect.Share -> onShare?.invoke(effect.text)
                is LogUiEffect.Notify -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    fun resume() {
        paused = false
        // The last delivery, not a separate buffer: an empty one (a clear) must still reach the list.
        apply(lastFeed)
        pendingCount = 0
    }

    LogScreen(
        modifier = modifier,
        state = state,
        entries = shown,
        listState = listState,
        title = title,
        caption = caption,
        paused = paused,
        pendingCount = if (paused) pendingCount else newCount,
        wrap = wrap,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onTogglePause = {
            if (paused) resume() else paused = true
        },
        onToggleWrap = { wrap = it },
        onJumpToNewest = {
            if (paused) resume()
            newCount = 0
            scope.launch { listState.animateScrollToItem(0) }
        },
        onBack = onBack,
        onShare = onShare,
        onCopy = onCopy,
    )
}

/**
 * How many records arrived above the previous newest row; -1 when the two lists share no head, which
 * means the query produced something else entirely. Size and the tail are unreliable — a bounded
 * source drops its oldest row on every arrival once it is full.
 */
private fun newRowsAbove(previous: List<LogRecord>, incoming: List<LogRecord>): Int {
    val previousHead = previous.firstOrNull()?.id ?: return -1
    return incoming.indexOfFirst { it.id == previousHead }
}
