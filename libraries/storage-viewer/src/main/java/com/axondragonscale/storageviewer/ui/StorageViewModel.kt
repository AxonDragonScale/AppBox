package com.axondragonscale.storageviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.axondragonscale.commons.Result
import com.axondragonscale.commons.catching
import com.axondragonscale.commons.fold
import com.axondragonscale.commons.getOrDefault
import com.axondragonscale.storageviewer.data.IStorageSource
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.model.StorageStores
import com.axondragonscale.storageviewer.model.TableRow
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Created by Ronak on 14/11/26 */

internal data class StorageUiState(
    val tab: StorageTab = StorageTab.DATABASES,
    val query: String = "",
    val stores: StorageStores? = null,
    val phase: Phase = Phase.LOADING,
    val error: StorageError? = null,
    val depth: Depth = Depth.Stores,
    val detailQuery: String = "",
    val tables: DetailState<List<DatabaseTable>> = DetailState.Loading,
    val schema: DetailState<TableSchema> = DetailState.Loading,
    val rows: DetailState<TableRows> = DetailState.Loading,
    val entries: DetailState<List<PreferenceEntry>> = DetailState.Loading,
    val searchHits: DetailState<List<PreferenceHit>> = DetailState.Loading,
    val searchPartialFailure: Boolean = false,
    val folder: DetailState<FolderListing> = DetailState.Loading,
    val preview: DetailState<FilePreview> = DetailState.Loading,
    val previewAsHex: Boolean = false,
    val sheetRow: TableRow? = null,
    val sheetEntry: PreferenceEntry? = null,
)

/** One cross-store search result: the entry and the store it was read from. */
internal data class PreferenceHit(val store: PreferencesStore, val entry: PreferenceEntry)

/** One cross-store search: the hits the readable stores returned, and whether any store failed. */
private data class SearchResult(
    val hits: DetailState<List<PreferenceHit>>,
    val partialFailure: Boolean = false,
)

/** How far below the store list the viewer is; each step is a screen, never an in-place swap. */
internal sealed interface Depth {
    data object Stores : Depth
    data class Tables(val store: DatabaseStore) : Depth
    data class Schema(val store: DatabaseStore, val table: String) : Depth
    data class Rows(val store: DatabaseStore, val table: String) : Depth
    data class Keys(val store: PreferencesStore) : Depth
    data class Folder(val root: FileRoot, val path: String) : Depth
    data class File(val root: FileRoot, val path: String) : Depth
}

/** The read behind one depth. Only the active depth's field holds a value, so no stale screen flashes. */
internal sealed interface DetailState<out T> {
    data object Loading : DetailState<Nothing>
    data class Content<T>(val value: T) : DetailState<T>
    data class Failed(val error: StorageError) : DetailState<Nothing>
}

internal enum class StorageTab { DATABASES, PREFERENCES, FILES }

internal enum class Phase { LOADING, CONTENT, EMPTY, NO_MATCHES, ERROR }

internal sealed interface StorageEvent {
    data class SelectTab(val tab: StorageTab) : StorageEvent
    data class QueryChange(val text: String) : StorageEvent
    data object ClearQuery : StorageEvent
    data object Refresh : StorageEvent
    data object Visible : StorageEvent
    data class OpenDatabase(val store: DatabaseStore) : StorageEvent
    data class OpenPreferences(val store: PreferencesStore) : StorageEvent
    data class OpenTable(val table: String) : StorageEvent
    data class OpenSchema(val table: String) : StorageEvent
    data class OpenRows(val table: String) : StorageEvent
    data class OpenRoot(val root: FileRoot) : StorageEvent
    data class OpenFolder(val node: FileNode) : StorageEvent
    data object UpOneFolder : StorageEvent
    data class OpenFile(val node: FileNode) : StorageEvent
    data object TogglePreviewMode : StorageEvent
    data object Back : StorageEvent
    data object RefreshDetail : StorageEvent
    data class DetailQueryChange(val text: String) : StorageEvent
    data class SelectRow(val row: TableRow) : StorageEvent
    data class SelectEntry(val entry: PreferenceEntry) : StorageEvent
    data object DismissSheet : StorageEvent
    data class CopyValue(val label: String, val text: String) : StorageEvent
}

internal sealed interface StorageEffect {
    data class Copy(val text: String, val label: String) : StorageEffect
    data class Message(val text: String) : StorageEffect
}

internal class StorageViewModel(private val source: IStorageSource) : ViewModel() {

    companion object {
        /** Wires the viewer without a DI framework. */
        fun factory(source: IStorageSource): ViewModelProvider.Factory = viewModelFactory {
            initializer { StorageViewModel(source) }
        }
    }

    private val _state = MutableStateFlow(StorageUiState())
    val state: StateFlow<StorageUiState> = _state.asStateFlow()

    private val effectChannel = Channel<StorageEffect>()
    val effects: Flow<StorageEffect> = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    private var loadJob: Job? = null
    private var detailJob: Job? = null
    private var searchJob: Job? = null
    private var visibleSeen = false

    init {
        load()
    }

    fun onEvent(event: StorageEvent): Any = when (event) {
        is StorageEvent.SelectTab -> selectTab(event.tab)
        is StorageEvent.QueryChange -> changeQuery(event.text)
        StorageEvent.ClearQuery -> changeQuery("")
        StorageEvent.Refresh -> load()
        StorageEvent.Visible -> showFirstVisible()
        is StorageEvent.OpenDatabase -> openTables(event.store)
        is StorageEvent.OpenPreferences -> openPreferences(event.store)
        is StorageEvent.OpenTable -> openSchema(event.table)
        is StorageEvent.OpenSchema -> openSchema(event.table)
        is StorageEvent.OpenRows -> openRows(event.table)
        is StorageEvent.OpenRoot -> openRoot(event.root)
        is StorageEvent.OpenFolder -> openFolder(event.node)
        StorageEvent.UpOneFolder -> upOneFolder()
        is StorageEvent.OpenFile -> openFile(event.node)
        StorageEvent.TogglePreviewMode -> update { it.copy(previewAsHex = !it.previewAsHex) }
        StorageEvent.Back -> goBack()
        StorageEvent.RefreshDetail -> refreshDetail()
        is StorageEvent.DetailQueryChange -> update { it.copy(detailQuery = event.text) }
        is StorageEvent.SelectRow -> update { it.copy(sheetRow = event.row) }
        is StorageEvent.SelectEntry -> update { it.copy(sheetEntry = event.entry) }
        StorageEvent.DismissSheet -> update { it.copy(sheetRow = null, sheetEntry = null) }
        is StorageEvent.CopyValue -> copyValue(event)
    }

    // region Loading

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            catching { source.stores() }.fold(
                onSuccess = { stores ->
                    update { it.copy(stores = stores, error = null) }
                    scheduleSearch()
                },
                onFailure = { failure ->
                    val detail = failure.message ?: failure::class.simpleName
                    update { it.copy(error = StorageError.Unreadable(detail)) }
                },
            )
        }
    }

    /** The first frame can beat the init scan; only load again when that scan is not running. */
    private fun showFirstVisible() {
        if (visibleSeen) return
        visibleSeen = true
        if (loadJob?.isActive != true) load()
    }

    // endregion

    // region Search

    private fun selectTab(tab: StorageTab) {
        update { it.copy(tab = tab) }
        scheduleSearch()
    }

    private fun changeQuery(text: String) {
        update { it.copy(query = text) }
        scheduleSearch()
    }

    /**
     * The cross-store search (design 18): two typed characters, a 250 ms settle, then one concurrent
     * read per store. Every keystroke, tab switch and rescan cancels the read it replaces.
     */
    private fun scheduleSearch() {
        searchJob?.cancel()
        val current = _state.value
        val query = current.query.trim()
        val due = current.tab == StorageTab.PREFERENCES && query.length >= MIN_SEARCH_CHARS
        // Nothing is read below the threshold, so no results stand in for a spinner that would never end.
        val hits = if (due) DetailState.Loading else DetailState.Content(emptyList<PreferenceHit>())
        update { it.copy(searchHits = hits, searchPartialFailure = false) }
        if (!due) return
        val stores = current.stores?.preferences.orEmpty()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val result = searchPreferences(stores, query)
            update { it.copy(searchHits = result.hits, searchPartialFailure = result.partialFailure) }
        }
    }

    /** One broken store must not fail the rest, but it is reported when it leaves nothing to read. */
    private suspend fun searchPreferences(
        stores: List<PreferencesStore>,
        query: String,
    ): SearchResult = coroutineScope {
        val reads = stores
            .map { store -> async { store to source.entries(store) } }
            .awaitAll()
        val errors = reads.mapNotNull { (_, read) -> read.getFailureResultOrNull() }
        val firstError = errors.firstOrNull()
        if (firstError != null && errors.size == reads.size) {
            return@coroutineScope SearchResult(hits = DetailState.Failed(firstError))
        }
        val hits = reads
            .mapNotNull { (store, read) -> read.getSuccessResultOrNull()?.let { store to it } }
            .flatMap { (store, entries) ->
                entries.asSequence()
                    .filter { it.key.matchesQuery(query) || it.display.matchesQuery(query) }
                    .take(HITS_PER_STORE)
                    .map { entry -> PreferenceHit(store, entry) }
                    .toList()
            }
            .sortedWith(compareBy({ it.store.name }, { it.entry.key }))
        SearchResult(hits = DetailState.Content(hits), partialFailure = errors.isNotEmpty())
    }

    // endregion

    // region Detail

    /** Navigation clears every other depth's value, so a screen always reads the depth it shows. */
    private fun openTables(store: DatabaseStore) {
        update {
            it.copy(
                depth = Depth.Tables(store),
                detailQuery = "",
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadTables(store)
    }

    private fun openPreferences(store: PreferencesStore) {
        update {
            it.copy(
                depth = Depth.Keys(store),
                detailQuery = "",
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadEntries(store)
    }

    private fun openSchema(table: String) {
        val store = _state.value.depth.store ?: return
        update {
            it.copy(
                depth = Depth.Schema(store, table),
                detailQuery = "",
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadSchema(store = store, table = table)
    }

    private fun openRows(table: String) {
        val store = _state.value.depth.store ?: return
        update {
            it.copy(
                depth = Depth.Rows(store, table),
                detailQuery = "",
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadRows(store = store, table = table)
    }

    private fun goBack() {
        when (val depth = _state.value.depth) {
            Depth.Stores -> Unit
            is Depth.Tables -> showStores()
            is Depth.Schema -> openTables(depth.store)
            is Depth.Rows -> openTables(depth.store)
            is Depth.Keys -> showStores()
            is Depth.Folder -> if (depth.path == depth.root.path) showStores() else upOneFolder()
            is Depth.File -> showFolder(depth.root, parentPath(depth.root, depth.path) ?: depth.root.path)
        }
    }

    private fun showStores() {
        detailJob?.cancel()
        update {
            it.copy(
                depth = Depth.Stores,
                detailQuery = "",
                previewAsHex = false,
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
    }

    private fun refreshDetail() {
        when (val depth = _state.value.depth) {
            Depth.Stores -> load()
            is Depth.Tables -> loadTables(depth.store)
            is Depth.Schema -> loadSchema(store = depth.store, table = depth.table)
            is Depth.Rows -> loadRows(store = depth.store, table = depth.table)
            is Depth.Keys -> loadEntries(depth.store)
            is Depth.Folder -> loadFolder(depth.root, depth.path)
            is Depth.File -> loadPreview(depth.root, depth.path)
        }
    }

    /** Describes the store first: the subtitle reads its version, which the home scan may not know. */
    private fun loadTables(store: DatabaseStore) {
        detailJob?.cancel()
        update { it.copy(tables = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            val described = source.describe(store).getOrDefault(store)
            source.tables(described).fold(
                onSuccess = { tables ->
                    update { state ->
                        val depth = state.depth
                        if (depth is Depth.Tables && depth.store.path == store.path) {
                            state.copy(depth = Depth.Tables(described), tables = DetailState.Content(tables))
                        } else {
                            state
                        }
                    }
                },
                onFailure = { error -> update { it.copy(tables = DetailState.Failed(error)) } },
            )
        }
    }

    private fun loadSchema(store: DatabaseStore, table: String) {
        detailJob?.cancel()
        update { it.copy(schema = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            source.schema(store, table).fold(
                onSuccess = { schema -> update { it.copy(schema = DetailState.Content(schema)) } },
                onFailure = { error -> update { it.copy(schema = DetailState.Failed(error)) } },
            )
        }
    }

    private fun loadRows(store: DatabaseStore, table: String) {
        detailJob?.cancel()
        update { it.copy(rows = DetailState.Loading, schema = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            // The grid's header names its columns, so both reads are needed for a table with no rows.
            val schemaRead = async { source.schema(store, table) }
            val rowsRead = async { source.rows(store, table) }
            val schema = schemaRead.await().toDetail()
            val rows = rowsRead.await().toDetail()
            update { state ->
                if (state.depth == Depth.Rows(store, table)) {
                    state.copy(schema = schema, rows = rows)
                } else {
                    state
                }
            }
        }
    }

    private fun loadEntries(store: PreferencesStore) {
        detailJob?.cancel()
        update { it.copy(entries = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            source.entries(store).fold(
                onSuccess = { entries -> update { it.copy(entries = DetailState.Content(entries)) } },
                onFailure = { error -> update { it.copy(entries = DetailState.Failed(error)) } },
            )
        }
    }

    private fun <T> Result<T, StorageError>.toDetail(): DetailState<T> = fold(
        onSuccess = { DetailState.Content(it) },
        onFailure = { DetailState.Failed(it) },
    )

    // region Files

    /** A root opens at its own path, listing everything it holds. */
    private fun openRoot(root: FileRoot) {
        update {
            it.copy(
                depth = Depth.Folder(root, root.path),
                previewAsHex = false,
                detailQuery = "",
                tables = DetailState.Loading,
                schema = DetailState.Loading,
                rows = DetailState.Loading,
                entries = DetailState.Loading,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadFolder(root, root.path)
    }

    private fun openFolder(node: FileNode) {
        val depth = _state.value.depth as? Depth.Folder ?: return
        showFolder(depth.root, node.path)
    }

    /** One step up, or back to the store list when the current folder is the root itself. */
    private fun upOneFolder() {
        val depth = _state.value.depth as? Depth.Folder ?: return
        val parent = parentPath(depth.root, depth.path)
        if (parent == null) showStores() else showFolder(depth.root, parent)
    }

    private fun openFile(node: FileNode) {
        val depth = _state.value.depth as? Depth.Folder ?: return
        update {
            it.copy(
                depth = Depth.File(depth.root, node.path),
                previewAsHex = false,
                detailQuery = "",
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadPreview(depth.root, node.path)
    }

    private fun showFolder(root: FileRoot, path: String) {
        update {
            it.copy(
                depth = Depth.Folder(root, path),
                detailQuery = "",
                previewAsHex = false,
                folder = DetailState.Loading,
                preview = DetailState.Loading,
                sheetRow = null,
                sheetEntry = null,
            )
        }
        loadFolder(root, path)
    }

    private fun loadFolder(root: FileRoot, path: String) {
        detailJob?.cancel()
        update { it.copy(folder = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            source.children(root, path).fold(
                onSuccess = { listing ->
                    update { state ->
                        if (state.depth == Depth.Folder(root, path)) {
                            state.copy(folder = DetailState.Content(listing))
                        } else {
                            state
                        }
                    }
                },
                onFailure = { error ->
                    update { state ->
                        if (state.depth == Depth.Folder(root, path)) {
                            state.copy(folder = DetailState.Failed(error))
                        } else {
                            state
                        }
                    }
                },
            )
        }
    }

    private fun loadPreview(root: FileRoot, path: String) {
        detailJob?.cancel()
        update { it.copy(preview = DetailState.Loading) }
        detailJob = viewModelScope.launch {
            source.preview(root, path).fold(
                onSuccess = { preview ->
                    update { state ->
                        if (state.depth == Depth.File(root, path)) {
                            state.copy(preview = DetailState.Content(preview))
                        } else {
                            state
                        }
                    }
                },
                onFailure = { error ->
                    update { state ->
                        if (state.depth == Depth.File(root, path)) {
                            state.copy(preview = DetailState.Failed(error))
                        } else {
                            state
                        }
                    }
                },
            )
        }
    }

    /** The directory above [path], or null when [path] is the root itself or above it. */
    private fun parentPath(root: FileRoot, path: String): String? {
        if (path == root.path) return null
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        return parent.takeIf { it.length >= root.path.length && it != path }
    }

    // endregion

    private fun copyValue(event: StorageEvent.CopyValue) = viewModelScope.launch {
        effectChannel.send(StorageEffect.Copy(text = event.text, label = event.label))
    }

    // endregion

    // region State

    private fun update(transform: (StorageUiState) -> StorageUiState) {
        _state.update { current ->
            transform(current).let { next -> next.copy(phase = next.phase()) }
        }
    }

    private fun StorageUiState.phase(): Phase = when {
        error != null -> Phase.ERROR
        stores == null -> Phase.LOADING
        searching -> searchPhase()
        visibleCount() > 0 -> Phase.CONTENT
        query.isBlank() -> Phase.EMPTY
        else -> Phase.NO_MATCHES
    }

    /** A search is pending, hits, no hits, or — when every store it read failed — a failed read. */
    private fun StorageUiState.searchPhase(): Phase = when (val hits = searchHits) {
        DetailState.Loading -> Phase.LOADING
        is DetailState.Failed -> Phase.ERROR
        is DetailState.Content -> if (hits.value.isEmpty()) Phase.NO_MATCHES else Phase.CONTENT
    }

    // endregion
}

// region Search

private const val MIN_SEARCH_CHARS = 2
private const val SEARCH_DEBOUNCE_MS = 250L

/** One store's ceiling, so a store with thousands of keys cannot crowd out the others. */
private const val HITS_PER_STORE = 50

// endregion

// region Matching

/**
 * The home search over preference keys only starts once the query can cross stores. Below the
 * threshold the tab is a store-name filter like the other two, so a one-character query is never
 * reported as a run that found nothing.
 */
internal val StorageUiState.searching: Boolean
    get() = tab == StorageTab.PREFERENCES && query.trim().length >= MIN_SEARCH_CHARS

internal fun StorageUiState.visibleCount(): Int {
    val stores = stores ?: return 0
    return when (tab) {
        StorageTab.DATABASES -> stores.databasesMatching(query).size
        StorageTab.PREFERENCES -> stores.preferencesMatching(query).size
        StorageTab.FILES -> stores.rootsMatching(query).size
    }
}

internal fun StorageStores.databasesMatching(query: String): List<DatabaseStore> =
    databases.filter { it.name.matchesQuery(query) }

internal fun StorageStores.preferencesMatching(query: String): List<PreferencesStore> =
    preferences.filter { it.name.matchesQuery(query) }

/** A root matches on either line: the label names it, the description says what it holds. */
internal fun StorageStores.rootsMatching(query: String): List<FileRoot> =
    roots.filter { it.label.matchesQuery(query) || it.description.matchesQuery(query) }

internal fun List<DatabaseTable>.matching(query: String): List<DatabaseTable> =
    filter { it.name.matchesQuery(query) }

/** A key matches on its name or on the value as shown, so a value search finds keys the name never names. */
internal fun List<PreferenceEntry>.entriesMatching(query: String): List<PreferenceEntry> =
    filter { it.key.matchesQuery(query) || it.display.matchesQuery(query) }

/** A row matches on its label or on any cell, so a value search finds rows the label never names. */
internal fun TableRows.matching(query: String): List<TableRow> =
    if (query.isBlank()) {
        rows
    } else {
        rows.filter { row -> row.label.matchesQuery(query) || row.cells.any { it.display.matchesQuery(query) } }
    }

private val Depth.store: DatabaseStore?
    get() = when (this) {
        Depth.Stores -> null
        is Depth.Tables -> store
        is Depth.Schema -> store
        is Depth.Rows -> store
        is Depth.Keys -> null
        is Depth.Folder -> null
        is Depth.File -> null
    }

private fun String.matchesQuery(query: String): Boolean =
    query.isBlank() || contains(query, ignoreCase = true)

// endregion
