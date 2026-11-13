package com.axondragonscale.wallmatic.model

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/** A crop off the centred 1x default, as a focal point plus zoom in the source image. */
data class WallpaperCrop(
    val focalX: Float,
    val focalY: Float,
    val zoom: Float,
)

/** The crop a preview must show, or null while the wallpaper is at the default centre crop. */
val Wallpaper.crop: WallpaperCrop?
    get() = if (hasCustomCrop) WallpaperCrop(cropFocalX, cropFocalY, cropZoom) else null
