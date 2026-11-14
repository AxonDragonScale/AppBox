package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

/** Everything the viewer can list, in the order the three home tabs show it. */
public data class StorageStores(
    val databases: List<DatabaseStore>,
    val preferences: List<PreferencesStore>,
    val roots: List<FileRoot>,
)

/** One SQLite file. [tableCount] and [userVersion] are null until a reader opens it. */
public data class DatabaseStore(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val tableCount: Int? = null,
    val userVersion: Int? = null,
)

public data class PreferencesStore(
    val name: String,
    val path: String,
    val kind: PreferencesKind,
    val sizeBytes: Long,
)

public enum class PreferencesKind { DATASTORE, SHARED_PREFERENCES }

/** A browsable directory. [path] is empty when the platform has no such directory. */
public data class FileRoot(
    val kind: FileRootKind,
    val label: String,
    val description: String,
    val path: String,
    val exists: Boolean,
    val childCount: Int,
)

public enum class FileRootKind { FILES, CACHE, NO_BACKUP, CACHE_EXTERNAL, CODE_CACHE }
