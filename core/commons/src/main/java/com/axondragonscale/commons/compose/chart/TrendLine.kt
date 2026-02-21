package com.axondragonscale.commons.compose.chart

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 21/02/26
 *
 * A generic, data-agnostic trend line composable.
 *
 * Draws a line connecting sequential data points, optionally smoothed using
 * Catmull-Rom spline interpolation converted to cubic Bézier curves.
 * Uses [Modifier.drawWithCache] internally so the [Path] and draw commands
 * are only recomputed when input data actually changes.
 *
 * Typically used as an overlay on top of a [BarChart] — give both the same
 * size and the trend line will align with the bar centers.
 *
 * @param modifier Modifier for sizing and positioning.
 * @param points Y-values for each data point. The points are evenly spaced
 *   across the width of the composable. Needs at least 2 points to draw.
 * @param color Color of the line stroke.
 * @param strokeWidth Thickness of the line.
 * @param strokeCap Cap style for line ends.
 * @param smooth When true, uses Catmull-Rom → cubic Bézier smoothing.
 *   When false, draws straight line segments between points.
 */
@Composable
fun TrendLine(
    modifier: Modifier = Modifier,
    points: List<Float>,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    smooth: Boolean = true,
) {
    if (points.size < 2) return

    Box(
        modifier = modifier.drawWithCache {
            val maxValue = points.max().coerceAtLeast(1f)
            val count = points.size
            val stepX = if (count > 1) size.width / (count - 1) else 0f

            // Map points to pixel coordinates (y is inverted: 0 at top)
            val pixelPoints = points.mapIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value / maxValue) * size.height
                x to y
            }

            val path = Path().apply {
                if (smooth && pixelPoints.size >= 3) {
                    buildSmoothPath(pixelPoints)
                } else {
                    buildLinearPath(pixelPoints)
                }
            }

            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = strokeCap,
                join = StrokeJoin.Round,
            )

            onDrawBehind {
                drawPath(
                    path = path,
                    color = color,
                    style = stroke,
                )
            }
        }
    )
}

/**
 * Builds a straight-line path through [points].
 */
private fun Path.buildLinearPath(points: List<Pair<Float, Float>>) {
    val (startX, startY) = points.first()
    moveTo(startX, startY)
    for (i in 1 until points.size) {
        val (x, y) = points[i]
        lineTo(x, y)
    }
}

/**
 * Builds a smooth path through [points] using Catmull-Rom spline interpolation
 * converted to cubic Bézier control points.
 *
 * For each segment between points[i] and points[i+1], four points are used
 * (p0, p1, p2, p3) to compute two Bézier control points. The first and last
 * points are duplicated to handle boundary conditions.
 */
private fun Path.buildSmoothPath(points: List<Pair<Float, Float>>) {
    val (startX, startY) = points.first()
    moveTo(startX, startY)

    // Catmull-Rom tension factor (0.5 = centripetal, standard)
    val tension = 0.5f

    for (i in 0 until points.size - 1) {
        // Clamp indices to handle boundaries
        val p0 = points[(i - 1).coerceAtLeast(0)]
        val p1 = points[i]
        val p2 = points[(i + 1).coerceAtMost(points.size - 1)]
        val p3 = points[(i + 2).coerceAtMost(points.size - 1)]

        // Convert Catmull-Rom to cubic Bézier control points
        val cp1x = p1.first + (p2.first - p0.first) * tension / 3f
        val cp1y = p1.second + (p2.second - p0.second) * tension / 3f
        val cp2x = p2.first - (p3.first - p1.first) * tension / 3f
        val cp2y = p2.second - (p3.second - p1.second) * tension / 3f

        cubicTo(cp1x, cp1y, cp2x, cp2y, p2.first, p2.second)
    }
}

// region Previews

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmoothTrendLinePreview() {
    MaterialTheme {
        Surface {
            TrendLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp),
                points = listOf(2f, 5f, 3f, 8f, 6f, 9f, 4f, 7f, 10f, 6f, 8f, 5f),
                smooth = true,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinearTrendLinePreview() {
    MaterialTheme {
        Surface {
            TrendLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp),
                points = listOf(2f, 5f, 3f, 8f, 6f, 9f, 4f, 7f, 10f, 6f, 8f, 5f),
                smooth = false,
                strokeWidth = 2.dp,
                color = Color(0xFFEF6C00),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrendLineTwoPointsPreview() {
    MaterialTheme {
        Surface {
            TrendLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp),
                points = listOf(3f, 7f),
                smooth = true,
            )
        }
    }
}

// endregion
