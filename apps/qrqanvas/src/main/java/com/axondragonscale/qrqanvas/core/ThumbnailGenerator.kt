package com.axondragonscale.qrqanvas.core

import android.graphics.Bitmap
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Shared utility for generating QR code thumbnail bitmaps.
 * Used by HomeVM and TemplatesVM to render small preview images
 * for grid display without duplicating rendering logic.
 */
object ThumbnailGenerator {

    private const val DEFAULT_THUMBNAIL_SIZE = 200

    /**
     * Renders a small QR code thumbnail from the given [content] and [style].
     * Strips the logo and overrides size/quietZone for fast, compact rendering.
     *
     * @param content The QR content to encode.
     * @param style The QR style to apply. Logo and size will be overridden.
     * @param size The output thumbnail size in pixels.
     * @param quietZone The quiet zone (margin) in modules. Defaults to 1.
     * @return A rendered [Bitmap], or null if rendering fails.
     */
    fun generate(
        content: QRContent,
        style: QRStyle,
        size: Int = DEFAULT_THUMBNAIL_SIZE,
        quietZone: Int = 1,
    ): Bitmap? = try {
        val thumbnailStyle = style.copy(size = size, quietZone = quietZone, logoData = null)
        val bitMatrix = QRGenerator.generate(content, thumbnailStyle.errorCorrection)
        QRRenderer.render(bitMatrix, thumbnailStyle)
    } catch (e: Exception) {
        null
    }
}
