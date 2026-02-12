package com.axondragonscale.wallmatic.ui.wallpaper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A fullscreen crop overlay composable.
 *
 * The overlay fills the entire screen. A centered crop rectangle is shown with
 * draggable corner and edge handles. The area outside the crop rect is dimmed.
 *
 * Supports single-finger drag on handles/edges/center and two-finger pinch to
 * uniformly resize the crop rect.
 *
 * All interaction happens in screen coordinates. On change, the crop rect is
 * mapped back to image pixel coordinates via [onCropChange].
 */

// Touch target radii for handle hit detection
private const val CORNER_TOUCH_RADIUS = 72f
private const val EDGE_TOUCH_RADIUS = 56f
private const val MIN_CROP_SIZE = 80f

// Visual sizes
private const val CORNER_LINE_LENGTH = 32f
private const val CORNER_LINE_THICKNESS = 5f
private const val BORDER_THICKNESS = 2f
private const val GRID_LINE_THICKNESS = 0.8f

private enum class DragHandle {
    NONE,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP, BOTTOM, LEFT, RIGHT,
    CENTER,
}

@Composable
fun CropOverlay(
    modifier: Modifier = Modifier,
    imageSize: IntSize,
    cropRect: android.graphics.Rect?,
    onCropChange: (android.graphics.Rect) -> Unit,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // The crop rect in screen coordinates
    var screenCropRect by remember(canvasSize) {
        mutableStateOf(
            if (canvasSize.width > 0 && canvasSize.height > 0) {
                screenRectFromImageRect(cropRect, canvasSize, imageSize)
            } else {
                Rect.Zero
            }
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = size
                screenCropRect = screenRectFromImageRect(cropRect, size, imageSize)
            }
            .pointerInput(canvasSize) {
                if (canvasSize.width <= 0 || canvasSize.height <= 0) return@pointerInput
                val bounds = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())

                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    val handle = detectHandle(firstDown.position, screenCropRect)

                    var isPinching = false
                    var prevPinchDistance = 0f

                    // Consume the down event so it doesn't propagate
                    firstDown.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.isEmpty()) {
                            // All fingers lifted – emit final rect
                            onCropChange(
                                imageRectFromScreenRect(screenCropRect, canvasSize, imageSize)
                            )
                            break
                        }

                        if (pressed.size >= 2) {
                            // --- Pinch gesture ---
                            val p1 = pressed[0].position
                            val p2 = pressed[1].position
                            val currentDist = distance(p1, p2)

                            if (!isPinching) {
                                isPinching = true
                                prevPinchDistance = currentDist
                            } else if (prevPinchDistance > 0f) {
                                val scaleFactor = currentDist / prevPinchDistance
                                screenCropRect = applyPinch(
                                    rect = screenCropRect,
                                    scaleFactor = scaleFactor,
                                    bounds = bounds,
                                )
                                prevPinchDistance = currentDist
                            }

                            pressed.forEach { it.consume() }
                        } else if (pressed.size == 1 && !isPinching) {
                            // --- Single-finger drag ---
                            val change = pressed.first()
                            if (change.positionChanged()) {
                                val delta = change.position - change.previousPosition
                                screenCropRect = applyDrag(
                                    rect = screenCropRect,
                                    handle = handle,
                                    delta = delta,
                                    bounds = bounds,
                                )
                                change.consume()
                            }
                        } else if (pressed.size == 1 && isPinching) {
                            // One finger lifted during pinch – just consume, don't drag
                            pressed.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@Canvas

        drawDimOverlay(screenCropRect)
        drawCropBorder(screenCropRect)
        drawGridLines(screenCropRect)
        drawCornerHandles(screenCropRect)
        drawEdgeHandles(screenCropRect)
    }
}

// ------------------------------------------------------------------
// Gesture helpers
// ------------------------------------------------------------------

private fun distance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

// ------------------------------------------------------------------
// Coordinate mapping
// ------------------------------------------------------------------

/**
 * Creates a screen-space crop rect from an image-space [android.graphics.Rect].
 * If [imageRect] is null, returns a centered rect with 10 % inset on each side.
 */
private fun screenRectFromImageRect(
    imageRect: android.graphics.Rect?,
    canvasSize: IntSize,
    imageSize: IntSize,
): Rect {
    val cw = canvasSize.width.toFloat()
    val ch = canvasSize.height.toFloat()

    if (imageRect == null) {
        val insetX = cw * 0.10f
        val insetY = ch * 0.10f
        return Rect(insetX, insetY, cw - insetX, ch - insetY)
    }

    val iw = imageSize.width.toFloat()
    val ih = imageSize.height.toFloat()
    if (iw <= 0f || ih <= 0f) return Rect(0f, 0f, cw, ch)

    val scale = maxOf(cw / iw, ch / ih) // ContentScale.Crop
    val offsetX = (cw - iw * scale) / 2f
    val offsetY = (ch - ih * scale) / 2f

    return Rect(
        left = (imageRect.left * scale + offsetX).coerceIn(0f, cw),
        top = (imageRect.top * scale + offsetY).coerceIn(0f, ch),
        right = (imageRect.right * scale + offsetX).coerceIn(0f, cw),
        bottom = (imageRect.bottom * scale + offsetY).coerceIn(0f, ch),
    )
}

/**
 * Converts a screen-space crop rect back to image pixel coordinates.
 */
private fun imageRectFromScreenRect(
    screenRect: Rect,
    canvasSize: IntSize,
    imageSize: IntSize,
): android.graphics.Rect {
    val cw = canvasSize.width.toFloat()
    val ch = canvasSize.height.toFloat()
    val iw = imageSize.width.toFloat()
    val ih = imageSize.height.toFloat()
    if (iw <= 0f || ih <= 0f) return android.graphics.Rect(0, 0, imageSize.width, imageSize.height)

    val scale = maxOf(cw / iw, ch / ih)
    val offsetX = (cw - iw * scale) / 2f
    val offsetY = (ch - ih * scale) / 2f

    return android.graphics.Rect(
        ((screenRect.left - offsetX) / scale).toInt().coerceIn(0, imageSize.width),
        ((screenRect.top - offsetY) / scale).toInt().coerceIn(0, imageSize.height),
        ((screenRect.right - offsetX) / scale).toInt().coerceIn(0, imageSize.width),
        ((screenRect.bottom - offsetY) / scale).toInt().coerceIn(0, imageSize.height),
    )
}

// ------------------------------------------------------------------
// Hit detection
// ------------------------------------------------------------------

private fun detectHandle(offset: Offset, rect: Rect): DragHandle {
    // Corners first (largest touch target, highest priority)
    if (nearPoint(offset, rect.topLeft, CORNER_TOUCH_RADIUS)) return DragHandle.TOP_LEFT
    if (nearPoint(offset, Offset(rect.right, rect.top), CORNER_TOUCH_RADIUS)) return DragHandle.TOP_RIGHT
    if (nearPoint(offset, Offset(rect.left, rect.bottom), CORNER_TOUCH_RADIUS)) return DragHandle.BOTTOM_LEFT
    if (nearPoint(offset, Offset(rect.right, rect.bottom), CORNER_TOUCH_RADIUS)) return DragHandle.BOTTOM_RIGHT

    // Edges
    if (nearHLine(offset, rect.top, rect.left, rect.right, EDGE_TOUCH_RADIUS)) return DragHandle.TOP
    if (nearHLine(offset, rect.bottom, rect.left, rect.right, EDGE_TOUCH_RADIUS)) return DragHandle.BOTTOM
    if (nearVLine(offset, rect.left, rect.top, rect.bottom, EDGE_TOUCH_RADIUS)) return DragHandle.LEFT
    if (nearVLine(offset, rect.right, rect.top, rect.bottom, EDGE_TOUCH_RADIUS)) return DragHandle.RIGHT

    // Inside → move entire rect
    if (rect.contains(offset)) return DragHandle.CENTER

    return DragHandle.NONE
}

private fun nearPoint(point: Offset, target: Offset, radius: Float): Boolean =
    abs(point.x - target.x) < radius && abs(point.y - target.y) < radius

private fun nearHLine(point: Offset, y: Float, left: Float, right: Float, threshold: Float): Boolean =
    abs(point.y - y) < threshold && point.x in (left - threshold)..(right + threshold)

private fun nearVLine(point: Offset, x: Float, top: Float, bottom: Float, threshold: Float): Boolean =
    abs(point.x - x) < threshold && point.y in (top - threshold)..(bottom + threshold)

// ------------------------------------------------------------------
// Drag logic
// ------------------------------------------------------------------

private fun applyDrag(
    rect: Rect,
    handle: DragHandle,
    delta: Offset,
    bounds: Size,
): Rect {
    val minW = MIN_CROP_SIZE
    val minH = MIN_CROP_SIZE

    return when (handle) {
        DragHandle.TOP_LEFT -> rect.copy(
            left = (rect.left + delta.x).coerceIn(0f, rect.right - minW),
            top = (rect.top + delta.y).coerceIn(0f, rect.bottom - minH),
        )

        DragHandle.TOP_RIGHT -> rect.copy(
            right = (rect.right + delta.x).coerceIn(rect.left + minW, bounds.width),
            top = (rect.top + delta.y).coerceIn(0f, rect.bottom - minH),
        )

        DragHandle.BOTTOM_LEFT -> rect.copy(
            left = (rect.left + delta.x).coerceIn(0f, rect.right - minW),
            bottom = (rect.bottom + delta.y).coerceIn(rect.top + minH, bounds.height),
        )

        DragHandle.BOTTOM_RIGHT -> rect.copy(
            right = (rect.right + delta.x).coerceIn(rect.left + minW, bounds.width),
            bottom = (rect.bottom + delta.y).coerceIn(rect.top + minH, bounds.height),
        )

        DragHandle.TOP -> rect.copy(
            top = (rect.top + delta.y).coerceIn(0f, rect.bottom - minH),
        )

        DragHandle.BOTTOM -> rect.copy(
            bottom = (rect.bottom + delta.y).coerceIn(rect.top + minH, bounds.height),
        )

        DragHandle.LEFT -> rect.copy(
            left = (rect.left + delta.x).coerceIn(0f, rect.right - minW),
        )

        DragHandle.RIGHT -> rect.copy(
            right = (rect.right + delta.x).coerceIn(rect.left + minW, bounds.width),
        )

        DragHandle.CENTER -> {
            val w = rect.width
            val h = rect.height
            val newLeft = (rect.left + delta.x).coerceIn(0f, bounds.width - w)
            val newTop = (rect.top + delta.y).coerceIn(0f, bounds.height - h)
            Rect(newLeft, newTop, newLeft + w, newTop + h)
        }

        DragHandle.NONE -> rect
    }
}

// ------------------------------------------------------------------
// Pinch logic
// ------------------------------------------------------------------

/**
 * Scale the crop rect uniformly around its center by [scaleFactor].
 * The result is clamped to [bounds] and the minimum crop size.
 */
private fun applyPinch(
    rect: Rect,
    scaleFactor: Float,
    bounds: Size,
): Rect {
    val cx = rect.center.x
    val cy = rect.center.y
    val halfW = (rect.width * scaleFactor / 2f)
    val halfH = (rect.height * scaleFactor / 2f)

    // Enforce minimum size
    val clampedHalfW = halfW.coerceAtLeast(MIN_CROP_SIZE / 2f)
    val clampedHalfH = halfH.coerceAtLeast(MIN_CROP_SIZE / 2f)

    var newLeft = cx - clampedHalfW
    var newTop = cy - clampedHalfH
    var newRight = cx + clampedHalfW
    var newBottom = cy + clampedHalfH

    // Clamp to bounds, shifting the rect if needed to keep it within bounds
    if (newLeft < 0f) {
        newRight -= newLeft // shift right by the overshoot
        newLeft = 0f
    }
    if (newTop < 0f) {
        newBottom -= newTop
        newTop = 0f
    }
    if (newRight > bounds.width) {
        newLeft -= (newRight - bounds.width)
        newRight = bounds.width
    }
    if (newBottom > bounds.height) {
        newTop -= (newBottom - bounds.height)
        newBottom = bounds.height
    }

    // Final clamp in case shifting pushed the other side out
    newLeft = newLeft.coerceAtLeast(0f)
    newTop = newTop.coerceAtLeast(0f)
    newRight = newRight.coerceAtMost(bounds.width)
    newBottom = newBottom.coerceAtMost(bounds.height)

    return Rect(newLeft, newTop, newRight, newBottom)
}

// ------------------------------------------------------------------
// Drawing helpers
// ------------------------------------------------------------------

private fun DrawScope.drawDimOverlay(cropRect: Rect) {
    clipRect(
        left = cropRect.left,
        top = cropRect.top,
        right = cropRect.right,
        bottom = cropRect.bottom,
        clipOp = ClipOp.Difference,
    ) {
        drawRect(color = Color.Black.copy(alpha = 0.55f), size = size)
    }
}

private fun DrawScope.drawCropBorder(rect: Rect) {
    drawRect(
        color = Color.White,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = BORDER_THICKNESS),
    )
}

private fun DrawScope.drawGridLines(rect: Rect) {
    val thirdW = rect.width / 3f
    val thirdH = rect.height / 3f
    val color = Color.White.copy(alpha = 0.40f)

    for (i in 1..2) {
        val x = rect.left + thirdW * i
        drawLine(color, Offset(x, rect.top), Offset(x, rect.bottom), GRID_LINE_THICKNESS)
    }
    for (i in 1..2) {
        val y = rect.top + thirdH * i
        drawLine(color, Offset(rect.left, y), Offset(rect.right, y), GRID_LINE_THICKNESS)
    }
}

private fun DrawScope.drawCornerHandles(rect: Rect) {
    val color = Color.White
    drawCornerL(rect.left, rect.top, dx = 1f, dy = 1f, color)
    drawCornerL(rect.right, rect.top, dx = -1f, dy = 1f, color)
    drawCornerL(rect.left, rect.bottom, dx = 1f, dy = -1f, color)
    drawCornerL(rect.right, rect.bottom, dx = -1f, dy = -1f, color)
}

private fun DrawScope.drawCornerL(cx: Float, cy: Float, dx: Float, dy: Float, color: Color) {
    drawLine(
        color = color,
        start = Offset(cx, cy),
        end = Offset(cx + CORNER_LINE_LENGTH * dx, cy),
        strokeWidth = CORNER_LINE_THICKNESS,
    )
    drawLine(
        color = color,
        start = Offset(cx, cy),
        end = Offset(cx, cy + CORNER_LINE_LENGTH * dy),
        strokeWidth = CORNER_LINE_THICKNESS,
    )
}

private fun DrawScope.drawEdgeHandles(rect: Rect) {
    val color = Color.White
    val len = 20f
    val thick = CORNER_LINE_THICKNESS

    drawLine(color, Offset(rect.center.x - len, rect.top), Offset(rect.center.x + len, rect.top), thick)
    drawLine(color, Offset(rect.center.x - len, rect.bottom), Offset(rect.center.x + len, rect.bottom), thick)
    drawLine(color, Offset(rect.left, rect.center.y - len), Offset(rect.left, rect.center.y + len), thick)
    drawLine(color, Offset(rect.right, rect.center.y - len), Offset(rect.right, rect.center.y + len), thick)
}
