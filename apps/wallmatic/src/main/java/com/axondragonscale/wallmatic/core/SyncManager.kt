package com.axondragonscale.wallmatic.core

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.axondragonscale.commons.log
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.getAllWallpapers
import com.axondragonscale.wallmatic.model.hasWallpapers
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.FileUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Created by Ronak Harkhani on 26/06/24
 */
class SyncManager(
    private val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
    private val repository: WallmaticRepository,
    private val wallpaperUpdater: WallpaperUpdater,
) {

    /**
     * Synchronizes the albums and wallpapers in the database with the actual files on the device.
     *
     * This function performs the following actions:
     *  - Removes invalid wallpapers and folders from albums.
     *  - Updates album covers if necessary.
     *  - Updates the current wallpaper if it is no longer valid.
     *
     * Syncing is housekeeping, so a failure is logged and swallowed here: it must never block
     * the wallpaper update that follows, nor app start.
     */
    suspend fun syncAlbums() {
        try {
            val albums = repository.getAllFullAlbums()
            albums.forEach { album ->
                // Remove invalid wallpapers
                val (validWallpapers, invalidWallpapers) = album.wallpapers.partition {
                    FileUtil.isValidFile(context, Uri.parse(it.uri))
                }
                repository.deleteWallpapers(invalidWallpapers.map { it.id })

                // Remove invalid folders
                val validFolders = syncFolders(album.folders)

                // Update album cover if needed
                val validCoverUri = validFolders.firstOrNull()?.coverUri
                    ?: validWallpapers.firstOrNull()?.uri

                repository.updateAlbum(album.id) {
                    coverUri = validCoverUri
                    folderIds = validFolders.map { it.id }
                    wallpaperIds = validWallpapers.map { it.id }
                }
            }

            // Update wallpaper and config if current wallpaper is not in the album
            repairConfig()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            this.logE { "Album sync failed. error: ${e.message}" }
            this.log { e }
        }
    }

    /**
     * Syncs the given list of folders with the disk storage.
     *
     * This function iterates through each folder and performs the following actions:
     * 1. Removes the folder from the database if it no longer exists on disk.
     * 2. Retrieves all wallpapers from the disk storage for the folder.
     * 3. Adds new wallpapers found on disk to the database.
     * 4. Removes wallpapers from the database that are no longer present on disk.
     * 5. Updates the folder cover if it's invalid or missing.
     *
     * @param folders The list of folders to refresh.
     * @return A new list of folders with updated information, excluding invalid folders.
     */
    private suspend fun syncFolders(folders: List<FullFolder>): List<FullFolder> {
        val validFolders = mutableListOf<FullFolder>()
        folders.forEach { folder ->
            // Remove folder from DB if it doesn't exist anymore
            if (!FileUtil.isValidFolder(context, folder.folderUri.toUri())) {
                repository.deleteFolders(listOf(folder.id))
                return@forEach
            }

            // Sync with disk storage
            val diskWallpapers = FileUtil.getAllWallpapersInFolder(context, folder.folderUri.toUri())
            val syncedWallpapers = diskWallpapers.map { uri ->
                val existingWallpaper = folder.wallpapers.firstOrNull { it.uri == uri.toString() }
                if (existingWallpaper != null) return@map existingWallpaper

                val newWallpaper = Wallpaper(uri.toString())
                val id = repository.saveWallpaper(newWallpaper)
                newWallpaper.also { it.id = id }
            }

            // Delete invalid wallpapers
            val invalidWallpapers = folder.wallpapers.filter { wallpaper ->
                wallpaper.uri !in syncedWallpapers.map { it.uri }
            }
            repository.deleteWallpapers(invalidWallpapers.map { it.id })
            if (syncedWallpapers.isEmpty()) {
                repository.deleteFolders(listOf(folder.id))
                return@forEach
            }

            val coverUri = syncedWallpapers.minByOrNull { it.id }?.uri
            val syncedFolder = folder.copy(coverUri = coverUri, wallpapers = syncedWallpapers)
            repository.updateFolder(folder.id) {
                this.coverUri = syncedFolder.coverUri
                this.wallpapers = syncedFolder.wallpapers.map { it.id }
            }
            validFolders.add(syncedFolder)
        }

        return validFolders
    }

    /**
     * Validates config against the current album state and resets it if necessary.
     * Triggers an immediate wallpaper update if the current wallpaper is stale.
     *
     * This only reads Room and config, never the filesystem, so it is the cheap repair to run
     * after the user deletes an album that might be the configured one.
     */
    suspend fun repairConfig() {
        val config = appPrefsRepository.configFlow.first()
        if (!config.isInit) return

        val refreshConfig: suspend (Int, Int, TargetScreen) -> Unit = { albumId, currentWallpaperId, target ->
            val album = repository.getFullAlbum(albumId)
            if (album == null || !album.hasWallpapers())
                appPrefsRepository.setConfig(config.copy { isInit = false })
            else if (currentWallpaperId !in album.getAllWallpapers().map { it.id }) {
                val result = wallpaperUpdater.updateWallpaper(target)
                this.logD { "Config repair wallpaper update. target: $target, result: $result" }
            }
        }

        refreshConfig(config.homeConfig.albumId, config.homeConfig.currentWallpaperId, TargetScreen.Home)
        if (!config.mirrorHomeConfigForLock)
            refreshConfig(config.lockConfig.albumId, config.lockConfig.currentWallpaperId, TargetScreen.Lock)
    }

}
