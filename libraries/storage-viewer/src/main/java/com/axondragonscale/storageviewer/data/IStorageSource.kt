package com.axondragonscale.storageviewer.data

import com.axondragonscale.commons.Result
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferencesStore
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.model.StorageStores
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema

/** Created by Ronak on 14/11/26 */

/** Rows returned when the caller does not ask for a smaller page. */
public const val ROW_LIMIT: Int = 500

/** Longest cell text the viewer keeps before marking it truncated. */
public const val CELL_BYTES: Int = 8 * 1024

/** Longest text preview a file open reads. */
public const val PREVIEW_BYTES: Int = 64 * 1024

/** Longest BLOB cell written into a hex preview. */
public const val BLOB_BYTES: Int = 4 * 1024

/**
 * Where the viewer reads from.
 *
 * Every method is read-only and runs against the live files, so a store can disappear or be
 * locked between a listing and an open; the failure comes back as [StorageError] instead of
 * throwing. The default [AndroidStorageSource] reads the calling app's own storage through
 * [android.content.Context].
 */
public interface IStorageSource {

    public suspend fun stores(): StorageStores

    public suspend fun describe(store: DatabaseStore): Result<DatabaseStore, StorageError>

    public suspend fun tables(store: DatabaseStore): Result<List<DatabaseTable>, StorageError>

    public suspend fun schema(store: DatabaseStore, table: String): Result<TableSchema, StorageError>

    public suspend fun rows(
        store: DatabaseStore,
        table: String,
        limit: Int = ROW_LIMIT,
    ): Result<TableRows, StorageError>

    public suspend fun entries(store: PreferencesStore): Result<List<PreferenceEntry>, StorageError>

    /** One directory's entries, every one of them: the surface filters nothing out. */
    public suspend fun children(
        root: FileRoot,
        path: String,
    ): Result<FolderListing, StorageError>

    public suspend fun preview(
        root: FileRoot,
        path: String,
        maxBytes: Int = PREVIEW_BYTES,
    ): Result<FilePreview, StorageError>
}
