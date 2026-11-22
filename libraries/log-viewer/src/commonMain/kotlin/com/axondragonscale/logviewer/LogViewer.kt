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
        // One ViewModel per source type, so the Logs and Logcat dialogs never share one.
        key = "log-viewer:${source::class.simpleName}",
        factory = LogViewModel.factory(source),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var shown by remember { mutableStateOf(emptyList<LogRecord>()) }
    var newCount by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var pendingCount by remember { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf(emptyList<LogRecord>()) }
    var wrap by remember { mutableStateOf(false) }
    val pausedNow by rememberUpdatedState(paused)

    // A closed viewer releases its stream but keeps its filters; a logcat source outlives it.
    LaunchedEffect(viewModel) { viewModel.start() }
    DisposableEffect(viewModel) { onDispose { viewModel.stop() } }

    // A filter change is a new view: follow the newest again.
    LaunchedEffect(
        state.levels,
        state.text,
        state.tags,
        state.excludedTags,
        state.packages,
        state.excludedPackages,
    ) {
        paused = false
        pendingCount = 0
        pending = emptyList()
    }

    fun apply(incoming: List<LogRecord>) {
        val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        val offset = listState.firstVisibleItemScrollOffset
        val anchorKey = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key
        val previous = shown

        val sameList = incoming.size == previous.size &&
            incoming.firstOrNull()?.id == previous.firstOrNull()?.id &&
            incoming.lastOrNull()?.id == previous.lastOrNull()?.id
        val prepended = previous.isNotEmpty() &&
            incoming.size > previous.size &&
            incoming.lastOrNull()?.id == previous.lastOrNull()?.id
        val appended = previous.isNotEmpty() &&
            incoming.size > previous.size &&
            incoming.firstOrNull()?.id == previous.firstOrNull()?.id

        shown = incoming

        when {
            sameList -> Unit

            prepended && atTop && !listState.isScrollInProgress -> {
                listState.requestScrollToItem(0)
                newCount = 0
            }

            prepended -> {
                newCount += (incoming.size - previous.size).coerceAtLeast(0)
                val index = anchorKey?.let { key -> incoming.indexOfFirst { it.id.value == key } } ?: -1
                if (index >= 0) listState.requestScrollToItem(index, offset)
            }

            appended -> Unit

            else -> {
                newCount = 0
                listState.requestScrollToItem(0)
            }
        }
    }

    // Read the viewport before applying the list: the only reliable way to know whether the user is
    // following the newest records or reading history.
    LaunchedEffect(viewModel) {
        viewModel.entries.collect { incoming ->
            if (pausedNow) {
                if (incoming.size > shown.size) pendingCount += incoming.size - shown.size
                pending = incoming
            } else {
                apply(incoming)
            }
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

    val resume = {
        paused = false
        if (pending.isNotEmpty()) apply(pending)
        pending = emptyList()
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
        onLoadOlder = { viewModel.onEvent(LogUiEvent.LoadOlder) },
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
