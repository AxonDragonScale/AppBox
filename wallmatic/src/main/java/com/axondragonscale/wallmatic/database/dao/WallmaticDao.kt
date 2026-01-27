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

    // Get All

    @Query("SELECT * FROM album")
    fun getAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM folder WHERE id in (:ids)")
    suspend fun getFolders(ids: List<Int>): List<Folder>

    @Query("SELECT * FROM wallpaper WHERE id IN (:ids) ORDER BY isBlacklisted")
    suspend fun getWallpapers(ids: List<Int>): List<Wallpaper>

    // Get One

    @Query("SELECT * FROM album WHERE id = :id")
    suspend fun getAlbum(id: Int): Album?

    @Query("SELECT * FROM folder WHERE id = :id")
    suspend fun getFolder(id: Int): Folder?

    @Query("SELECT * FROM wallpaper WHERE id = :id")
    suspend fun getWallpaper(id: Int): Wallpaper?

    // Upsert

    @Upsert
    suspend fun upsertAlbum(album: Album): Long

    @Upsert
    suspend fun upsertFolder(folder: Folder): Long

    @Upsert
    suspend fun upsertWallpaper(wallpaper: Wallpaper): Long

    @Upsert
    suspend fun upsertWallpapers(wallpaper: List<Wallpaper>): LongArray

    // Delete

    @Query("DELETE FROM album WHERE id = :id")
    suspend fun deleteAlbum(id: Int)

    @Query("DELETE FROM folder WHERE id IN (:ids)")
    suspend fun deleteFolders(ids: List<Int>)

    @Query("DELETE FROM wallpaper WHERE id IN (:ids)")
    suspend fun deleteWallpapers(ids: List<Int>)

    @Transaction
    suspend fun deleteFullAlbum(id: Int) {
        val album = getAlbum(id) ?: return
        deleteAlbum(album.id)
        deleteFolders(album.folderIds)
        deleteWallpapers(album.wallpaperIds)
    }


    // Delete All

    @Query("DELETE FROM album")
    suspend fun deleteAllAlbums()

    @Query("DELETE FROM folder")
    suspend fun deleteAllFolders()

    @Query("DELETE FROM wallpaper")
    suspend fun deleteAllWallpapers()

    @Transaction
    suspend fun deleteAllData() {
        deleteAllAlbums()
        deleteAllFolders()
        deleteAllWallpapers()
    }
}
