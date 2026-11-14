package com.axondragonscale.storageviewer.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.DevToolsTheme
import com.axondragonscale.storageviewer.model.Cell
import com.axondragonscale.storageviewer.model.CellKind
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FileRootKind
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferenceType
import com.axondragonscale.storageviewer.model.PreferencesKind
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.PreviewKind
import com.axondragonscale.storageviewer.model.SchemaColumn
import com.axondragonscale.storageviewer.model.SchemaIndex
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.model.StorageStores
import com.axondragonscale.storageviewer.model.TableRow
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema
import com.axondragonscale.storageviewer.ui.DetailState
import com.axondragonscale.storageviewer.ui.Phase
import com.axondragonscale.storageviewer.ui.PreferenceHit
import com.axondragonscale.storageviewer.ui.StorageTab
import com.axondragonscale.storageviewer.ui.StorageUiState
import com.axondragonscale.storageviewer.ui.chrome.BannerAction
import com.axondragonscale.storageviewer.ui.chrome.BannerActionVariant
import com.axondragonscale.storageviewer.ui.chrome.BannerTone
import com.axondragonscale.storageviewer.ui.chrome.MetaChip
import com.axondragonscale.storageviewer.ui.chrome.SectionAction
import com.axondragonscale.storageviewer.ui.chrome.StatusPill
import com.axondragonscale.storageviewer.ui.chrome.StoragePathLine
import com.axondragonscale.storageviewer.ui.chrome.StorageSearchField
import com.axondragonscale.storageviewer.ui.chrome.StorageSectionHeader
import com.axondragonscale.storageviewer.ui.chrome.StorageSegmentedTabs
import com.axondragonscale.storageviewer.ui.chrome.StorageStatusBanner
import com.axondragonscale.storageviewer.ui.chrome.StorageTopBar
import com.axondragonscale.storageviewer.ui.chrome.TabSegment
import com.axondragonscale.storageviewer.ui.chrome.TypeChip
import com.axondragonscale.storageviewer.ui.chrome.kindColor
import com.axondragonscale.storageviewer.ui.chrome.preferenceColor
import com.axondragonscale.storageviewer.ui.chrome.rootPath
import com.axondragonscale.storageviewer.ui.list.FileRow
import com.axondragonscale.storageviewer.ui.list.KeyRow
import com.axondragonscale.storageviewer.ui.list.PreferenceResultRow
import com.axondragonscale.storageviewer.ui.list.SkeletonList
import com.axondragonscale.storageviewer.ui.list.StoreRow
import com.axondragonscale.storageviewer.ui.list.StorageEmptyState
import com.axondragonscale.storageviewer.ui.list.StorageTableRow
import com.axondragonscale.storageviewer.ui.screens.FilePreviewScreen
import com.axondragonscale.storageviewer.ui.screens.FolderScreen
import com.axondragonscale.storageviewer.ui.screens.KeysScreen
import com.axondragonscale.storageviewer.ui.screens.RowsScreen
import com.axondragonscale.storageviewer.ui.screens.SchemaScreen
import com.axondragonscale.storageviewer.ui.screens.StoresScreen
import com.axondragonscale.storageviewer.ui.screens.TablesScreen
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle
import com.axondragonscale.storageviewer.ui.style.rememberStorageViewerStyle

/** Created by Ronak on 14/11/26 */

@Composable
private fun StoragePreviewTheme(
    content: @Composable ColumnScope.() -> Unit,
) {
    DevToolsTheme {
        CompositionLocalProvider(LocalStorageViewerStyle provides rememberStorageViewerStyle()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    content = content,
                )
            }
        }
    }
}

private val PreviewDatabase = DatabaseStore(
    name = "devinator.db",
    path = "/data/user/0/com.axondragonscale.devinator/databases/devinator.db",
    sizeBytes = 1_240_000,
    tableCount = 12,
    userVersion = 7,
)

private val PreviewPreferences = PreferencesStore(
    name = "settings.preferences_pb",
    path = "/data/user/0/com.axondragonscale.devinator/files/datastore/settings.preferences_pb",
    kind = PreferencesKind.DATASTORE,
    sizeBytes = 4_210,
)

private val PreviewRow = TableRow(
    id = 4821,
    label = "Wireless debugging",
    labelKind = CellKind.TEXT,
    cells = listOf(
        Cell(column = "id", kind = CellKind.INTEGER, display = "4821", byteCount = 4, truncated = false),
        Cell(column = "label", kind = CellKind.TEXT, display = "Wireless debugging", byteCount = 18, truncated = false),
        Cell(column = "enabled", kind = CellKind.INTEGER, display = "1", byteCount = 1, truncated = false),
        Cell(column = "updated_at", kind = CellKind.NULL, display = "NULL", byteCount = 0, truncated = false),
        Cell(column = "payload", kind = CellKind.BLOB, display = "0x8f3a001c… 512 B", byteCount = 512, truncated = false),
    ),
)

private val PreviewKey = PreferenceEntry(
    key = "wireless_auto_off",
    type = PreferenceType.LONG,
    display = "1729517400000",
    byteCount = 8,
    truncated = false,
)

private const val PreviewModifiedAgoMs = 4 * 60_000L
private val PreviewModified = System.currentTimeMillis() - PreviewModifiedAgoMs

private val PreviewFolder = FileNode(
    name = "datastore",
    path = "/data/user/0/com.axondragonscale.devinator/files/datastore",
    isDirectory = true,
    sizeBytes = 0,
    lastModified = PreviewModified,
)

private val PreviewFile = FileNode(
    name = "settings.preferences_pb",
    path = "/data/user/0/com.axondragonscale.devinator/files/datastore/settings.preferences_pb",
    isDirectory = false,
    sizeBytes = 4_210,
    lastModified = PreviewModified,
)

private val PreviewSegments = listOf(
    TabSegment(label = "Databases", count = 3),
    TabSegment(label = "Preferences", count = 3),
    TabSegment(label = "Files", count = 5),
)

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreRowPreview() {
    StoragePreviewTheme {
        StoreRow(
            name = PreviewDatabase.name,
            meta = "12 tables · v7",
            trailing = "1.2 MB",
            color = LocalStorageViewerStyle.current.storeDatabase,
            icon = Icons.Filled.Storage,
            onClick = {},
        )
        StoreRow(
            name = PreviewPreferences.name,
            meta = "DataStore",
            trailing = "4.1 KB",
            color = LocalStorageViewerStyle.current.storePreferences,
            icon = Icons.Filled.Tune,
            onClick = {},
        )
        StoreRow(
            name = "files",
            meta = "/data/user/0/com.axondragonscale.devinator/files",
            trailing = "38 entries",
            color = LocalStorageViewerStyle.current.typeFiles,
            icon = Icons.Filled.Folder,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TableRowPreview() {
    StoragePreviewTheme {
        StorageTableRow(
            name = "tile_state",
            counts = "24 rows · 6 columns",
            icon = Icons.Filled.Inbox,
            onClick = {},
            onDdl = {},
            onRows = {},
        )
        StorageTableRow(
            name = "wireless_deadline",
            counts = "1 row · 3 columns",
            icon = Icons.Filled.Inbox,
            onClick = {},
            onDdl = {},
            onRows = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeyRowPreview() {
    StoragePreviewTheme {
        KeyRow(entry = PreviewKey, onClick = {})
        KeyRow(
            entry = PreferenceEntry(
                key = "wireless_enabled",
                type = PreferenceType.BOOL,
                display = "true",
                byteCount = 1,
                truncated = false,
            ),
            onClick = {},
        )
        KeyRow(
            entry = PreferenceEntry(
                key = "known_tiles",
                type = PreferenceType.STRING_SET,
                display = "[\"adb\", \"wireless\"]",
                byteCount = 24,
                truncated = false,
            ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileRowPreview() {
    StoragePreviewTheme {
        StoragePathLine(path = rootPath(PreviewRoot, PreviewFolder.path))
        FileRow(node = PreviewFolder, meta = "Modified 12 Nov · 3 entries", onClick = {})
        FileRow(node = PreviewFile, meta = "Modified 12 Nov · text", onClick = {})
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 420)
@Preview(
    showBackground = true,
    widthDp = 412,
    heightDp = 420,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EmptyStatePreview() {
    StoragePreviewTheme {
        StorageEmptyState(
            modifier = Modifier.height(380.dp),
            icon = Icons.Filled.SearchOff,
            title = "No databases match",
            body = "Nothing in this app's databases folder matches \"tile\".",
            helper = "Searched 3 database files.",
            action = {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable(role = Role.Button, onClick = {})
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Clear search",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BannerPreview() {
    StoragePreviewTheme {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StorageStatusBanner(
                tone = BannerTone.Warn,
                title = "This database is in use",
                body = "Close the app that holds it, then try again.",
                details = "android.database.sqlite.SQLiteDatabaseLockedException",
                action = BannerAction(
                    label = "Retry",
                    variant = BannerActionVariant.Primary,
                    onClick = {},
                ),
            )
            StorageStatusBanner(
                tone = BannerTone.Error,
                title = "Couldn't open this store",
                body = "The file is no longer readable. It may have been deleted.",
                action = BannerAction(
                    label = "Retry",
                    variant = BannerActionVariant.Secondary,
                    onClick = {},
                ),
            )
            StorageStatusBanner(
                tone = BannerTone.Busy,
                title = "Scanning storage",
                body = "Reading the databases folder.",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChipsPreview() {
    StoragePreviewTheme {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaChip(text = "24 rows")
                MetaChip(text = "v7")
                MetaChip(text = "NOT NULL")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(text = "TEXT", color = kindColor(CellKind.TEXT))
                TypeChip(text = "INTEGER", color = kindColor(CellKind.INTEGER))
                TypeChip(text = "NULL", color = kindColor(CellKind.NULL))
                TypeChip(text = "BLOB", color = kindColor(CellKind.BLOB))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(text = "SCANNING", color = MaterialTheme.colorScheme.primary)
                StatusPill(
                    text = "BOOL",
                    color = preferenceColor(PreferenceType.BOOL),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChromePreview() {
    StoragePreviewTheme {
        StorageTopBar(
            title = "Storage",
            subtitle = "Read-only",
            onBack = null,
            onRefresh = {},
            onOverflow = null,
        )
        StorageSegmentedTabs(
            modifier = Modifier.padding(horizontal = 16.dp),
            segments = PreviewSegments,
            selected = 1,
            onSelect = {},
        )
        StorageSearchField(
            modifier = Modifier.padding(horizontal = 16.dp),
            query = "preferences",
            placeholder = "Search preference stores",
            active = true,
            onQueryChange = {},
            onClear = {},
        )
        StorageSectionHeader(
            label = "DataStore",
            count = "3",
            action = SectionAction(label = "Show all", onClick = {}),
        )
        SkeletonList(count = 2, rowHeight = 56.dp)
    }
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopBarDetailPreview() {
    StoragePreviewTheme {
        StorageTopBar(
            title = "devinator.db",
            subtitle = "v7 · Read-only",
            onBack = {},
            onRefresh = null,
            onOverflow = {},
        )
    }
}

private val PreviewRoots = listOf(
    FileRoot(FileRootKind.FILES, "Files", "App-private files", "/data/user/0/com.axondragonscale.devinator/files", true, 38),
    FileRoot(FileRootKind.CACHE, "Cache", "Trimmed by the system", "/data/user/0/com.axondragonscale.devinator/cache", true, 12),
    FileRoot(FileRootKind.NO_BACKUP, "No-backup files", "Never backed up", "/data/user/0/com.axondragonscale.devinator/no_backup", true, 2),
    FileRoot(FileRootKind.CACHE_EXTERNAL, "Cache (external)", "May be absent", "", false, 0),
    FileRoot(FileRootKind.CODE_CACHE, "Code cache", "Compiled code", "/data/user/0/com.axondragonscale.devinator/code_cache", true, 4),
)

private val PreviewStores = StorageStores(
    databases = listOf(
        PreviewDatabase,
        PreviewDatabase.copy(
            name = "wallmatic.db",
            path = "/data/user/0/com.axondragonscale.devinator/databases/wallmatic.db",
            sizeBytes = 184_000,
            tableCount = 4,
            userVersion = 2,
        ),
        PreviewDatabase.copy(
            name = "topty.db",
            path = "/data/user/0/com.axondragonscale.devinator/databases/topty.db",
            sizeBytes = 12_400,
            tableCount = null,
            userVersion = null,
        ),
    ),
    preferences = listOf(
        PreviewPreferences,
        PreviewPreferences.copy(
            name = "tweaks",
            path = "/data/user/0/com.axondragonscale.devinator/files/datastore/tweaks.preferences_pb",
            sizeBytes = 812,
        ),
    ),
    roots = PreviewRoots,
)

@Composable
private fun StoresPreview(state: StorageUiState) {
    StoragePreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(820.dp),
        ) {
            StoresScreen(state = state, onEvent = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresDatabasesPreview() {
    StoresPreview(StorageUiState(stores = PreviewStores, phase = Phase.CONTENT))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresPreferencesPreview() {
    StoresPreview(
        StorageUiState(
            tab = StorageTab.PREFERENCES,
            stores = PreviewStores,
            phase = Phase.CONTENT,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresFilesPreview() {
    StoresPreview(
        StorageUiState(
            tab = StorageTab.FILES,
            stores = PreviewStores,
            phase = Phase.CONTENT,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresLoadingPreview() {
    StoresPreview(StorageUiState(phase = Phase.LOADING))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresEmptyPreview() {
    StoresPreview(
        StorageUiState(
            stores = PreviewStores.copy(databases = emptyList()),
            phase = Phase.EMPTY,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresNoMatchesPreview() {
    StoresPreview(
        StorageUiState(
            query = "tile",
            stores = PreviewStores,
            phase = Phase.NO_MATCHES,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresErrorPreview() {
    StoresPreview(
        StorageUiState(
            phase = Phase.ERROR,
            error = StorageError.Unreadable("FileNotFoundException: databases"),
        ),
    )
}

// region Database surfaces

private val PreviewTables = listOf(
    DatabaseTable(name = "tile_state", isView = false, rowCount = 24, columnCount = 6),
    DatabaseTable(name = "wireless_deadline", isView = false, rowCount = 1, columnCount = 3),
    DatabaseTable(name = "known_tiles", isView = true, rowCount = 12, columnCount = 4),
)

private val PreviewSchema = TableSchema(
    sql = "CREATE TABLE tile_state (id INTEGER PRIMARY KEY, label TEXT NOT NULL, " +
        "enabled INTEGER NOT NULL DEFAULT 1, updated_at INTEGER, payload BLOB)",
    columns = listOf(
        SchemaColumn(name = "id", type = "INTEGER", notNull = true, primaryKey = true, default = null),
        SchemaColumn(name = "label", type = "TEXT", notNull = true, primaryKey = false, default = null),
        SchemaColumn(name = "enabled", type = "INTEGER", notNull = true, primaryKey = false, default = "1"),
        SchemaColumn(name = "updated_at", type = "INTEGER", notNull = false, primaryKey = false, default = null),
        SchemaColumn(name = "payload", type = "BLOB", notNull = false, primaryKey = false, default = null),
    ),
    indexes = listOf(
        SchemaIndex(name = "index_tile_state_label", unique = true, columns = listOf("label")),
        SchemaIndex(name = "index_tile_state_updated_at", unique = false, columns = listOf("updated_at")),
    ),
)

private val PreviewRows = TableRows(
    rows = listOf(
        PreviewRow,
        PreviewRow.copy(
            id = 4822,
            label = "Bluetooth",
            cells = listOf(
                Cell("id", CellKind.INTEGER, "4822", 4, false),
                Cell("label", CellKind.TEXT, "Bluetooth", 9, false),
                Cell("enabled", CellKind.INTEGER, "0", 1, false),
                Cell("updated_at", CellKind.INTEGER, "1729517400000", 8, false),
                Cell("payload", CellKind.NULL, "NULL", 0, false),
            ),
        ),
    ),
    total = 24,
)

@Composable
private fun ScreenPreview(content: @Composable () -> Unit) {
    StoragePreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(915.dp),
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TablesContentPreview() {
    ScreenPreview {
        TablesScreen(
            store = PreviewDatabase,
            tables = DetailState.Content(PreviewTables),
            query = "",
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TablesEmptyPreview() {
    ScreenPreview {
        TablesScreen(
            store = PreviewDatabase,
            tables = DetailState.Content(emptyList()),
            query = "",
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TablesBusyPreview() {
    ScreenPreview {
        TablesScreen(
            store = PreviewDatabase,
            tables = DetailState.Failed(StorageError.Busy),
            query = "",
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SchemaContentPreview() {
    ScreenPreview {
        SchemaScreen(
            store = PreviewDatabase,
            table = "tile_state",
            schema = DetailState.Content(PreviewSchema),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RowsContentPreview() {
    ScreenPreview {
        RowsScreen(
            store = PreviewDatabase,
            table = "tile_state",
            schema = DetailState.Content(PreviewSchema),
            rows = DetailState.Content(PreviewRows),
            query = "",
            selectedRowId = PreviewRow.id,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RowsNoMatchesPreview() {
    ScreenPreview {
        RowsScreen(
            store = PreviewDatabase,
            table = "tile_state",
            schema = DetailState.Content(PreviewSchema),
            rows = DetailState.Content(PreviewRows),
            query = "vpn",
            selectedRowId = null,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RowsEmptyPreview() {
    ScreenPreview {
        RowsScreen(
            store = PreviewDatabase,
            table = "tile_state",
            schema = DetailState.Content(PreviewSchema),
            rows = DetailState.Content(TableRows(rows = emptyList(), total = 0)),
            query = "",
            selectedRowId = null,
            onEvent = {},
        )
    }
}

// endregion

// region Preferences surfaces

private val PreviewEntries = listOf(
    PreviewKey,
    PreferenceEntry(
        key = "wireless_enabled",
        type = PreferenceType.BOOL,
        display = "true",
        byteCount = 1,
        truncated = false,
    ),
    PreferenceEntry(
        key = "known_tiles",
        type = PreferenceType.STRING_SET,
        display = "[\"adb\", \"wireless\"]",
        byteCount = 24,
        truncated = false,
    ),
    PreferenceEntry(
        key = "notes",
        type = PreferenceType.STRING,
        display = "Enabled on the lab device after the 12 Nov rollout; keep until the next soak test.",
        byteCount = 512,
        truncated = true,
    ),
)

private val PreviewHits = listOf(
    PreferenceHit(store = PreviewPreferences, entry = PreviewKey),
    PreferenceHit(
        store = PreviewStores.preferences[1],
        entry = PreviewEntries[2],
    ),
)

@Composable
private fun KeysPreview(entries: DetailState<List<PreferenceEntry>>, query: String = "") {
    ScreenPreview {
        KeysScreen(
            store = PreviewPreferences,
            entries = entries,
            query = query,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeysContentPreview() {
    KeysPreview(DetailState.Content(PreviewEntries))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeysEmptyPreview() {
    KeysPreview(DetailState.Content(emptyList()))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeysNoMatchesPreview() {
    KeysPreview(DetailState.Content(PreviewEntries), query = "vpn")
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeysUnreadablePreview() {
    KeysPreview(DetailState.Failed(StorageError.Unreadable("InvalidProtocolBufferException: truncated input")))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeysBusyPreview() {
    KeysPreview(DetailState.Failed(StorageError.Busy))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresPreferencesSearchPreview() {
    StoresPreview(
        StorageUiState(
            tab = StorageTab.PREFERENCES,
            query = "wireless",
            stores = PreviewStores,
            phase = Phase.CONTENT,
            searchHits = DetailState.Content(PreviewHits),
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresPreferencesPartialSearchPreview() {
    StoresPreview(
        StorageUiState(
            tab = StorageTab.PREFERENCES,
            query = "wireless",
            stores = PreviewStores,
            phase = Phase.CONTENT,
            searchHits = DetailState.Content(PreviewHits),
            searchPartialFailure = true,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoresPreferencesSearchErrorPreview() {
    StoresPreview(
        StorageUiState(
            tab = StorageTab.PREFERENCES,
            query = "wireless",
            stores = PreviewStores,
            phase = Phase.ERROR,
            searchHits = DetailState.Failed(StorageError.Unreadable("IOException: shared_prefs")),
        ),
    )
}

@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreferenceResultRowPreview() {
    StoragePreviewTheme {
        PreviewHits.forEach { hit -> PreferenceResultRow(hit = hit, onClick = {}) }
    }
}

// endregion

// region Files surfaces

private val PreviewRoot = PreviewRoots.first()

private val PreviewListing = FolderListing(
    path = PreviewRoot.path,
    folders = listOf(
        PreviewFolder.copy(name = "exports", path = "${PreviewRoot.path}/exports"),
    ),
    files = listOf(
        PreviewFile.copy(name = "settings.preferences_pb", path = "${PreviewRoot.path}/settings.preferences_pb"),
        PreviewFile.copy(
            name = "session.log",
            path = "${PreviewRoot.path}/session.log",
            sizeBytes = 1_048_576,
        ),
    ),
)

private val PreviewHex = listOf(
    "00000000  7B 0A 20 20 22 77 69 72  |{.. \"wir|",
    "00000008  65 6C 65 73 73 22 3A 20  |eless\": |",
)

private val PreviewTextPreview = FilePreview(
    kind = PreviewKind.TEXT,
    text = "{\n  \"wireless_enabled\": true,\n  \"wireless_auto_off\": 1729517400000\n}",
    hexLines = PreviewHex,
    shownBytes = 68,
    totalBytes = 68,
    lastModified = PreviewModified,
)

private val PreviewBinaryPreview = FilePreview(
    kind = PreviewKind.BINARY,
    text = null,
    hexLines = PreviewHex,
    shownBytes = 16,
    totalBytes = 4_210,
    lastModified = PreviewModified,
)

private val PreviewTruncatedPreview = FilePreview(
    kind = PreviewKind.TRUNCATED,
    text = "{\n  \"wireless_enabled\": true,\n  \"wireless_auto_off\": 1729517400000\n}",
    hexLines = PreviewHex,
    shownBytes = 65_536,
    totalBytes = 262_144,
    lastModified = PreviewModified - 2 * 24 * 60 * 60 * 1000L,
)

@Composable
private fun FolderSurface(
    listing: DetailState<FolderListing>,
    query: String = "",
) {
    ScreenPreview {
        FolderScreen(
            root = PreviewRoot,
            path = PreviewRoot.path,
            listing = listing,
            query = query,
            onEvent = {},
        )
    }
}

@Composable
private fun FileSurface(preview: DetailState<FilePreview>, asHex: Boolean = false) {
    ScreenPreview {
        FilePreviewScreen(
            root = PreviewRoot,
            path = "${PreviewRoot.path}/settings.preferences_pb",
            preview = preview,
            asHex = asHex,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderContentPreview() {
    FolderSurface(DetailState.Content(PreviewListing))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderEmptyPreview() {
    FolderSurface(
        DetailState.Content(
            FolderListing(
                path = PreviewRoot.path,
                folders = emptyList(),
                files = emptyList(),
            ),
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderNoMatchesPreview() {
    FolderSurface(DetailState.Content(PreviewListing), query = "vpn")
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderUnreadablePreview() {
    FolderSurface(DetailState.Failed(StorageError.Unreadable("FileNotFoundException: files")))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileTextPreview() {
    FileSurface(DetailState.Content(PreviewTextPreview))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileHexPreview() {
    FileSurface(DetailState.Content(PreviewTextPreview), asHex = true)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileBinaryPreview() {
    FileSurface(DetailState.Content(PreviewBinaryPreview), asHex = true)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileTruncatedPreview() {
    FileSurface(DetailState.Content(PreviewTruncatedPreview))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileUnreadablePreview() {
    FileSurface(DetailState.Failed(StorageError.Unreadable("FileNotFoundException: settings.preferences_pb")))
}

// endregion
