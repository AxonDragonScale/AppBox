package com.axondragonscale.commons.compose.chart

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 21/02/26
 *
 * Orientation for [BarChart] rendering.
 */
enum class BarChartOrientation {
    /** Bars grow upward from the bottom. X-axis = categories, Y-axis = values. */
    Vertical,

    /** Bars grow rightward from the left. Y-axis = categories, X-axis = values. */
    Horizontal,
}

/**
 * A single bar in a [BarChart].
 *
 * @param value The numeric value this bar represents. Bar size is proportional to the max value.
 * @param label Optional label displayed alongside the bar (below for vertical, left for horizontal).
 * @param valueLabel Optional label drawn above the bar (vertical only). Typically the raw count.
 * @param color Optional per-bar color override. `null` falls back to [BarChartStyle.barColor].
 * @param labelColor Optional per-label color override. `null` falls back to [BarChartStyle.labelColor].
 * @param valueLabelColor Optional per-value-label color override. `null` falls back to [BarChartStyle.valueLabelColor].
 */
data class BarEntry(
    val value: Float,
    val label: String = "",
    val valueLabel: String = "",
    val color: Color? = null,
    val labelColor: Color? = null,
    val valueLabelColor: Color? = null,
)

/**
 * Visual styling for a [BarChart].
 *
 * Use the [default] factory to create an instance that resolves theme-aware defaults
 * from [MaterialTheme]. Fields can be selectively overridden.
 *
 * @param orientation Whether bars grow vertically (upward) or horizontally (rightward).
 * @param barColor Default color for bars that don't specify [BarEntry.color].
 * @param barCornerRadius Corner radius applied to bar ends.
 * @param barSpacing Space between adjacent bars.
 * @param labelStyle [TextStyle] used for category label rendering (below/left of bar).
 * @param labelColor Default color for labels that don't specify [BarEntry.labelColor].
 * @param valueLabelStyle [TextStyle] used for value labels drawn above each bar (vertical only).
 * @param valueLabelColor Default color for value labels that don't specify [BarEntry.valueLabelColor].
 */
data class BarChartStyle(
    val orientation: BarChartOrientation,
    val barColor: Color,
    val barCornerRadius: Dp,
    val barSpacing: Dp,
    val labelStyle: TextStyle,
    val labelColor: Color,
    val valueLabelStyle: TextStyle,
    val valueLabelColor: Color,
) {
    companion object {
        @Composable
        fun default(
            orientation: BarChartOrientation = BarChartOrientation.Vertical,
            barColor: Color = MaterialTheme.colorScheme.primary,
            barCornerRadius: Dp = 4.dp,
            barSpacing: Dp = 4.dp,
            labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
            labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
            valueLabelStyle: TextStyle = labelStyle,
            valueLabelColor: Color = labelColor,
        ) = BarChartStyle(
            orientation = orientation,
            barColor = barColor,
            barCornerRadius = barCornerRadius,
            barSpacing = barSpacing,
            labelStyle = labelStyle,
            labelColor = labelColor,
            valueLabelStyle = valueLabelStyle,
            valueLabelColor = valueLabelColor
        )
    }
}

/**
 * A generic, data-agnostic bar chart composable.
 *
 * Draws proportional bars with optional labels and per-bar color customization.
 * Uses [Modifier.drawWithCache] internally so draw commands are only recomputed
 * when input data actually changes.
 *
 * @param modifier Modifier for sizing and positioning.
 * @param bars The bar entries to render. Each [BarEntry] carries its value, label, and
 *   optional color overrides.
 * @param style Visual configuration for the chart. Use [BarChartStyle.default] to create.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    bars: List<BarEntry>,
    style: BarChartStyle = BarChartStyle.default(),
) {
    if (bars.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier.drawWithCache {
            val count = bars.size
            val maxValue = bars.maxOf { it.value }.coerceAtLeast(1f)
            val hasLabels = bars.any { it.label.isNotEmpty() }
            val hasValueLabels = bars.any { it.valueLabel.isNotEmpty() }
            val cornerRadiusPx = style.barCornerRadius.toPx()
            val spacingPx = style.barSpacing.toPx()
            val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

            // Pre-resolve per-bar colors so the draw lambda just indexes into lists
            val barColors = bars.map { it.color ?: style.barColor }
            val labelColors = bars.map { it.labelColor ?: style.labelColor }
            val valueLabelColors = bars.map { it.valueLabelColor ?: style.valueLabelColor }

            when (style.orientation) {
                BarChartOrientation.Vertical -> {

                    // Measure category labels (below bars)
                    val labelResults = if (hasLabels) {
                        bars.mapIndexed { index, bar ->
                            textMeasurer.measure(
                                text = bar.label,
                                style = style.labelStyle.copy(
                                    textAlign = TextAlign.Center,
                                    color = labelColors[index],
                                ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    } else emptyList()

                    // Measure value labels (above bars) to reserve space at the top
                    val valueLabelResults = if (hasValueLabels) {
                        bars.mapIndexed { index, bar ->
                            textMeasurer.measure(
                                text = bar.valueLabel,
                                style = style.valueLabelStyle.copy(
                                    textAlign = TextAlign.Center,
                                    color = valueLabelColors[index],
                                ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    } else emptyList()

                    val labelHeight = if (hasLabels) labelResults.maxOf { it.size.height }.toFloat() else 0f
                    val labelPadding = if (hasLabels) 4.dp.toPx() else 0f
                    val valueLabelHeight = if (hasValueLabels) valueLabelResults.maxOf { it.size.height }.toFloat() else 0f
                    val valueLabelPadding = if (hasValueLabels) 3.dp.toPx() else 0f

                    // Bar zone sits between the value label space at the top and category label space at the bottom
                    val chartOffsetY = valueLabelHeight + valueLabelPadding
                    val chartHeight = size.height - chartOffsetY - labelHeight - labelPadding
                    val totalSpacing = spacingPx * (count - 1)
                    val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(1f)

                    onDrawBehind {
                        bars.forEachIndexed { index, bar ->
                            val fraction = bar.value / maxValue
                            val bHeight = (chartHeight * fraction).coerceAtLeast(0f)
                            val x = index * (barWidth + spacingPx)
                            val y = chartOffsetY + chartHeight - bHeight

                            drawRoundRect(
                                color = barColors[index],
                                topLeft = Offset(x, y),
                                size = Size(barWidth, bHeight),
                                cornerRadius = cornerRadius,
                            )

                            if (hasValueLabels && bar.valueLabel.isNotEmpty()) {
                                val valueLabelResult = valueLabelResults[index]
                                val valueLabelX = x + (barWidth - valueLabelResult.size.width) / 2f
                                val valueLabelY = y - valueLabelResult.size.height - valueLabelPadding
                                drawText(
                                    textLayoutResult = valueLabelResult,
                                    topLeft = Offset(valueLabelX, valueLabelY),
                                )
                            }

                            if (hasLabels) {
                                val labelResult = labelResults[index]
                                val labelX = x + (barWidth - labelResult.size.width) / 2f
                                val labelY = chartOffsetY + chartHeight + labelPadding
                                drawText(
                                    textLayoutResult = labelResult,
                                    topLeft = Offset(labelX, labelY),
                                )
                            }
                        }
                    }
                }

                BarChartOrientation.Horizontal -> {
                    // Measure labels to reserve space on the left
                    val labelResults = if (hasLabels) {
                        bars.mapIndexed { index, bar ->
                            textMeasurer.measure(
                                text = bar.label,
                                style = style.labelStyle.copy(color = labelColors[index]),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    } else emptyList()

                    val labelWidth = if (hasLabels) {
                        labelResults.maxOf { it.size.width }.toFloat()
                    } else 0f
                    val labelPadding = if (hasLabels) 8.dp.toPx() else 0f

                    val chartWidth = size.width - labelWidth - labelPadding
                    val totalSpacing = spacingPx * (count - 1)
                    val barHeight = ((size.height - totalSpacing) / count).coerceAtLeast(1f)

                    onDrawBehind {
                        bars.forEachIndexed { index, bar ->
                            val fraction = bar.value / maxValue
                            val bWidth = (chartWidth * fraction).coerceAtLeast(0f)
                            val x = labelWidth + labelPadding
                            val y = index * (barHeight + spacingPx)

                            drawRoundRect(
                                color = barColors[index],
                                topLeft = Offset(x, y),
                                size = Size(bWidth, barHeight),
                                cornerRadius = cornerRadius,
                            )

                            if (hasLabels) {
                                val labelResult = labelResults[index]
                                val labelX = labelWidth - labelResult.size.width
                                val labelY = y + (barHeight - labelResult.size.height) / 2f
                                drawText(
                                    textLayoutResult = labelResult,
                                    topLeft = Offset(labelX, labelY),
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// region Previews

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerticalBarChartPreview() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val values = listOf(3f, 7f, 2f, 9f, 5f, 8f, 4f)
    MaterialTheme {
        Surface {
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                bars = values.zip(days) { v, d -> BarEntry(v, d) },
                style = BarChartStyle.default(barSpacing = 6.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HorizontalBarChartPreview() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val values = listOf(5f, 8f, 3f, 9f, 6f, 7f, 4f)
    MaterialTheme {
        Surface {
            BarChart(
                modifier = Modifier.size(width = 300.dp, height = 200.dp),
                bars = values.zip(days) { v, d -> BarEntry(v, d) },
                style = BarChartStyle.default(
                    orientation = BarChartOrientation.Horizontal,
                    barSpacing = 6.dp,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarChartNoLabelsPreview() {
    MaterialTheme {
        Surface {
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                bars = List(24) {
                    BarEntry(if (it in 6..22) (it - 5).toFloat() * 2 else it.toFloat())
                },
                style = BarChartStyle.default(barSpacing = 1.dp, barCornerRadius = 2.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarChartPerBarColorsPreview() {
    val colors = listOf(
        Color(0xFFD32F2F),
        Color(0xFFEF6C00),
        Color(0xFFF9A825),
        Color(0xFF2E7D32),
        Color(0xFF1565C0),
        Color(0xFF283593),
        Color(0xFF6650A4),
    )
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val values = listOf(3f, 7f, 2f, 9f, 5f, 8f, 4f)
    MaterialTheme {
        Surface {
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                bars = values.indices.map { i -> BarEntry(value = values[i], label = days[i], color = colors[i]) },
                style = BarChartStyle.default(barSpacing = 6.dp),
            )
        }
    }
}

// endregion
