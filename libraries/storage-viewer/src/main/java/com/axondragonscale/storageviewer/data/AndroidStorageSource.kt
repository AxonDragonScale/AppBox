package com.axondragonscale.storageviewer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import com.axondragonscale.commons.Result
import com.axondragonscale.commons.catching
import com.axondragonscale.commons.mapFailure
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferencesKind
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.model.StorageStores
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Created by Ronak on 14/11/26 */

/** The default source: reads the calling app's own files in place, through [Context]. */
public class AndroidStorageSource(private val context: Context) : IStorageSource {

    private val appContext = context.applicationContext

    override suspend fun stores(): StorageStores = withContext(Dispatchers.IO) {
        StorageStores(
            databases = discoverDatabases(appContext),
            preferences = discoverPreferences(appContext),
            roots = discoverRoots(appContext),
        )
    }

    override suspend fun describe(store: DatabaseStore): Result<DatabaseStore, StorageError> =
        read { DatabaseReader.describe(store) }

    override suspend fun tables(store: DatabaseStore): Result<List<DatabaseTable>, StorageError> =
        read { DatabaseReader.tables(store) }

    override suspend fun schema(
        store: DatabaseStore,
        table: String,
    ): Result<TableSchema, StorageError> = read { DatabaseReader.schema(store, table) }

    override suspend fun rows(
        store: DatabaseStore,
        table: String,
        limit: Int,
    ): Result<TableRows, StorageError> = read { DatabaseReader.rows(store, table, limit) }

    override suspend fun entries(
        store: PreferencesStore,
    ): Result<List<PreferenceEntry>, StorageError> = read {
        when (store.kind) {
            PreferencesKind.DATASTORE -> DataStoreReader.entries(store.path)
            PreferencesKind.SHARED_PREFERENCES -> SharedPrefsReader.entries(store.path)
        }
    }

    override suspend fun children(
        root: FileRoot,
        path: String,
    ): Result<FolderListing, StorageError> = read { FileWalker.children(root, path) }

    override suspend fun preview(
        root: FileRoot,
        path: String,
        maxBytes: Int,
    ): Result<FilePreview, StorageError> = read { FileWalker.preview(root, path, maxBytes) }

    private suspend fun <T> read(block: suspend () -> T): Result<T, StorageError> =
        catching {
            withContext(Dispatchers.IO) { block() }
        }.mapFailure { error -> error.toStorageError() }
}

private fun Throwable.toStorageError(): StorageError = when (this) {
    is CancellationException -> throw this
    is SQLiteDatabaseLockedException -> StorageError.Busy
    is SQLiteException -> StorageError.Unreadable("${this::class.simpleName}: $message")
    else -> StorageError.Unreadable("${this::class.simpleName}: $message")
}
