package com.axondragonscale.logcatviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.axondragonscale.logcatviewer.data.ILogcatSource
import com.axondragonscale.logcatviewer.data.ISavedViewsStore
import com.axondragonscale.logcatviewer.data.LogcatPackageResolver
import com.axondragonscale.logcatviewer.model.FilterKind
import com.axondragonscale.logcatviewer.model.FilterRule
import com.axondragonscale.logcatviewer.model.LogBuffer
import com.axondragonscale.logcatviewer.model.LogEntry
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.model.LogLevel
import com.axondragonscale.logcatviewer.model.SavedView
import com.axondragonscale.logcatviewer.stream.LogSession
import com.axondragonscale.logcatviewer.stream.LogcatStreamer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/** Created by Ronak on 09/11/26 */

private const val PACKAGE_REFRESH_MS = 3_000L
private const val SHARE_LINES = 2_000

internal sealed interface LogcatUiEvent {
    data object Start : LogcatUiEvent
    data object Stop : LogcatUiEvent
    data object TogglePause : LogcatUiEvent
    data object ClearLog : LogcatUiEvent
    data object ToggleAutoscroll : LogcatUiEvent
    data object ToggleWrap : LogcatUiEvent
    data object SelectAllLevels : LogcatUiEvent
    data object ClearFilters : LogcatUiEvent
    data object Export : LogcatUiEvent
    data class SetLevel(val level: LogLevel, val enabled: Boolean) : LogcatUiEvent
    data class SetQuery(val query: String) : LogcatUiEvent
    data class SetRegex(val enabled: Boolean) : LogcatUiEvent
    data class SelectBuffer(val buffer: LogBuffer) : LogcatUiEvent
    data class AddRule(val rule: FilterRule) : LogcatUiEvent
    data class RemoveRule(val rule: FilterRule) : LogcatUiEvent
    data class SetSelectedTag(val include: Boolean) : LogcatUiEvent
    data class SelectEntry(val entry: LogEntry) : LogcatUiEvent
    data object ClearSelection : LogcatUiEvent
    data object ShareSelected : LogcatUiEvent
    data class SaveView(val name: String) : LogcatUiEvent
    data class RenameView(val id: Long, val name: String) : LogcatUiEvent
    data class DeleteView(val id: Long) : LogcatUiEvent
    data class ApplyView(val view: SavedView) : LogcatUiEvent
}

internal sealed interface LogcatUiEffect {
    data class ShowSnackbar(val message: String) : LogcatUiEffect
    data class Share(val text: String) : LogcatUiEffect
}

internal data class LogcatSelection(
    val entry: LogEntry,
    val packageName: String?,
)

internal data class LogcatUiState(
    val filter: LogFilter = LogFilter(),
    val entries: List<LogEntry> = emptyList(),
    val rawCount: Int = 0,
    val topBar: LogcatTopBarState = LogcatTopBarState(),
    val savedViews: List<SavedView> = emptyList(),
    val selected: LogcatSelection? = null,
)

/** What the bar shows; kept apart from the entries so a log batch does not rebuild the bar. */
internal data class LogcatTopBarState(
    val paused: Boolean = false,
    val pendingCount: Int = 0,
    val buffer: LogBuffer = LogBuffer.ALL,
    val autoscroll: Boolean = true,
    val wrap: Boolean = false,
)

/** PIDs currently writing for the package rules; a package the platform hides resolves to nothing. */
private data class ResolvedPackages(
    val included: Set<Int> = emptySet(),
    val excluded: Set<Int> = emptySet(),
)


internal class LogcatViewModel(
    source: ILogcatSource,
    private val savedViewsStore: ISavedViewsStore,
    private val packageResolver: LogcatPackageResolver,
) : ViewModel() {

    private val streamer = LogcatStreamer(source)
    private val filter = MutableStateFlow(LogFilter())
    private val buffer = MutableStateFlow(LogBuffer.ALL)
    private val autoscroll = MutableStateFlow(true)
    private val wrap = MutableStateFlow(false)
    private var session: LogSession? = null
    private var selectionJob: Job? = null

    private val _uiState = MutableStateFlow(LogcatUiState())
    val uiState = _uiState.asStateFlow()

    private val effectChannel = Channel<LogcatUiEffect>()
    val uiEffect = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        observeLogs()
        observePresentation()
        observeSavedViews()
    }

    fun onEvent(event: LogcatUiEvent): Any = when (event) {
        LogcatUiEvent.Start -> start()
        LogcatUiEvent.Stop -> stop()
        LogcatUiEvent.TogglePause -> streamer.setPaused(!streamer.isPaused.value)
        LogcatUiEvent.ClearLog -> streamer.clear()
        LogcatUiEvent.ToggleAutoscroll -> autoscroll.update { !it }
        LogcatUiEvent.ToggleWrap -> wrap.update { !it }
        LogcatUiEvent.SelectAllLevels -> filter.update { it.copy(levels = LogLevel.all) }
        LogcatUiEvent.ClearFilters -> filter.value = LogFilter()
        LogcatUiEvent.Export -> export()
        is LogcatUiEvent.SetLevel -> filter.update { it.withLevel(event.level, event.enabled) }
        is LogcatUiEvent.SetQuery -> filter.update { it.withQuery(event.query) }
        is LogcatUiEvent.SetRegex -> filter.update { it.withRegex(event.enabled) }
        is LogcatUiEvent.SelectBuffer -> selectBuffer(event.buffer)
        is LogcatUiEvent.AddRule -> filter.update { it.withRule(event.rule) }
        is LogcatUiEvent.RemoveRule -> filter.update { it.withoutRule(event.rule) }
        is LogcatUiEvent.SetSelectedTag -> setSelectedTag(event.include)
        is LogcatUiEvent.SelectEntry -> selectEntry(event.entry)
        LogcatUiEvent.ClearSelection -> clearSelection()
        LogcatUiEvent.ShareSelected -> shareSelected()
        is LogcatUiEvent.SaveView -> saveView(event.name)
        is LogcatUiEvent.RenameView -> renameView(event.id, event.name)
        is LogcatUiEvent.DeleteView -> deleteView(event.id)
        is LogcatUiEvent.ApplyView -> filter.value = event.view.filter
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    // region Log stream

    private fun start() {
        session = streamer.start(buffer.value)
    }

    private fun stop() {
        session?.let(streamer::stop)
        session = null
    }

    private fun selectBuffer(value: LogBuffer) {
        if (buffer.value == value) return
        buffer.value = value
        start()
    }

    private fun observeLogs() = viewModelScope.launch {
        combine(streamer.entries, appliedFilter()) { raw, (applied, packages) ->
            val query = applied.query.trim()
            val regex = if (applied.regex && query.isNotEmpty()) {
                runCatching { Regex(query, RegexOption.IGNORE_CASE) }.getOrNull()
            } else {
                null
            }
            Triple(applied, raw.filter { it.matches(applied, packages, query, regex) }, raw.size)
        }
            .flowOn(Dispatchers.Default)
            .collect { (applied, kept, rawCount) ->
                _uiState.update { it.copy(filter = applied, entries = kept, rawCount = rawCount) }
            }
    }

    /**
     * The filter together with the PIDs its package rules currently resolve to, at every emission:
     * a new rule is never evaluated against the previous resolution. Re-resolved every
     * [PACKAGE_REFRESH_MS] so an app restart stays in scope.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun appliedFilter(): Flow<Pair<LogFilter, ResolvedPackages>> = filter
        .transformLatest { applied ->
            val included = applied.includePackages
            val excluded = applied.excludePackages
            if (included.isEmpty() && excluded.isEmpty()) {
                emit(applied to ResolvedPackages())
            } else {
                while (true) {
                    emit(
                        applied to ResolvedPackages(
                            included = packageResolver.pidsFor(included),
                            excluded = packageResolver.pidsFor(excluded),
                        ),
                    )
                    delay(PACKAGE_REFRESH_MS)
                }
            }
        }
        .flowOn(Dispatchers.IO)

    private fun observePresentation() = viewModelScope.launch {
        combine(streamer.isPaused, streamer.pendingCount, buffer, autoscroll, wrap) { paused, pending, nextBuffer, auto, wrapLines ->
            LogcatTopBarState(paused, pending, nextBuffer, auto, wrapLines)
        }.collect { topBar -> _uiState.update { it.copy(topBar = topBar) } }
    }

    private fun observeSavedViews() = viewModelScope.launch {
        savedViewsStore.views.collect { views -> _uiState.update { it.copy(savedViews = views) } }
    }

    private fun LogEntry.matches(
        applied: LogFilter,
        packages: ResolvedPackages,
        query: String,
        regex: Regex?,
    ): Boolean {
        if (level !in applied.levels) return false
        if (applied.includeTags.isNotEmpty() && tag !in applied.includeTags) return false
        if (tag in applied.excludeTags) return false
        if (applied.includePackages.isNotEmpty() && pid !in packages.included) return false
        if (pid in packages.excluded) return false
        if (query.isEmpty()) return true
        return if (applied.regex) regex?.containsMatchIn(message) == true else message.contains(query, true)
    }

    // endregion

    // region Selection and views

    private fun selectEntry(entry: LogEntry) {
        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            val packageName = withContext(Dispatchers.IO) { packageResolver.packageForPid(entry.pid) }
            _uiState.update { it.copy(selected = LogcatSelection(entry, packageName)) }
        }
    }

    private fun clearSelection() {
        selectionJob?.cancel()
        _uiState.update { it.copy(selected = null) }
    }

    private fun shareSelected() {
        val entry = _uiState.value.selected?.entry ?: return
        viewModelScope.launch { effectChannel.send(LogcatUiEffect.Share(entry.asLine())) }
    }

    private fun setSelectedTag(include: Boolean) {
        val selected = _uiState.value.selected ?: return
        filter.update { it.withRule(FilterRule(FilterKind.TAG, selected.entry.tag, include)) }
        clearSelection()
    }

    private fun saveView(raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) return
        val saved = SavedView(
            id = Clock.System.now().toEpochMilliseconds(),
            name = name,
            filter = filter.value,
        )
        viewModelScope.launch {
            savedViewsStore.save(saved)
            effectChannel.send(LogcatUiEffect.ShowSnackbar("View saved."))
        }
    }

    private fun renameView(id: Long, raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            savedViewsStore.rename(id, name)
            effectChannel.send(LogcatUiEffect.ShowSnackbar("View renamed."))
        }
    }

    private fun deleteView(id: Long) = viewModelScope.launch {
        savedViewsStore.delete(id)
        effectChannel.send(LogcatUiEffect.ShowSnackbar("View deleted."))
    }

    private fun export() = viewModelScope.launch {
        val text = _uiState.value.entries.takeLast(SHARE_LINES).joinToString("\n") { it.asLine() }
        if (text.isNotBlank()) effectChannel.send(LogcatUiEffect.Share(text))
    }

    // endregion

    companion object {
        /** Wires the viewer without a DI framework. */
        internal fun factory(
            source: ILogcatSource,
            savedViewsStore: ISavedViewsStore,
            packageResolver: LogcatPackageResolver,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LogcatViewModel(
                    source = source,
                    savedViewsStore = savedViewsStore,
                    packageResolver = packageResolver,
                )
            }
        }
    }
}
