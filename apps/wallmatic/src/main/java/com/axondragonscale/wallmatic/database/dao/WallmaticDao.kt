package com.axondragonscale.wallmatic.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Folder
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 26/06/24
 */
@Dao
interface WallmaticDao {

    // region Observe

    @Query("SELECT * FROM album ORDER BY id")
    fun observeAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM folder ORDER BY id")
    fun observeFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM wallpaper ORDER BY isBlacklisted, id")
    fun observeWallpapers(): Flow<List<Wallpaper>>

    // endregion

    // region Read

    @Query("SELECT * FROM album WHERE id = :id")
    suspend fun getAlbum(id: Int): Album?

    @Query("SELECT * FROM folder WHERE albumId = :albumId ORDER BY id")
    suspend fun getFolders(albumId: Int): List<Folder>

    @Query("SELECT * FROM wallpaper WHERE id = :id")
    suspend fun getWallpaper(id: Int): Wallpaper?

    @Query("SELECT * FROM wallpaper WHERE albumId = :albumId AND folderId IS NULL ORDER BY isBlacklisted, id")
    suspend fun getDirectWallpapers(albumId: Int): List<Wallpaper>

    @Query("SELECT * FROM wallpaper WHERE folderId = :folderId ORDER BY isBlacklisted, id")
    suspend fun getFolderWallpapers(folderId: Int): List<Wallpaper>

    @Query("SELECT uri FROM wallpaper WHERE albumId = :albumId")
    suspend fun getAlbumWallpaperUris(albumId: Int): List<String>

    @Query("SELECT folderUri FROM folder WHERE albumId = :albumId")
    suspend fun getAlbumFolderUris(albumId: Int): List<String>

    // endregion

    // region Update

    @Query("UPDATE wallpaper SET focalX = :focalX, focalY = :focalY, zoom = :zoom WHERE id = :id")
    suspend fun updateWallpaperCrop(id: Int, focalX: Float, focalY: Float, zoom: Float)

    @Query("UPDATE wallpaper SET isBlacklisted = :blacklisted WHERE id = :id")
    suspend fun setBlacklisted(id: Int, blacklisted: Boolean)

    @Query("UPDATE album SET name = :name WHERE id = :id")
    suspend fun updateAlbumName(id: Int, name: String)

    // endregion

    // region Upsert

    @Upsert
    suspend fun upsertAlbum(album: Album): Long

    @Upsert
    suspend fun upsertFolder(folder: Folder): Long

    @Upsert
    suspend fun upsertWallpapers(wallpapers: List<Wallpaper>): LongArray

    @Transaction
    suspend fun upsertFolderWithWallpapers(folder: Folder, wallpapers: List<Wallpaper>): Int {
        val folderId = upsertFolder(folder).toInt()
        upsertWallpapers(wallpapers.map { it.copy(folderId = folderId) })
        return folderId
    }

    // endregion

    // region Delete

    @Query("DELETE FROM wallpaper WHERE id = :id")
    suspend fun deleteWallpaper(id: Int)

    @Query("DELETE FROM wallpaper WHERE folderId = :folderId")
    suspend fun deleteFolderWallpapers(folderId: Int)

    @Query("DELETE FROM wallpaper WHERE albumId = :albumId")
    suspend fun deleteAlbumWallpapers(albumId: Int)

    @Query("DELETE FROM folder WHERE id = :id")
    suspend fun deleteFolder(id: Int)

    @Query("DELETE FROM folder WHERE albumId = :albumId")
    suspend fun deleteAlbumFolders(albumId: Int)

    @Query("DELETE FROM album WHERE id = :id")
    suspend fun deleteAlbum(id: Int)

    @Transaction
    suspend fun deleteFullAlbum(id: Int) {
        deleteAlbumWallpapers(id)
        deleteAlbumFolders(id)
        deleteAlbum(id)
    }

    @Transaction
    suspend fun deleteFullFolder(id: Int) {
        deleteFolderWallpapers(id)
        deleteFolder(id)
    }

    // endregion
}
