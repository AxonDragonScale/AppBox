package com.axondragonscale.wallmatic.core

import android.graphics.Rect
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * The largest rect at [targetAspect] that fits the source, shrunk by [zoom] and centred on the
 * focal point, clamped inside the source. Focal coordinates are a fraction of [sourceWidth] and
 * [sourceHeight]; the apply path passes the decoded, EXIF-rotated bitmap's own dimensions.
 */
fun viewport(
    sourceWidth: Int,
    sourceHeight: Int,
    focalX: Float,
    focalY: Float,
    zoom: Float,
    targetAspect: Float,
): Rect {
    if (sourceWidth <= 0 || sourceHeight <= 0) return Rect()

    val aspect = targetAspect.takeIf { it.isFinite() && it > 0f }
        ?: sourceWidth.toFloat() / sourceHeight
    val safeZoom = zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
    val safeFocalX = focalX.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f
    val safeFocalY = focalY.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f

    val cropWidth = min(sourceWidth.toFloat(), sourceHeight * aspect) / safeZoom
    val cropHeight = min(sourceHeight.toFloat(), sourceWidth / aspect) / safeZoom

    val left = (safeFocalX * sourceWidth - cropWidth / 2f).coerceIn(0f, sourceWidth - cropWidth)
    val top = (safeFocalY * sourceHeight - cropHeight / 2f).coerceIn(0f, sourceHeight - cropHeight)
    val leftPx = floor(left).toInt()
    val topPx = floor(top).toInt()

    // Round outward, so the rect always covers the region the focal point and zoom asked for.
    return Rect(
        leftPx,
        topPx,
        ceil(left + cropWidth).toInt().coerceIn(leftPx + 1, sourceWidth),
        ceil(top + cropHeight).toInt().coerceIn(topPx + 1, sourceHeight),
    )
}

internal const val MIN_CROP_ZOOM = 1f
internal const val MAX_CROP_ZOOM = 4f
