package com.axondragonscale.logviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.model.toLogLine
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.source.LogFacets
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogRule
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.source.LogStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Created by Ronak Harkhani on 11/22/26 */

private const val TEXT_DEBOUNCE_MS = 300L

internal data class LogUiState(
    val capabilities: LogCapabilities,
    val query: LogQuery = LogQuery(),
    val facets: LogFacets = LogFacets(),
    val hasOlder: Boolean = false,
    val hasLoaded: Boolean = false,
    val notice: String? = null,
)

internal sealed interface LogUiEvent {
    data class SetText(val text: String) : LogUiEvent
    data class SetLevel(val level: LogLevel, val enabled: Boolean) : LogUiEvent
    data class AddRule(val facet: LogFacet, val value: String, val include: Boolean) : LogUiEvent
    data class RemoveRule(val facet: LogFacet, val value: String) : LogUiEvent
    data object SelectAllLevels : LogUiEvent
    data object LoadOlder : LogUiEvent
    data object Clear : LogUiEvent
    data object Share : LogUiEvent
}

internal sealed interface LogUiEffect {
    data class Share(val text: String) : LogUiEffect
    data class Notify(val message: String) : LogUiEffect
}

private data class StreamStatus(val loaded: Boolean, val older: Boolean, val notice: String?)

internal class LogViewModel(private val source: LogSource) : ViewModel() {

    private val capabilities = source.capabilities

    /**
     * Kept apart from [uiState] on purpose: the screen reads its scroll position before this list
     * changes, so it must receive each list as its own emission.
     */
    private val _entries = MutableStateFlow<List<LogRecord>>(emptyList())
    val entries: StateFlow<List<LogRecord>> = _entries.asStateFlow()

    private val _uiState = MutableStateFlow(LogUiState(capabilities = capabilities))
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<LogUiEffect>()
    val uiEffect = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    private var stream: LogStream? = null
    private var streamJob: Job? = null
    private var textJob: Job? = null
    private var started = false

    /** Called when the viewer enters composition; a dialog that closes releases the source. */
    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            _uiState.update { it.copy(facets = source.facets()) }
        }
        restart()
    }

    /** Releases whatever the stream started, and keeps the filters for the next open. */
    fun stop() {
        started = false
        textJob?.cancel()
        streamJob?.cancel()
        stream?.close()
        stream = null
        _entries.value = emptyList()
        _uiState.update { it.copy(hasLoaded = false, hasOlder = false, notice = null) }
    }

    fun onEvent(event: LogUiEvent): Any = when (event) {
        is LogUiEvent.SetText -> {
            _uiState.update { it.copy(query = it.query.copy(text = event.text)) }
            textJob?.cancel()
            textJob = viewModelScope.launch {
                delay(TEXT_DEBOUNCE_MS)
                restart()
            }
        }

        is LogUiEvent.SetLevel -> {
            _uiState.update {
                val levels = if (event.enabled) it.query.levels + event.level else it.query.levels - event.level
                it.copy(query = it.query.copy(levels = levels))
            }
            restart()
        }

        is LogUiEvent.AddRule -> {
            _uiState.update {
                it.copy(query = it.query.withRule(LogRule(event.facet, event.value, event.include)))
            }
            restart()
        }

        is LogUiEvent.RemoveRule -> {
            _uiState.update { it.copy(query = it.query.withoutRule(event.facet, event.value)) }
            restart()
        }

        LogUiEvent.SelectAllLevels -> {
            _uiState.update { it.copy(query = it.query.copy(levels = LogLevel.entries.toSet())) }
            restart()
        }

        LogUiEvent.LoadOlder -> viewModelScope.launch { stream?.loadOlder() }
        LogUiEvent.Clear -> clear()
        LogUiEvent.Share -> share()
    }

    private fun restart() {
        if (!started) return
        textJob?.cancel()
        streamJob?.cancel()
        stream?.close()

        _entries.value = emptyList()
        _uiState.update { it.copy(hasOlder = false, hasLoaded = false, notice = null) }

        val next = source.open(_uiState.value.query)
        stream = next
        streamJob = viewModelScope.launch {
            launch {
                next.entries.collect { rows -> _entries.value = rows }
            }
            launch {
                combine(
                    next.isLoaded,
                    next.hasOlder,
                    next.notice,
                ) { loaded, older, notice -> StreamStatus(loaded, older && capabilities.history, notice) }
                    .collect { status ->
                        _uiState.update {
                            it.copy(
                                hasLoaded = status.loaded,
                                hasOlder = status.older,
                                notice = status.notice,
                            )
                        }
                    }
            }
        }
    }

    private fun clear() = viewModelScope.launch {
        stream?.clear()
        effectChannel.send(LogUiEffect.Notify("Logs cleared"))
    }

    private fun share() = viewModelScope.launch {
        val rows = _entries.value
        if (rows.isEmpty()) {
            effectChannel.send(LogUiEffect.Notify("Nothing to share"))
        } else {
            effectChannel.send(LogUiEffect.Share(rows.joinToString("\n") { it.toLogLine() }))
        }
    }

    override fun onCleared() = stop()

    companion object {
        fun factory(source: LogSource) = viewModelFactory {
            initializer { LogViewModel(source) }
        }
    }
}
