package com.axondragonscale.commons.compose.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.chart.RingChartLabelStyle.Companion.default

/**
 * Created by Ronak Harkhani on 21/02/26
 *
 * Visual styling for the background track of a [RingChart].
 *
 * @param color Color of the progress arc.
 * @param trackColor Color of the background track ring. Defaults to [color] at 15% opacity.
 * @param strokeWidth Thickness of the ring stroke (shared by track and progress arc).
 * @param strokeCap Cap style for the arc ends.
 */
data class RingChartTrackStyle(
    val color: Color,
    val trackColor: Color,
    val strokeWidth: Dp = 8.dp,
    val strokeCap: StrokeCap = StrokeCap.Round,
) {
    companion object {
        @Composable
        fun default(
            color: Color = MaterialTheme.colorScheme.primary,
            trackColor: Color = color.copy(alpha = 0.15f),
            strokeWidth: Dp = 8.dp,
            strokeCap: StrokeCap = StrokeCap.Round,
        ) = RingChartTrackStyle(color, trackColor, strokeWidth, strokeCap)
    }
}

/**
 * Visual styling for the center label of a [RingChart].
 *
 * Use the [default] factory to create an instance that resolves theme-aware defaults
 * from [MaterialTheme]. Fields can be selectively overridden.
 *
 * @param style [TextStyle] used for label rendering.
 * @param color Color for the label text.
 */
data class RingChartLabelStyle(
    val style: TextStyle,
    val color: Color,
) {
    companion object {
        @Composable
        fun default(
            style: TextStyle = MaterialTheme.typography.titleMedium,
            color: Color = MaterialTheme.colorScheme.onSurface,
        ) = RingChartLabelStyle(style, color)
    }
}

/**
 * A generic, data-agnostic ring/arc chart composable.
 *
 * Draws a circular progress arc over a background track with an optional center label.
 * Uses [Modifier.drawWithCache] internally so draw commands are only recomputed
 * when input data actually changes.
 *
 * @param modifier Modifier for sizing and positioning. The ring is drawn to fill the
 *   available space (inset by half the stroke width).
 * @param progress Progress value in the range 0f..1f. Values outside this range are coerced.
 * @param startAngle Starting angle in degrees. -90f starts from the top (12 o'clock position).
 * @param label Optional text drawn at the center of the ring (e.g., "82%").
 * @param trackStyle Visual configuration for the ring (arc color, track color, stroke).
 * @param labelStyle Visual configuration for the center label text.
 */
@Composable
fun RingChart(
    modifier: Modifier = Modifier,
    progress: Float,
    startAngle: Float = -90f,
    label: String = "",
    trackStyle: RingChartTrackStyle = RingChartTrackStyle.default(),
    labelStyle: RingChartLabelStyle = RingChartLabelStyle.default(),
) {
    val textMeasurer = rememberTextMeasurer()


    Box(
        modifier = modifier.drawWithCache {
            val clampedProgress = progress.coerceIn(0f, 1f)
            val sweepAngle = 360f * clampedProgress
            val strokePx = trackStyle.strokeWidth.toPx()
            val stroke = Stroke(width = strokePx, cap = trackStyle.strokeCap)

            // Inset the arc so the stroke doesn't clip at the edges
            val inset = strokePx / 2f
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            // Pre-measure the label text
            val labelResult = if (label.isNotEmpty()) {
                textMeasurer.measure(
                    text = label,
                    style = labelStyle.style.copy(
                        textAlign = TextAlign.Center,
                        color = labelStyle.color,
                    ),
                    maxLines = 1,
                )
            } else null

            onDrawBehind {
                // Background track (full circle)
                drawArc(
                    color = trackStyle.trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = stroke,
                )

                // Progress arc
                if (clampedProgress > 0f) {
                    drawArc(
                        color = trackStyle.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }

                // Center label
                labelResult?.let { result ->
                    val textX = (size.width - result.size.width) / 2f
                    val textY = (size.height - result.size.height) / 2f
                    drawText(
                        textLayoutResult = result,
                        topLeft = Offset(textX, textY),
                    )
                }
            }
        }
    )
}
