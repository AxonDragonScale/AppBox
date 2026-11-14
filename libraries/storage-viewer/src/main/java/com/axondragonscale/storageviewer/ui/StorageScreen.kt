package com.axondragonscale.storageviewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.axondragonscale.storageviewer.ui.screens.FilePreviewScreen
import com.axondragonscale.storageviewer.ui.screens.FolderScreen
import com.axondragonscale.storageviewer.ui.screens.KeysScreen
import com.axondragonscale.storageviewer.ui.screens.RowsScreen
import com.axondragonscale.storageviewer.ui.screens.SchemaScreen
import com.axondragonscale.storageviewer.ui.screens.StoresScreen
import com.axondragonscale.storageviewer.ui.screens.TablesScreen
import com.axondragonscale.storageviewer.ui.sheets.RowSheet
import com.axondragonscale.storageviewer.ui.sheets.ValueSheet

/** Created by Ronak on 14/11/26 */

/**
 * The viewer's ground: host theme, one surface, the snackbar the effects post to. Insets belong to
 * the host; the content keeps clear of both system bars, one wrapper for each side.
 *
 * Back dismisses the open sheet, then pops one depth, and only then leaves through [onBack], so a
 * deep surface never strands the reader above the store list it belongs to.
 */
@Composable
internal fun StorageScreen(
    modifier: Modifier = Modifier,
    state: StorageUiState,
    title: String,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)? = null,
    onEvent: (StorageEvent) -> Unit,
) {
    val sheetRow = state.sheetRow
    val sheetEntry = state.sheetEntry
    val sheetOpen = sheetRow != null || sheetEntry != null
    val inDetail = state.depth != Depth.Stores
    BackHandler(enabled = sheetOpen || inDetail || onBack != null) {
        when {
            sheetOpen -> onEvent(StorageEvent.DismissSheet)
            inDetail -> onEvent(StorageEvent.Back)
            else -> onBack?.invoke()
        }
    }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            val content = Modifier.statusBarsPadding().navigationBarsPadding()
            when (val depth = state.depth) {
                Depth.Stores -> StoresScreen(
                    modifier = content,
                    state = state,
                    title = title,
                    onEvent = onEvent,
                )

                is Depth.Tables -> TablesScreen(
                    modifier = content,
                    store = depth.store,
                    tables = state.tables,
                    query = state.detailQuery,
                    onEvent = onEvent,
                )

                is Depth.Schema -> SchemaScreen(
                    modifier = content,
                    store = depth.store,
                    table = depth.table,
                    schema = state.schema,
                    onEvent = onEvent,
                )

                is Depth.Rows -> RowsScreen(
                    modifier = content,
                    store = depth.store,
                    table = depth.table,
                    schema = state.schema,
                    rows = state.rows,
                    query = state.detailQuery,
                    selectedRowId = sheetRow?.id,
                    onEvent = onEvent,
                )

                is Depth.Keys -> KeysScreen(
                    modifier = content,
                    store = depth.store,
                    entries = state.entries,
                    query = state.detailQuery,
                    onEvent = onEvent,
                )

                is Depth.Folder -> FolderScreen(
                    modifier = content,
                    root = depth.root,
                    path = depth.path,
                    listing = state.folder,
                    query = state.detailQuery,
                    onEvent = onEvent,
                )

                is Depth.File -> FilePreviewScreen(
                    modifier = content,
                    root = depth.root,
                    path = depth.path,
                    preview = state.preview,
                    asHex = state.previewAsHex,
                    onEvent = onEvent,
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
            if (sheetRow != null) {
                RowSheet(row = sheetRow, table = state.tableName(), onEvent = onEvent)
            }
            if (sheetEntry != null) {
                ValueSheet(entry = sheetEntry, keyCount = state.keyCount(), onEvent = onEvent)
            }
        }
    }
}

/** The table the open sheet belongs to; the sheet only opens from a table surface. */
private fun StorageUiState.tableName(): String = when (val depth = depth) {
    is Depth.Rows -> depth.table
    is Depth.Schema -> depth.table
    else -> ""
}

/** The open store's key count, as the value sheet's meta line states it. */
private fun StorageUiState.keyCount(): Int = when (val loaded = entries) {
    is DetailState.Content -> loaded.value.size
    DetailState.Loading, is DetailState.Failed -> 0
}
