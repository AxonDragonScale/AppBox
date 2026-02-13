package com.axondragonscale.qrqanvas.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import com.axondragonscale.qrqanvas.model.ColorConfig
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.LogoConfig
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
class QRRenderer {

    private val modulePath = Path()
    private val eyePath = Path()

    /**
     * Renders the given [bitMatrix] using the provided [style] into a [Bitmap].
     *
     * The output bitmap is square with dimensions [QRStyle.size] x [QRStyle.size].
     * A quiet zone of [QRStyle.quietZone] modules is added around the QR code.
     *
     * @param bitMatrix The QR code matrix where each cell is one module (from [QRGenerator]).
     * @param style The visual style configuration for rendering.
     * @param logoBitmap Optional logo bitmap to draw centered over the QR code.
     *   When provided, [QRStyle.logoConfig] must also be non-null to configure size, padding, and shape.
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
        drawBackground(canvas, style.backgroundColor, bgRect)

        // The bounding rect of the actual QR code area (excluding quiet zone)
        val qrLeft = style.quietZone * moduleSize
        val qrTop = style.quietZone * moduleSize
        val qrRight = qrLeft + matrixSize * moduleSize
        val qrBottom = qrTop + matrixSize * moduleSize
        val qrBounds = RectF(qrLeft, qrTop, qrRight, qrBottom)

        // Compute logo exclusion zone (if logo is present)
        val logoExclusionRect = computeLogoExclusionRect(style.logoConfig, logoBitmap, qrBounds)

        // Identify finder pattern regions
        val finderRegions = getFinderPatternRegions(matrixSize)

        // Draw data modules (everything outside finder regions and logo exclusion zone)
        drawDataModules(
            canvas, bitMatrix, style, moduleSize, matrixSize,
            finderRegions, qrBounds, logoExclusionRect,
        )

        // Draw finder patterns separately
        drawFinderPatterns(canvas, style, moduleSize, matrixSize, qrBounds)

        // Draw logo on top of everything
        if (logoBitmap != null && style.logoConfig != null) {
            drawLogo(canvas, logoBitmap, style.logoConfig, style.backgroundColor, qrBounds)
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
        logoConfig: LogoConfig?,
        logoBitmap: Bitmap?,
        qrBounds: RectF,
    ): RectF? {
        if (logoConfig == null || logoBitmap == null) return null

        val qrSize = qrBounds.width()
        val cx = qrBounds.centerX()
        val cy = qrBounds.centerY()

        // Total size includes the logo area + padding on each side
        val totalFraction = logoConfig.size + logoConfig.padding * 2
        val halfExclusion = qrSize * totalFraction / 2f

        return RectF(
            cx - halfExclusion,
            cy - halfExclusion,
            cx + halfExclusion,
            cy + halfExclusion,
        )
    }

    private fun drawBackground(canvas: Canvas, colorConfig: ColorConfig, rect: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        applyColorConfig(paint, colorConfig, rect)
        canvas.drawRect(rect, paint)
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

    private fun drawDataModules(
        canvas: Canvas,
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

                drawModule(canvas, rect, style.dotStyle, paint)
            }
        }
    }

    // region Dot Style Rendering

    /**
     * Draws a single data module with the given [dotStyle] shape inside [rect].
     */
    private fun drawModule(canvas: Canvas, rect: RectF, dotStyle: DotStyle, paint: Paint) {
        when (dotStyle) {
            DotStyle.Square -> drawSquareModule(canvas, rect, paint)
            DotStyle.Circle -> drawCircleModule(canvas, rect, paint)
            DotStyle.RoundedSquare -> drawRoundedSquareModule(canvas, rect, paint)
            DotStyle.Diamond -> drawDiamondModule(canvas, rect, paint)
            DotStyle.Star -> drawStarModule(canvas, rect, paint)
            DotStyle.Heart -> drawHeartModule(canvas, rect, paint)
        }
    }

    private fun drawSquareModule(canvas: Canvas, rect: RectF, paint: Paint) {
        canvas.drawRect(rect, paint)
    }

    private fun drawCircleModule(canvas: Canvas, rect: RectF, paint: Paint) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = rect.width().coerceAtMost(rect.height()) / 2f
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawRoundedSquareModule(canvas: Canvas, rect: RectF, paint: Paint) {
        val cornerRadius = rect.width() * 0.3f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    private fun drawDiamondModule(canvas: Canvas, rect: RectF, paint: Paint) {
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
        canvas.drawPath(modulePath, paint)
    }

    private fun drawStarModule(canvas: Canvas, rect: RectF, paint: Paint) {
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
        canvas.drawPath(modulePath, paint)
    }

    private fun drawHeartModule(canvas: Canvas, rect: RectF, paint: Paint) {
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
        canvas.drawPath(modulePath, paint)
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
    private fun drawFinderPatterns(
        canvas: Canvas,
        style: QRStyle,
        moduleSize: Float,
        matrixSize: Int,
        qrBounds: RectF,
    ) {
        val finderOrigins = listOf(
            0 to 0,                         // Top-left
            matrixSize - 7 to 0,            // Top-right
            0 to matrixSize - 7,            // Bottom-left
        )

        for ((originCol, originRow) in finderOrigins) {
            drawFinderPattern(canvas, style, moduleSize, originCol, originRow, qrBounds)
        }
    }

    private fun drawFinderPattern(
        canvas: Canvas,
        style: QRStyle,
        moduleSize: Float,
        originCol: Int,
        originRow: Int,
        qrBounds: RectF,
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

        when (style.eyeStyle) {
            EyeStyle.Square -> {
                canvas.drawRect(outerRect, framePaint)
                canvas.drawRect(innerRect, bgPaint)
                canvas.drawRect(dotRect, dotPaint)
            }

            EyeStyle.Circle -> {
                canvas.drawOval(outerRect, framePaint)
                canvas.drawOval(innerRect, bgPaint)
                canvas.drawOval(dotRect, dotPaint)
            }

            EyeStyle.RoundedSquare -> {
                val outerRadius = moduleSize * 2f
                val innerRadius = moduleSize * 1.4f
                val dotRadius = moduleSize * 0.9f
                canvas.drawRoundRect(outerRect, outerRadius, outerRadius, framePaint)
                canvas.drawRoundRect(innerRect, innerRadius, innerRadius, bgPaint)
                canvas.drawRoundRect(dotRect, dotRadius, dotRadius, dotPaint)
            }

            EyeStyle.Leaf -> {
                drawLeafRect(canvas, outerRect, moduleSize * 2.5f, framePaint)
                drawLeafRect(canvas, innerRect, moduleSize * 1.8f, bgPaint)
                drawLeafRect(canvas, dotRect, moduleSize * 1.0f, dotPaint)
            }

            EyeStyle.Diamond -> {
                drawDiamondRect(canvas, outerRect, framePaint)
                drawDiamondRect(canvas, innerRect, bgPaint)
                drawDiamondRect(canvas, dotRect, dotPaint)
            }
        }
    }

    /**
     * Draws a "leaf" shape: a rounded rect with only the top-left and
     * bottom-right corners rounded, giving an organic leaf appearance.
     */
    private fun drawLeafRect(canvas: Canvas, rect: RectF, radius: Float, paint: Paint) {
        val r = radius
        val radii = floatArrayOf(
            r, r,       // top-left
            0f, 0f,     // top-right
            r, r,       // bottom-right
            0f, 0f,     // bottom-left
        )
        eyePath.apply {
            rewind()
            addRoundRect(rect, radii, Path.Direction.CW)
            close()
        }
        canvas.drawPath(eyePath, paint)
    }

    /** Draws a diamond (rotated square) inscribed in [rect]. */
    private fun drawDiamondRect(canvas: Canvas, rect: RectF, paint: Paint) {
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
        canvas.drawPath(eyePath, paint)
    }

    // endregion

    // region Logo Rendering

    /**
     * Draws the logo centered on the QR code. The logo is:
     * 1. Surrounded by a padding area filled with the background color
     * 2. Clipped to the configured [LogoShape] (Circle, RoundedRect, Square)
     * 3. Scaled to fit within the configured size fraction of the QR code
     *
     * @param canvas The canvas to draw on.
     * @param logoBitmap The logo bitmap to draw.
     * @param logoConfig Configuration for size, padding, and shape.
     * @param backgroundColor The QR background color, used for the padding area.
     * @param qrBounds The bounding rect of the QR code area (excluding quiet zone).
     */
    private fun drawLogo(
        canvas: Canvas,
        logoBitmap: Bitmap,
        logoConfig: LogoConfig,
        backgroundColor: ColorConfig,
        qrBounds: RectF,
    ) {
        val qrSize = qrBounds.width()
        val cx = qrBounds.centerX()
        val cy = qrBounds.centerY()

        // Total area includes logo + padding on each side
        val totalFraction = logoConfig.size + logoConfig.padding * 2
        val totalHalf = qrSize * totalFraction / 2f

        // The outer rect covers logo + padding (background-filled border)
        val outerRect = RectF(
            cx - totalHalf,
            cy - totalHalf,
            cx + totalHalf,
            cy + totalHalf,
        )

        // The inner rect is just the logo area (inside the padding)
        val logoHalf = qrSize * logoConfig.size / 2f
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
        drawShapedRect(canvas, outerRect, logoConfig.shape, bgPaint)

        // Step 2: Clip to the logo shape and draw the bitmap
        canvas.save()
        clipToShape(canvas, logoRect, logoConfig.shape)

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }
        canvas.drawBitmap(logoBitmap, null, logoRect, logoPaint)
        canvas.restore()
    }

    /**
     * Draws a filled shape (Circle, RoundedRect, or Square) within the given [rect].
     */
    private fun drawShapedRect(canvas: Canvas, rect: RectF, shape: LogoShape, paint: Paint) {
        when (shape) {
            LogoShape.Circle -> {
                canvas.drawOval(rect, paint)
            }

            LogoShape.RoundedRect -> {
                val cornerRadius = rect.width() * 0.15f
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }

            LogoShape.Square -> {
                canvas.drawRect(rect, paint)
            }
        }
    }

    /**
     * Applies a clip to the [canvas] in the configured [shape] within the given [rect].
     */
    private fun clipToShape(canvas: Canvas, rect: RectF, shape: LogoShape) {
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
        canvas.clipPath(clipPath)
    }

    // endregion

    // region Color & Gradient Helpers

    /**
     * Configures the given [paint] based on the [colorConfig].
     *
     * - [ColorConfig.Solid]: Sets the paint color directly, clears any shader.
     * - [ColorConfig.LinearGradient]: Sets a linear gradient shader across the [bounds]
     *   at the specified angle, so all elements sharing the same bounds get a continuous gradient.
     * - [ColorConfig.RadialGradient]: Sets a radial gradient shader centered within the [bounds]
     *   at the specified fractional center position.
     *
     * @param paint The [Paint] to configure.
     * @param colorConfig The color configuration to apply.
     * @param bounds The bounding rectangle over which gradients are mapped.
     */
    private fun applyColorConfig(paint: Paint, colorConfig: ColorConfig, bounds: RectF) {
        when (colorConfig) {
            is ColorConfig.Solid -> {
                paint.shader = null
                paint.color = colorConfig.color.toInt()
            }

            is ColorConfig.LinearGradient -> {
                val colors = colorConfig.colors.map { it.toInt() }.toIntArray()
                if (colors.size < 2) {
                    // Fallback: treat as solid if fewer than 2 colors
                    paint.shader = null
                    paint.color = colors.firstOrNull() ?: 0xFF000000.toInt()
                    return
                }

                // Compute gradient start/end points from angle
                val angleRad = Math.toRadians(colorConfig.angleDegrees.toDouble())
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

            is ColorConfig.RadialGradient -> {
                val colors = colorConfig.colors.map { it.toInt() }.toIntArray()
                if (colors.size < 2) {
                    paint.shader = null
                    paint.color = colors.firstOrNull() ?: 0xFF000000.toInt()
                    return
                }

                val cx = bounds.left + bounds.width() * colorConfig.centerX
                val cy = bounds.top + bounds.height() * colorConfig.centerY

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
