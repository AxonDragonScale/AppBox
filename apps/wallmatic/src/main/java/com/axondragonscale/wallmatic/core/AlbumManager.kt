package com.axondragonscale.wallmatic.core

import android.content.Context
import android.net.Uri
import com.axondragonscale.wallmatic.database.entity.Folder
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.getAllWallpapers
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.FileUtil
import com.axondragonscale.wallmatic.util.takePersistableUriPermission

/**
 * Created by Ronak Harkhani on 26/06/24
 */
class AlbumManager(
    private val context: Context,
    private val repository: WallmaticRepository,
) {

    inline fun forAlbum(album: FullAlbum, block: SpecificAlbumManager.() -> Unit) {
        SpecificAlbumManager(album).block()
    }

    inner class SpecificAlbumManager(val album: FullAlbum) {

        suspend fun addFolder(folderUri: Uri) {
            context.takePersistableUriPermission(folderUri)

            // Ignore if folder is already present in the album
            if (folderUri.toString() in album.folders.map { it.folderUri }) return

            val wallpapers = FileUtil.getAllWallpapersInFolder(context, folderUri)
            if (wallpapers.isEmpty()) return // Ignore is folder is empty

            val wallpaperEntities = wallpapers.map { Wallpaper(it.toString()) }
            val wallpaperIds = repository.saveWallpapers(wallpaperEntities)

            val folderCoverUri = wallpapers.first().toString()
            val folder = Folder(
                name = FileUtil.getFolderName(context, folderUri),
                coverUri = folderCoverUri,
                folderUri = folderUri.toString(),
                wallpapers = wallpaperIds,
            )
            val folderId = repository.saveFolder(folder)

            repository.updateAlbum(album.id) {
                folderIds = folderIds + folderId
                if (coverUri == null) coverUri = folderCoverUri // Add coverUri only if not present
            }
        }

        suspend fun addWallpapers(uris: List<Uri>) {
            uris.forEach { uri -> context.takePersistableUriPermission(uri) }

            // TODO: Verify
            // Make sure existing wallpapers are not re-added
            val existingWallpaperUris = album.getAllWallpapers().map { it.uri }
            val newWallpapers = uris.filter { it.toString() !in existingWallpaperUris }
            val newWallpaperEntities = newWallpapers.map { Wallpaper(it.toString()) }
            val newWallpaperIds = repository.saveWallpapers(newWallpaperEntities)

            repository.updateAlbum(album.id) {
                wallpaperIds = wallpaperIds + newWallpaperIds
                if (coverUri == null) coverUri = newWallpapers.first().toString()
            }
        }

    }

}
