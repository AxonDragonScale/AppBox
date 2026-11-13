package com.axondragonscale.wallmatic.model

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/** A focal point plus zoom into the source image. */
data class WallpaperCrop(
    val focalX: Float = 0.5f,
    val focalY: Float = 0.5f,
    val zoom: Float = 1f,
) {

    companion object {
        val Default = WallpaperCrop()
    }

    val isDefault: Boolean get() = this == Default
}
