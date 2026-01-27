package com.axondragonscale.wallmatic.model

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 01/07/24
 */
data class FullAlbum(
    val id: Int,
    val name: String,
    val coverUri: String?,
    val folders: List<FullFolder>,
    val wallpapers: List<Wallpaper>,
)

fun FullAlbum.hasWallpapers() =
    wallpapers.isNotEmpty() || folders.any { it.wallpapers.isNotEmpty() }

fun FullAlbum.getAllWallpapers() =
    wallpapers + folders.flatMap { it.wallpapers }

fun FullAlbum.getWhitelistedWallpapers() =
    this.getAllWallpapers().filter { !it.isBlacklisted }


