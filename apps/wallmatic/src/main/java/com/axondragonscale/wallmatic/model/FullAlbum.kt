package com.axondragonscale.wallmatic.model

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 01/07/24
 */
data class FullAlbum(
    val id: Int,
    val name: String,
    val folders: List<FullFolder>,
    val wallpapers: List<Wallpaper>,
) {
    val cover: Wallpaper? get() = folders.firstOrNull()?.cover ?: wallpapers.firstOrNull()
    val allWallpapers: List<Wallpaper> get() = wallpapers + folders.flatMap { it.wallpapers }
    val hasWallpapers: Boolean get() = allWallpapers.isNotEmpty()

    fun getRandomWallpaperOrNull(): Wallpaper? = allWallpapers.filter { !it.isBlacklisted }.randomOrNull()
}
