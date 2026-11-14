package com.axondragonscale.storageviewer.data

import android.content.Context
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FileRootKind
import com.axondragonscale.storageviewer.model.PreferencesKind
import com.axondragonscale.storageviewer.model.PreferencesStore
import java.io.File
import java.io.IOException

/** Created by Ronak on 14/11/26 */

// region Discovery

private val DATABASE_SIDECARS = listOf("-wal", "-shm", "-journal", "-bak")
private const val SQLITE_INTERNAL_PREFIX = "sqlite_"
private const val DATASTORE_DIR = "datastore"
private const val DATASTORE_SUFFIX = ".preferences_pb"
private const val DATASTORE_TEMP_SUFFIX = ".tmp"
private const val SHARED_PREFS_DIR = "shared_prefs"
private const val SHARED_PREF_SUFFIX = ".xml"
private const val SHARED_PREF_BACKUP_SUFFIX = ".bak"

internal fun discoverDatabases(context: Context): List<DatabaseStore> =
    context.databaseList()
        .filterNot { name -> name.startsWith(SQLITE_INTERNAL_PREFIX) }
        .filterNot { name -> DATABASE_SIDECARS.any { suffix -> name.endsWith(suffix) } }
        .sorted()
        .mapNotNull { name ->
            val file = context.getDatabasePath(name)
            if (!file.isFile) return@mapNotNull null
            DatabaseStore(name = name, path = file.absolutePath, sizeBytes = file.length())
        }

internal fun discoverPreferences(context: Context): List<PreferencesStore> {
    val datastores = File(context.filesDir, DATASTORE_DIR)
        .listFiles()
        .orEmpty()
        .filter { file ->
            file.isFile &&
                file.name.endsWith(DATASTORE_SUFFIX) &&
                !file.name.endsWith(DATASTORE_TEMP_SUFFIX)
        }
        .sortedBy { file -> file.name }
        .map { file ->
            PreferencesStore(
                name = file.name.removeSuffix(DATASTORE_SUFFIX),
                path = file.absolutePath,
                kind = PreferencesKind.DATASTORE,
                sizeBytes = file.length(),
            )
        }

    val sharedPreferences = File(context.dataDir, SHARED_PREFS_DIR)
        .listFiles()
        .orEmpty()
        .filter { file ->
            file.isFile &&
                file.name.endsWith(SHARED_PREF_SUFFIX) &&
                !file.name.endsWith(SHARED_PREF_BACKUP_SUFFIX)
        }
        .sortedBy { file -> file.name }
        .map { file ->
            PreferencesStore(
                name = file.name.removeSuffix(SHARED_PREF_SUFFIX),
                path = file.absolutePath,
                kind = PreferencesKind.SHARED_PREFERENCES,
                sizeBytes = file.length(),
            )
        }

    return datastores + sharedPreferences
}

internal fun discoverRoots(context: Context): List<FileRoot> = listOf(
    fileRoot(FileRootKind.FILES, "Files", "App-private files", context.filesDir),
    fileRoot(FileRootKind.CACHE, "Cache", "Trimmed by the system", context.cacheDir),
    fileRoot(FileRootKind.NO_BACKUP, "No-backup files", "Never backed up", context.noBackupFilesDir),
    fileRoot(FileRootKind.CACHE_EXTERNAL, "Cache (external)", "May be absent", context.externalCacheDir),
    fileRoot(FileRootKind.CODE_CACHE, "Code cache", "Compiled code", context.codeCacheDir),
)

private fun fileRoot(
    kind: FileRootKind,
    label: String,
    description: String,
    dir: File?,
): FileRoot = FileRoot(
    kind = kind,
    label = label,
    description = description,
    path = dir?.let { file -> canonicalPath(file) }.orEmpty(),
    exists = dir?.isDirectory == true,
    childCount = dir?.listFiles()?.size ?: 0,
)

// The walker resolves symlinks, so the root must be canonical too or its paths never prefix-match.
private fun canonicalPath(dir: File): String =
    try {
        dir.canonicalPath
    } catch (_: IOException) {
        dir.absolutePath
    }

// endregion
