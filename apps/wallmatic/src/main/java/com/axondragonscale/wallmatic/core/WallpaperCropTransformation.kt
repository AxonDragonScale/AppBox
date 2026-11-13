package com.axondragonscale.wallmatic.core

import android.graphics.Bitmap
import coil3.size.Dimension
import coil3.size.Size
import coil3.transform.Transformation
import com.axondragonscale.wallmatic.model.WallpaperCrop

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * Crops a preview to the region [viewport] resolves for the request's own target aspect, so the
 * same wallpaper shows the same region in every container size. The cached bitmap is the crop.
 */
class WallpaperCropTransformation(
    private val crop: WallpaperCrop,
) : Transformation() {

    override val cacheKey = "WallpaperCrop:${crop.focalX}:${crop.focalY}:${crop.zoom}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val targetWidth = (size.width as? Dimension.Pixels)?.px ?: return input
        val targetHeight = (size.height as? Dimension.Pixels)?.px ?: return input

        val rect = viewport(
            sourceWidth = input.width,
            sourceHeight = input.height,
            crop = crop,
            targetAspect = targetWidth.toFloat() / targetHeight,
        )

        val coversEverything = rect.left == 0 && rect.top == 0 &&
            rect.width() == input.width && rect.height() == input.height
        if (rect.isEmpty || coversEverything) return input

        return Bitmap.createBitmap(input, rect.left, rect.top, rect.width(), rect.height())
    }
}
