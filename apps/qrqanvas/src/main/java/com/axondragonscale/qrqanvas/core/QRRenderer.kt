package com.axondragonscale.qrqanvas.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import com.axondragonscale.qrqanvas.model.ColorData
import com.axondragonscale.qrqanvas.model.Corner
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.LogoData
import com.axondragonscale.qrqanvas.model.LogoShape
import com.axondragonscale.qrqanvas.model.QRStyle
import com.google.zxing.common.BitMatrix
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Ronak Harkhani on 13/02/26
 *
 * Renders a [BitMatrix] (from [QRGenerator]) combined with a [QRStyle] into an Android [Bitmap].
 *
 * Supports:
 * - Solid, LinearGradient, and RadialGradient colors for background, dots, and eye patterns
 * - All [DotStyle] shapes for data modules (Square, Circle, RoundedSquare, Diamond, Star, Heart)
 * - All [EyeStyle] shapes for finder patterns (Square, Circle, RoundedSquare, Leaf, Diamond)
 * - Logo embedding with shape clipping, padding, and automatic module exclusion
 */
object QRRenderer {

    /**
     * Renders the given [bitMatrix] using the provided [style] into a [Bitmap].
     *
     * The output bitmap is square with dimensions [QRStyle.size] x [QRStyle.size].
     * A quiet zone of [QRStyle.quietZone] modules is added around the QR code.
     *
     * @param bitMatrix The QR code matrix where each cell is one module (from [QRGenerator]).
     * @param style The visual style configuration for rendering.
     * @param logoBitmap Optional logo bitmap to draw centered over the QR code.
     *   When provided, [QRStyle.logoData] must also be non-null to configure size, padding, and shape ([LogoData]).
     * @return A rendered [Bitmap] of the QR code.
     */
    fun render(bitMatrix: BitMatrix, style: QRStyle, logoBitmap: Bitmap? = null): Bitmap {
        val matrixSize = bitMatrix.width // BitMatrix is always square
        val totalModules = matrixSize + style.quietZone * 2
        val outputSize = style.size
        val moduleSize = outputSize.toFloat() / totalModules

        val bitmap = createBitmap(outputSize, outputSize)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgRect = RectF(0f, 0f, outputSize.toFloat(), outputSize.toFloat())
        canvas.drawBackground(style.backgroundColor, bgRect)

        // The bounding rect of the actual QR code area (excluding quiet zone)
        val qrOffset = style.quietZone * moduleSize
        val qrSize = matrixSize * moduleSize
        val qrBounds = RectF(qrOffset, qrOffset, qrOffset + qrSize, qrOffset + qrSize)

        // Compute logo exclusion zone (if logo is present)
        val logoExclusionRect = computeLogoExclusionRect(style.logoData, logoBitmap, qrBounds)

        // Identify finder pattern regions
        val finderRegions = getFinderPatternRegions(matrixSize)

        // Draw data modules (everything outside finder regions and logo exclusion zone)
        canvas.drawDataModules(
            bitMatrix, style, moduleSize, matrixSize,
            finderRegions, qrBounds, logoExclusionRect,
        )

        // Draw finder patterns separately
        canvas.drawFinderPatterns(style, moduleSize, matrixSize, qrBounds)

        // Draw logo on top of everything
        if (logoBitmap != null && style.logoData != null) {
            canvas.drawLogo(logoBitmap, style.logoData, style.backgroundColor, qrBounds)
        }

        return bitmap
    }

    /**
     * Computes the rectangular area in the center of the QR code where data modules
     * should be skipped to make room for the logo. Returns `null` if no logo is configured.
     *
     * The exclusion zone includes the logo itself plus its padding, so modules don't
     * crowd right up against the logo edge.
     */
    private fun computeLogoExclusionRect(
        logoData: LogoData?,
        logoBitmap: Bitmap?,
        qrBounds: RectF,
    ): RectF? {
        if (logoData == null || logoBitmap == null) return null

        val qrSize = qrBounds.width()
        val cx = qrBounds.centerX()
        val cy = qrBounds.centerY()

        // Total size includes the logo area + padding on each side
        val totalFraction = logoData.size + logoData.padding * 2
        val halfExclusion = qrSize * totalFraction / 2f

        return RectF(cx - halfExclusion, cy - halfExclusion, cx + halfExclusion, cy + halfExclusion)
    }

    private fun Canvas.drawBackground(colorData: ColorData, rect: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        applyColorConfig(paint, colorData, rect)
        drawRect(rect, paint)
    }

    /**
     * Returns a set of packed (col, row) pairs that belong to the three 7x7 finder patterns,
     * including their 1-module separator border.
     */
    private fun getFinderPatternRegions(matrixSize: Int): Set<Long> {
        val regions = mutableSetOf<Long>()

        // Each finder pattern occupies a 7x7 area, but we also skip the 1-module
        // separator around them (8x8 from corner, clipped to matrix bounds).
        val separatorSize = 8 // 7 for pattern + 1 for separator

        for (row in 0 until separatorSize.coerceAtMost(matrixSize)) {
            for (col in 0 until separatorSize.coerceAtMost(matrixSize)) {
                // Top-left
                regions.add(packCoord(col, row))
                // Top-right
                regions.add(packCoord(matrixSize - separatorSize + col, row))
                // Bottom-left
                regions.add(packCoord(col, matrixSize - separatorSize + row))
            }
        }

        return regions
    }

    /** Pack two ints into a single Long for efficient set lookup. */
    private fun packCoord(col: Int, row: Int): Long =
        (col.toLong() shl 32) or (row.toLong() and 0xFFFFFFFFL)

    private fun Canvas.drawDataModules(
        bitMatrix: BitMatrix,
        style: QRStyle,
        moduleSize: Float,
        matrixSize: Int,
        finderRegions: Set<Long>,
        qrBounds: RectF,
        logoExclusionRect: RectF?,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
        applyColorConfig(paint, style.dotColor, qrBounds)

        val quietZone = style.quietZone
        val rect = RectF()
        val modulePath = Path()

        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                if (!bitMatrix[col, row]) continue
                if (packCoord(col, row) in finderRegions) continue

                val left = (col + quietZone) * moduleSize
                val top = (row + quietZone) * moduleSize
                rect.set(left, top, left + moduleSize, top + moduleSize)

                // Skip modules whose center falls within the logo exclusion zone
                if (logoExclusionRect != null) {
                    val moduleCx = rect.centerX()
                    val moduleCy = rect.centerY()
                    if (logoExclusionRect.contains(moduleCx, moduleCy)) continue
                }

                drawModule(rect, style.dotStyle, paint, modulePath)
            }
        }
    }

    // region Dot Style Rendering

    /**
     * Draws a single data module with the given [dotStyle] shape inside [rect].
     */
    private fun Canvas.drawModule(rect: RectF, dotStyle: DotStyle, paint: Paint, modulePath: Path) {
        when (dotStyle) {
            DotStyle.Square -> drawSquareModule(rect, paint)
            DotStyle.Circle -> drawCircleModule(rect, paint)
            DotStyle.RoundedSquare -> drawRoundedSquareModule(rect, paint)
            DotStyle.Diamond -> drawDiamondModule(rect, paint, modulePath)
            DotStyle.Star -> drawStarModule(rect, paint, modulePath)
            DotStyle.Heart -> drawHeartModule(rect, paint, modulePath)
        }
    }

    private fun Canvas.drawSquareModule(rect: RectF, paint: Paint) {
        drawRect(rect, paint)
    }

    private fun Canvas.drawCircleModule(rect: RectF, paint: Paint) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = rect.width().coerceAtMost(rect.height()) / 2f
        drawCircle(cx, cy, radius, paint)
    }

    private fun Canvas.drawRoundedSquareModule(rect: RectF, paint: Paint) {
        val cornerRadius = rect.width() * 0.3f
        drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    private fun Canvas.drawDiamondModule(rect: RectF, paint: Paint, modulePath: Path) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        modulePath.apply {
            rewind()
            moveTo(cx, rect.top)            // Top
            lineTo(rect.right, cy)          // Right
            lineTo(cx, rect.bottom)         // Bottom
            lineTo(rect.left, cy)           // Left
            close()
        }
        drawPath(modulePath, paint)
    }

    private fun Canvas.drawStarModule(rect: RectF, paint: Paint, modulePath: Path) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val outerRadius = rect.width().coerceAtMost(rect.height()) / 2f
        val innerRadius = outerRadius * 0.38f

        modulePath.apply {
            rewind()
            for (i in 0 until 5) {
                // Outer point: starting at top (-90°), going clockwise (72° apart)
                val outerAngle = Math.toRadians(-90.0 + i * 72.0)
                val ox = cx + outerRadius * cos(outerAngle).toFloat()
                val oy = cy + outerRadius * sin(outerAngle).toFloat()

                // Inner point: offset by 36° from the outer point
                val innerAngle = Math.toRadians(-90.0 + i * 72.0 + 36.0)
                val ix = cx + innerRadius * cos(innerAngle).toFloat()
                val iy = cy + innerRadius * sin(innerAngle).toFloat()

                if (i == 0) moveTo(ox, oy) else lineTo(ox, oy)
                lineTo(ix, iy)
            }
            close()
        }
        drawPath(modulePath, paint)
    }

    private fun Canvas.drawHeartModule(rect: RectF, paint: Paint, modulePath: Path) {
        val w = rect.width()
        val h = rect.height()
        val cx = rect.centerX()

        // Heart proportions: two circular bumps at top, point at bottom
        val topY = rect.top + h * 0.3f

        modulePath.apply {
            rewind()

            // Start at the center dip between the two bumps
            moveTo(cx, rect.top + h * 0.38f)

            // Left bump (arc via cubic bezier)
            cubicTo(
                cx - w * 0.02f, rect.top + h * 0.1f,   // control 1
                rect.left, rect.top + h * 0.05f,         // control 2
                rect.left, topY,                          // end at left bump center-height
            )

            // Left side curving down to bottom point
            cubicTo(
                rect.left, rect.top + h * 0.6f,          // control 1
                cx, rect.top + h * 0.7f,                 // control 2
                cx, rect.bottom,                          // bottom point
            )

            // Right side curving up from bottom point
            cubicTo(
                cx, rect.top + h * 0.7f,                 // control 1
                rect.right, rect.top + h * 0.6f,         // control 2
                rect.right, topY,                         // right bump center-height
            )

            // Right bump back to center dip
            cubicTo(
                rect.right, rect.top + h * 0.05f,        // control 1
                cx + w * 0.02f, rect.top + h * 0.1f,     // control 2
                cx, rect.top + h * 0.38f,                 // back to start
            )

            close()
        }
        drawPath(modulePath, paint)
    }

    // endregion

    // region Finder Pattern Rendering

    /**
     * Draws the three finder patterns using the configured [EyeStyle].
     * Each finder pattern is a 7x7 grid composed of three layers:
     * - Outer frame: 7x7 shape (frame color)
     * - Inner separator: 5x5 shape (background color)
     * - Inner dot: 3x3 shape (dot color)
     */
    private fun Canvas.drawFinderPatterns(
        style: QRStyle,
        moduleSize: Float,
        matrixSize: Int,
        qrBounds: RectF,
    ) {
        val finderOrigins = listOf(
            Triple(0, 0, Corner.TopLeft),
            Triple(matrixSize - 7, 0, Corner.TopRight),
            Triple(0, matrixSize - 7, Corner.BottomLeft),
        )

        for ((originCol, originRow, corner) in finderOrigins) {
            drawFinderPattern(style, moduleSize, originCol, originRow, qrBounds, corner)
        }
    }

    private fun Canvas.drawFinderPattern(
        style: QRStyle,
        moduleSize: Float,
        originCol: Int,
        originRow: Int,
        qrBounds: RectF,
        corner: Corner,
    ) {
        val quietZone = style.quietZone

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
        applyColorConfig(framePaint, style.eyeFrameColor, qrBounds)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
        applyColorConfig(bgPaint, style.backgroundColor, qrBounds)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
        applyColorConfig(dotPaint, style.eyeDotColor, qrBounds)

        val x = (originCol + quietZone) * moduleSize
        val y = (originRow + quietZone) * moduleSize

        // Three concentric layers: 7-module outer, 5-module separator, 3-module center
        val outerRect = RectF(x, y, x + 7 * moduleSize, y + 7 * moduleSize)
        val innerRect = RectF(
            x + moduleSize, y + moduleSize,
            x + 6 * moduleSize, y + 6 * moduleSize,
        )
        val dotRect = RectF(
            x + 2 * moduleSize, y + 2 * moduleSize,
            x + 5 * moduleSize, y + 5 * moduleSize,
        )

        val eyePath = Path()

        when (style.eyeStyle) {
            EyeStyle.Square -> {
                drawRect(outerRect, framePaint)
                drawRect(innerRect, bgPaint)
                drawRect(dotRect, dotPaint)
            }

            EyeStyle.Circle -> {
                drawOval(outerRect, framePaint)
                drawOval(innerRect, bgPaint)
                drawOval(dotRect, dotPaint)
            }

            EyeStyle.RoundedSquare -> {
                val outerRadius = moduleSize * 2f
                val innerRadius = moduleSize * 1.4f
                val dotRadius = moduleSize * 0.9f
                drawRoundRect(outerRect, outerRadius, outerRadius, framePaint)
                drawRoundRect(innerRect, innerRadius, innerRadius, bgPaint)
                drawRoundRect(dotRect, dotRadius, dotRadius, dotPaint)
            }

            EyeStyle.Leaf -> {
                drawLeafRect(outerRect, moduleSize * 2.5f, framePaint, eyePath, corner)
                drawLeafRect(innerRect, moduleSize * 1.8f, bgPaint, eyePath, corner)
                drawLeafRect(dotRect, moduleSize * 1.0f, dotPaint, eyePath, corner)
            }

            EyeStyle.Diamond -> {
                drawDiamondRect(outerRect, framePaint, eyePath)
                drawDiamondRect(innerRect, bgPaint, eyePath)
                drawDiamondRect(dotRect, dotPaint, eyePath)
            }
        }
    }

    /**
     * Draws a "leaf" shape: a rounded rect with two diagonally opposite corners rounded,
     * oriented so that the sharp tips point outward from the QR code.
     *
     * The [corner] determines which diagonal pair gets the sharp tips:
     * - [Corner.TopRight] / [Corner.BottomLeft]: sharp at top-right & bottom-left (default leaf)
     * - [Corner.TopLeft] / [Corner.BottomRight]: sharp at top-left & bottom-right (mirrored)
     */
    private fun Canvas.drawLeafRect(
        rect: RectF,
        radius: Float,
        paint: Paint,
        eyePath: Path,
        corner: Corner = Corner.TopRight,
    ) {
        val r = radius
        val radii = when (corner) {
            Corner.TopLeft, Corner.BottomRight -> floatArrayOf(
                0f, 0f,     // top-left (sharp)
                r, r,       // top-right (rounded)
                0f, 0f,     // bottom-right (sharp)
                r, r,       // bottom-left (rounded)
            )

            Corner.TopRight, Corner.BottomLeft -> floatArrayOf(
                r, r,       // top-left (rounded)
                0f, 0f,     // top-right (sharp)
                r, r,       // bottom-right (rounded)
                0f, 0f,     // bottom-left (sharp)
            )
        }
        eyePath.apply {
            rewind()
            addRoundRect(rect, radii, Path.Direction.CW)
            close()
        }
        drawPath(eyePath, paint)
    }

    /** Draws a diamond (rotated square) inscribed in [rect]. */
    private fun Canvas.drawDiamondRect(rect: RectF, paint: Paint, eyePath: Path) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        eyePath.apply {
            rewind()
            moveTo(cx, rect.top)
            lineTo(rect.right, cy)
            lineTo(cx, rect.bottom)
            lineTo(rect.left, cy)
            close()
        }
        drawPath(eyePath, paint)
    }

    // endregion

    // region Logo Rendering

    /**
     * Draws the logo centered on the QR code. The logo is:
     * 1. Surrounded by a padding area filled with the background color
     * 2. Clipped to the configured [LogoShape] (Circle, RoundedRect, Square)
     * 3. Scaled to fit within the configured size fraction of the QR code
     *
     * @param logoBitmap The logo bitmap to draw.
     * @param logoData Configuration for size, padding, and shape ([LogoData]).
     * @param backgroundColor The QR background color, used for the padding area.
     * @param qrBounds The bounding rect of the QR code area (excluding quiet zone).
     */
    private fun Canvas.drawLogo(
        logoBitmap: Bitmap,
        logoData: LogoData,
        backgroundColor: ColorData,
        qrBounds: RectF,
    ) {
        val qrSize = qrBounds.width()
        val cx = qrBounds.centerX()
        val cy = qrBounds.centerY()

        // Total area includes logo + padding on each side
        val totalFraction = logoData.size + logoData.padding * 2
        val totalHalf = qrSize * totalFraction / 2f

        // The outer rect covers logo + padding (background-filled border)
        val outerRect = RectF(
            cx - totalHalf,
            cy - totalHalf,
            cx + totalHalf,
            cy + totalHalf,
        )

        // The inner rect is just the logo area (inside the padding)
        val logoHalf = qrSize * logoData.size / 2f
        val logoRect = RectF(
            cx - logoHalf,
            cy - logoHalf,
            cx + logoHalf,
            cy + logoHalf,
        )

        // Step 1: Draw the padded background area with the configured shape
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        applyColorConfig(bgPaint, backgroundColor, outerRect)
        drawShapedRect(outerRect, logoData.shape, bgPaint)

        // Step 2: Clip to the logo shape and draw the bitmap
        save()
        clipToShape(logoRect, logoData.shape)

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }
        drawBitmap(logoBitmap, null, logoRect, logoPaint)
        restore()
    }

    /**
     * Draws a filled shape (Circle, RoundedRect, or Square) within the given [rect].
     */
    private fun Canvas.drawShapedRect(rect: RectF, shape: LogoShape, paint: Paint) {
        when (shape) {
            LogoShape.Circle -> {
                drawOval(rect, paint)
            }

            LogoShape.RoundedRect -> {
                val cornerRadius = rect.width() * 0.15f
                drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }

            LogoShape.Square -> {
                drawRect(rect, paint)
            }
        }
    }

    /**
     * Applies a clip to this [Canvas] in the configured [shape] within the given [rect].
     */
    private fun Canvas.clipToShape(rect: RectF, shape: LogoShape) {
        val clipPath = Path()
        when (shape) {
            LogoShape.Circle -> {
                clipPath.addOval(rect, Path.Direction.CW)
            }

            LogoShape.RoundedRect -> {
                val cornerRadius = rect.width() * 0.15f
                clipPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
            }

            LogoShape.Square -> {
                clipPath.addRect(rect, Path.Direction.CW)
            }
        }
        clipPath(clipPath)
    }

    // endregion

    // region Color & Gradient Helpers

    /**
     * Configures the given [paint] based on the [colorData].
     *
     * - [ColorData.Solid]: Sets the paint color directly, clears any shader.
     * - [ColorData.LinearGradient]: Sets a linear gradient shader across the [bounds]
     *   at the specified angle, so all elements sharing the same bounds get a continuous gradient.
     * - [ColorData.RadialGradient]: Sets a radial gradient shader centered within the [bounds]
     *   at the specified fractional center position.
     *
     * @param paint The [Paint] to configure.
     * @param colorData The color configuration to apply.
     * @param bounds The bounding rectangle over which gradients are mapped.
     */
    private fun applyColorConfig(paint: Paint, colorData: ColorData, bounds: RectF) {
        when (colorData) {
            is ColorData.Solid -> {
                paint.shader = null
                paint.color = colorData.color.toInt()
            }

            is ColorData.LinearGradient -> {
                val colors = colorData.colors.map { it.toInt() }.toIntArray()
                if (colors.size < 2) {
                    // Fallback: treat as solid if fewer than 2 colors
                    paint.shader = null
                    paint.color = colors.firstOrNull() ?: 0xFF000000.toInt()
                    return
                }

                // Compute gradient start/end points from angle
                val angleRad = Math.toRadians(colorData.angleDegrees.toDouble())
                val cx = bounds.centerX()
                val cy = bounds.centerY()
                val halfW = bounds.width() / 2f
                val halfH = bounds.height() / 2f

                // The gradient line extends from center outward in the direction of the angle.
                // We project onto the bounding box diagonal for full coverage.
                val diagonal = kotlin.math.sqrt(
                    (halfW * halfW + halfH * halfH).toDouble()
                ).toFloat()

                val dx = cos(angleRad).toFloat() * diagonal
                val dy = sin(angleRad).toFloat() * diagonal

                val x0 = cx - dx
                val y0 = cy - dy
                val x1 = cx + dx
                val y1 = cy + dy

                paint.shader = android.graphics.LinearGradient(
                    x0, y0, x1, y1,
                    colors,
                    null, // evenly distributed
                    android.graphics.Shader.TileMode.CLAMP,
                )
            }

            is ColorData.RadialGradient -> {
                val colors = colorData.colors.map { it.toInt() }.toIntArray()
                if (colors.size < 2) {
                    paint.shader = null
                    paint.color = colors.firstOrNull() ?: 0xFF000000.toInt()
                    return
                }

                val cx = bounds.left + bounds.width() * colorData.centerX
                val cy = bounds.top + bounds.height() * colorData.centerY

                // Radius covers the entire bounds from the center point
                val maxDx = maxOf(cx - bounds.left, bounds.right - cx)
                val maxDy = maxOf(cy - bounds.top, bounds.bottom - cy)
                val radius = kotlin.math.sqrt(
                    (maxDx * maxDx + maxDy * maxDy).toDouble()
                ).toFloat()

                paint.shader = android.graphics.RadialGradient(
                    cx, cy, radius,
                    colors,
                    null, // evenly distributed
                    android.graphics.Shader.TileMode.CLAMP,
                )
            }
        }
    }

    // endregion
}
