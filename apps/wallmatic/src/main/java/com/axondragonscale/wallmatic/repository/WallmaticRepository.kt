package com.axondragonscale.wallmatic.repository

import com.axondragonscale.wallmatic.database.dao.WallmaticDao
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Folder
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.WallpaperCrop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Created by Ronak Harkhani on 26/06/24
 */
class WallmaticRepository(private val dao: WallmaticDao) {

    fun observeFullAlbums(): Flow<List<FullAlbum>> =
        combine(dao.observeAlbums(), dao.observeFolders(), dao.observeWallpapers()) { albums, folders, wallpapers ->
            assembleAlbums(albums, folders, wallpapers)
        }

    fun observeFullFolders(): Flow<List<FullFolder>> =
        combine(dao.observeFolders(), dao.observeWallpapers()) { folders, wallpapers ->
            assembleFolders(folders, wallpapers)
        }

    suspend fun getAllFullAlbums(): List<FullAlbum> = assembleAlbums(
        dao.observeAlbums().first(),
        dao.observeFolders().first(),
        dao.observeWallpapers().first(),
    )

    suspend fun getFullAlbum(albumId: Int): FullAlbum? {
        val album = dao.getAlbum(albumId) ?: return null
        val folders = dao.getFolders(albumId).map { folder ->
            FullFolder(folder.id, folder.name, folder.folderUri, dao.getFolderWallpapers(folder.id))
        }
        return FullAlbum(album.id, album.name, folders, dao.getDirectWallpapers(albumId))
    }

    suspend fun getWallpaper(id: Int): Wallpaper? = dao.getWallpaper(id)

    suspend fun getAlbumWallpaperUris(albumId: Int): Set<String> =
        dao.getAlbumWallpaperUris(albumId).toSet()

    suspend fun getAlbumFolderUris(albumId: Int): Set<String> =
        dao.getAlbumFolderUris(albumId).toSet()

    suspend fun saveAlbum(name: String): Int = dao.upsertAlbum(Album(name)).toInt()
    suspend fun renameAlbum(albumId: Int, name: String) = dao.updateAlbumName(albumId, name)
    suspend fun saveFolderWithWallpapers(folder: Folder, wallpapers: List<Wallpaper>): Int =
        dao.upsertFolderWithWallpapers(folder, wallpapers)
    suspend fun saveWallpapers(wallpapers: List<Wallpaper>): List<Int> =
        dao.upsertWallpapers(wallpapers).map { it.toInt() }

    suspend fun updateWallpaperCrop(wallpaperId: Int, crop: WallpaperCrop) =
        dao.updateWallpaperCrop(wallpaperId, crop.focalX, crop.focalY, crop.zoom)

    suspend fun setBlacklisted(wallpaperId: Int, blacklisted: Boolean) =
        dao.setBlacklisted(wallpaperId, blacklisted)

    suspend fun deleteFullAlbum(albumId: Int) = dao.deleteFullAlbum(albumId)
    suspend fun deleteFullFolder(folderId: Int) = dao.deleteFullFolder(folderId)
    suspend fun deleteWallpaper(wallpaperId: Int) = dao.deleteWallpaper(wallpaperId)

    private fun assembleAlbums(albums: List<Album>, folders: List<Folder>, wallpapers: List<Wallpaper>): List<FullAlbum> {
        val foldersByAlbum = folders.groupBy { it.albumId }
        val wallpapersByFolder = wallpapers.filter { it.folderId != null }.groupBy { it.folderId }
        return albums.map { album ->
            val fullFolders = foldersByAlbum[album.id].orEmpty().map { folder ->
                FullFolder(folder.id, folder.name, folder.folderUri, wallpapersByFolder[folder.id].orEmpty())
            }
            val direct = wallpapers.filter { it.albumId == album.id && it.folderId == null }
            FullAlbum(album.id, album.name, fullFolders, direct)
        }
    }

    private fun assembleFolders(folders: List<Folder>, wallpapers: List<Wallpaper>): List<FullFolder> {
        val byFolder = wallpapers.filter { it.folderId != null }.groupBy { it.folderId }
        return folders.map { FullFolder(it.id, it.name, it.folderUri, byFolder[it.id].orEmpty()) }
    }
}
