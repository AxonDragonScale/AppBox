package com.axondragonscale.wallmatic.core

import android.content.Context
import android.net.Uri
import com.axondragonscale.wallmatic.database.entity.Folder
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.FileUtil
import com.axondragonscale.wallmatic.util.takePersistableUriPermission

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/** User-driven album edits: add folders and wallpapers, remove folders. */
class AlbumEditor(
    private val context: Context,
    private val repository: WallmaticRepository,
) {

    suspend fun addFolder(albumId: Int, folderUri: Uri) {
        context.takePersistableUriPermission(folderUri)

        if (folderUri.toString() in repository.getAlbumFolderUris(albumId)) return

        val wallpapers = FileUtil.getAllWallpapersInFolder(context, folderUri)
        if (wallpapers.isEmpty()) return

        repository.saveFolderWithWallpapers(
            folder = Folder(
                name = FileUtil.getFolderName(context, folderUri),
                folderUri = folderUri.toString(),
                albumId = albumId,
            ),
            wallpapers = wallpapers.map { Wallpaper(uri = it.toString(), albumId = albumId) },
        )
    }

    suspend fun addWallpapers(albumId: Int, uris: List<Uri>) {
        uris.forEach { context.takePersistableUriPermission(it) }

        // Re-selecting a wallpaper the album already holds is a no-op, not an error.
        val existingUris = repository.getAlbumWallpaperUris(albumId)
        val newUris = uris.filter { it.toString() !in existingUris }
        if (newUris.isEmpty()) return

        repository.saveWallpapers(newUris.map { Wallpaper(uri = it.toString(), albumId = albumId) })
    }

    suspend fun removeFolder(folderId: Int) = repository.deleteFullFolder(folderId)

}
