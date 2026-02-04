package com.axondragonscale.wallmatic.ui.util

import com.axondragonscale.wallmatic.database.entity.Album

/**
 * Created by Ronak Harkhani on 21/09/24
 */

fun Album.countSummary() =
    "${folderIds.size} Folder${if (isPluralFolders()) "s" else ""}, " +
            "${wallpaperIds.size} Wallpaper${if (isPluralWallpapers()) "s" else ""}"

private fun Album.isPluralFolders() = folderIds.size != 1
private fun Album.isPluralWallpapers() = wallpaperIds.size != 1

fun Album.hasWallpapers() =
    folderIds.isNotEmpty() || wallpaperIds.isNotEmpty()
