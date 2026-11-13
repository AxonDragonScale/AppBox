package com.axondragonscale.wallmatic.core

import android.content.Context
import androidx.core.net.toUri
import com.axondragonscale.commons.log
import com.axondragonscale.commons.logE
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.FileUtil
import kotlinx.coroutines.CancellationException

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/** Reconciles album rows with the files on disk. */
class AlbumSyncer(
    private val context: Context,
    private val repository: WallmaticRepository,
) {

    /**
     * Syncs albums with the filesystem; failures are logged and swallowed.
     *
     * Syncing is housekeeping, so a failure must never block the wallpaper update that follows,
     * nor app start.
     */
    suspend fun syncAlbums() {
        try {
            repository.getAllFullAlbums().forEach { album ->
                val (_, invalidWallpapers) = album.wallpapers.partition {
                    FileUtil.isValidFile(context, it.uri.toUri())
                }
                invalidWallpapers.forEach { repository.deleteWallpaper(it.id) }

                album.folders.forEach { folder -> syncFolder(album.id, folder) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            this.logE { "Album sync failed. error: ${e.message}" }
            this.log { e }
        }
    }

    private suspend fun syncFolder(albumId: Int, folder: FullFolder) {
        val folderUri = folder.folderUri.toUri()
        if (!FileUtil.isValidFolder(context, folderUri)) {
            repository.deleteFullFolder(folder.id)
            return
        }

        val diskUris = FileUtil.getAllWallpapersInFolder(context, folderUri).map { it.toString() }
        val knownUris = folder.wallpapers.mapTo(mutableSetOf()) { it.uri }

        repository.saveWallpapers(
            diskUris.filter { it !in knownUris }
                .map { Wallpaper(uri = it, albumId = albumId, folderId = folder.id) }
        )
        folder.wallpapers
            .filter { it.uri !in diskUris }
            .forEach { repository.deleteWallpaper(it.id) }

        if (diskUris.isEmpty()) repository.deleteFullFolder(folder.id)
    }

}
