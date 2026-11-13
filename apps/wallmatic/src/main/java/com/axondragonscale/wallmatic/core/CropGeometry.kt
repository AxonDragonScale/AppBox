package com.axondragonscale.wallmatic.core

import android.graphics.Rect
import com.axondragonscale.wallmatic.model.WallpaperCrop
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * Created by Ronak Harkhani on 11/13/26
 */

data class CropBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

/** Fractional source rect for [crop] at [targetAspect]; values are fractions of the source. */
fun cropBox(sourceAspect: Float, targetAspect: Float, crop: WallpaperCrop): CropBox {
    val safeSourceAspect = sourceAspect.takeIf { it.isFinite() && it > 0f } ?: 1f
    val safeTargetAspect = targetAspect.takeIf { it.isFinite() && it > 0f } ?: safeSourceAspect
    val zoom = crop.zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
    val width = (min(1f, safeTargetAspect / safeSourceAspect) / zoom).coerceIn(0f, 1f)
    val height = (min(1f, safeSourceAspect / safeTargetAspect) / zoom).coerceIn(0f, 1f)
    val focalX = crop.focalX.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f
    val focalY = crop.focalY.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f
    return CropBox(
        left = (focalX - width / 2f).coerceIn(0f, 1f - width),
        top = (focalY - height / 2f).coerceIn(0f, 1f - height),
        width = width,
        height = height,
    )
}

/**
 * [cropBox] in source pixels, rounded outward so the rect always covers the region the focal
 * point and zoom asked for. The apply path passes the decoded, EXIF-rotated bitmap's dimensions.
 */
fun viewport(
    sourceWidth: Int,
    sourceHeight: Int,
    crop: WallpaperCrop,
    targetAspect: Float,
): Rect {
    if (sourceWidth <= 0 || sourceHeight <= 0) return Rect()
    val box = cropBox(sourceWidth.toFloat() / sourceHeight, targetAspect, crop)
    val left = floor(box.left * sourceWidth).toInt()
    val top = floor(box.top * sourceHeight).toInt()
    return Rect(
        left,
        top,
        ceil((box.left + box.width) * sourceWidth).toInt().coerceIn(left + 1, sourceWidth),
        ceil((box.top + box.height) * sourceHeight).toInt().coerceIn(top + 1, sourceHeight),
    )
}

internal const val MIN_CROP_ZOOM = 1f
internal const val MAX_CROP_ZOOM = 4f
