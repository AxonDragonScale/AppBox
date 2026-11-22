package com.axondragonscale.logviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.source.LogStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Created by Ronak Harkhani on 11/22/26 */

private const val TEXT_DEBOUNCE_MS = 300L

internal data class LogUiState(
    val capabilities: LogCapabilities,
    val levels: Set<LogLevel> = LogLevel.entries.toSet(),
    val text: String = "",
    val tags: Set<String> = emptySet(),
    val excludedTags: Set<String> = emptySet(),
    val packages: Set<String> = emptySet(),
    val excludedPackages: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val hasOlder: Boolean = false,
    val hasLoaded: Boolean = false,
    val notice: String? = null,
) {
    val isFiltered: Boolean
        get() = levels.size < LogLevel.entries.size ||
            text.isNotBlank() ||
            tags.isNotEmpty() ||
            excludedTags.isNotEmpty() ||
            packages.isNotEmpty() ||
            excludedPackages.isNotEmpty()
}

internal sealed interface LogUiEvent {
    data class SetText(val text: String) : LogUiEvent
    data class SetLevel(val level: LogLevel, val enabled: Boolean) : LogUiEvent
    data class IncludeTag(val tag: String) : LogUiEvent
    data class ExcludeTag(val tag: String) : LogUiEvent
    data class RemoveTag(val tag: String) : LogUiEvent
    data class IncludePackage(val packageName: String) : LogUiEvent
    data class ExcludePackage(val packageName: String) : LogUiEvent
    data class RemovePackage(val packageName: String) : LogUiEvent
    data object SelectAllLevels : LogUiEvent
    data object LoadOlder : LogUiEvent
    data object Clear : LogUiEvent
    data object Share : LogUiEvent
}

internal sealed interface LogUiEffect {
    data class Share(val text: String) : LogUiEffect
    data class Notify(val message: String) : LogUiEffect
}

internal class LogViewModel(private val source: LogSource) : ViewModel() {

    /**
     * Kept apart from [uiState] on purpose: the screen reads its scroll position before this list
     * changes, so it must receive each list as its own emission.
     */
    private val _entries = MutableStateFlow<List<LogRecord>>(emptyList())
    val entries: StateFlow<List<LogRecord>> = _entries.asStateFlow()

    private val _uiState = MutableStateFlow(LogUiState(capabilities = source.capabilities))
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<LogUiEffect>()
    val uiEffect = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    private var stream: LogStream? = null
    private var streamJob: Job? = null
    private var textJob: Job? = null
    private var loadingOlder = false
    private var started = false

    init {
        viewModelScope.launch {
            val tags = runCatching { source.tags() }.getOrDefault(emptyList())
            _uiState.update { it.copy(availableTags = tags) }
        }
    }

    /** Called when the viewer enters composition; a dialog that closes releases the source. */
    fun start() {
        if (started) return
        started = true
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
        _uiState.update { it.copy(hasLoaded = false, hasOlder = false) }
    }

    fun onEvent(event: LogUiEvent): Any = when (event) {
        is LogUiEvent.SetText -> {
            _uiState.update { it.copy(text = event.text) }
            textJob?.cancel()
            textJob = viewModelScope.launch {
                delay(TEXT_DEBOUNCE_MS)
                restart()
            }
        }

        is LogUiEvent.SetLevel -> {
            _uiState.update {
                val levels = if (event.enabled) it.levels + event.level else it.levels - event.level
                it.copy(levels = levels)
            }
            restart()
        }

        is LogUiEvent.IncludeTag -> {
            _uiState.update {
                it.copy(tags = it.tags + event.tag, excludedTags = it.excludedTags - event.tag)
            }
            restart()
        }

        is LogUiEvent.ExcludeTag -> {
            _uiState.update {
                it.copy(excludedTags = it.excludedTags + event.tag, tags = it.tags - event.tag)
            }
            restart()
        }

        is LogUiEvent.RemoveTag -> {
            _uiState.update { it.copy(tags = it.tags - event.tag, excludedTags = it.excludedTags - event.tag) }
            restart()
        }

        is LogUiEvent.IncludePackage -> {
            _uiState.update {
                it.copy(packages = it.packages + event.packageName, excludedPackages = it.excludedPackages - event.packageName)
            }
            restart()
        }

        is LogUiEvent.ExcludePackage -> {
            _uiState.update {
                it.copy(excludedPackages = it.excludedPackages + event.packageName, packages = it.packages - event.packageName)
            }
            restart()
        }

        is LogUiEvent.RemovePackage -> {
            _uiState.update {
                it.copy(packages = it.packages - event.packageName, excludedPackages = it.excludedPackages - event.packageName)
            }
            restart()
        }

        LogUiEvent.SelectAllLevels -> {
            _uiState.update { it.copy(levels = LogLevel.entries.toSet()) }
            restart()
        }

        LogUiEvent.LoadOlder -> loadOlder()
        LogUiEvent.Clear -> clear()
        LogUiEvent.Share -> share()
    }

    private fun restart() {
        if (!started) return
        textJob?.cancel()
        streamJob?.cancel()
        stream?.close()

        _entries.value = emptyList()
        _uiState.update { it.copy(hasOlder = false, hasLoaded = false) }

        val query = LogQuery(
            levels = _uiState.value.levels,
            text = _uiState.value.text,
            tags = _uiState.value.tags,
            excludedTags = _uiState.value.excludedTags,
            packages = _uiState.value.packages,
            excludedPackages = _uiState.value.excludedPackages,
        )
        val next = source.open(query)
        stream = next
        streamJob = viewModelScope.launch {
            launch {
                next.entries.collect { rows -> _entries.value = rows }
            }
            launch {
                next.isLoaded.collect { loaded -> _uiState.update { it.copy(hasLoaded = loaded) } }
            }
            launch {
                next.hasOlder.collect { older -> _uiState.update { it.copy(hasOlder = older) } }
            }
            next.notice?.let { notices ->
                launch {
                    notices.collect { notice -> _uiState.update { it.copy(notice = notice) } }
                }
            }
        }
    }

    private fun loadOlder() = viewModelScope.launch {
        val current = stream ?: return@launch
        if (loadingOlder || !_uiState.value.hasOlder) return@launch
        loadingOlder = true
        runCatching { current.loadOlder() }
        loadingOlder = false
    }

    private fun clear() = viewModelScope.launch {
        runCatching { stream?.clear() }
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
