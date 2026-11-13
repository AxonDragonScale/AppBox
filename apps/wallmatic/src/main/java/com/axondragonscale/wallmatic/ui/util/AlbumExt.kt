package com.axondragonscale.wallmatic.ui.util

import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder

/**
 * Created by Ronak Harkhani on 21/09/24
 */

fun FullAlbum.countSummary() =
    "${folders.size.counted("Folder")} · ${allWallpapers.size.counted("Wallpaper")}"

fun FullFolder.countSummary() = wallpapers.size.counted("Wallpaper")

private fun Int.counted(noun: String) = "$this $noun${if (this == 1) "" else "s"}"
