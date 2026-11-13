package com.axondragonscale.wallmatic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.transformations
import com.axondragonscale.wallmatic.core.WallpaperCropTransformation
import com.axondragonscale.wallmatic.model.WallpaperCrop

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * The Coil model for a wallpaper preview: [uri] alone unless [crop] is a custom crop, in which
 * case the request carries the crop transformation.
 */
@Composable
fun wallpaperModel(uri: String?, crop: WallpaperCrop = WallpaperCrop.Default): Any? {
    if (uri.isNullOrBlank() || crop.isDefault) return uri

    val context = LocalPlatformContext.current
    return remember(uri, crop) {
        ImageRequest.Builder(context)
            .data(uri)
            .transformations(WallpaperCropTransformation(crop))
            .build()
    }
}
