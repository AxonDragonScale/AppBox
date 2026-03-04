package com.axondragonscale.limitless.ui.habit.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.ChartEmptyState
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.commons.monthAbbreviations
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.model.MonthCount
import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number

private val BarWidth = 32.dp
private val BarSpacing = 12.dp
private val ChartHeight = 180.dp
private const val MinMonths = 12

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A horizontally-scrollable card showing total records per calendar month as a bar chart.
 *
 * Reuses [BarChart] from `:core:commons` with a pixel-precise fixed width so each bar is
 * exactly [BarWidth] wide regardless of how many months are displayed:
 *   chartWidth = BarWidth × n + BarSpacing × (n − 1)
 * This satisfies BarChart's internal formula: barWidth = (totalWidth − spacing × (n−1)) / n = BarWidth.
 *
 * At least [MinMonths] months are always shown — if [MonthlyTrendData.monthCounts] has fewer
 * entries, empty placeholder months (count = 0) are prepended before the earliest real month.
 * Empty months render with no bar and no value label — just the month abbreviation — clearly
 * communicating absence of data. If [MonthlyTrendData.monthCounts] is entirely empty,
 * a [ChartEmptyState] is shown instead.
 *
 * The scroll position is pinned to the trailing edge on first composition so the most recent
 * month is always in view. [Int.MAX_VALUE] is used as a sentinel; [androidx.compose.foundation.ScrollState]
 * clamps it to [androidx.compose.foundation.ScrollState.maxValue] automatically.
 *
 * Visual accents per bar:
 * - Current month  → full habit color, habit-color value + category labels
 * - Past months    → habit color at 45 % opacity, [MaterialTheme.colorScheme.onSurfaceVariant] labels
 * - Empty/padded   → no bar drawn (value = 0), [MaterialTheme.colorScheme.onSurfaceVariant] category label
 */
@Composable
fun MonthlyTrendChart(
    modifier: Modifier = Modifier,
    data: MonthlyTrendData,
    color: Color,
) {
    val today = remember { LocalDate.today() }
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val scrollState = rememberScrollState()

    // Pad with empty months at the front so the chart always shows >= MinMonths bars.
    // An empty monthCounts list stays empty here and triggers ChartEmptyState below.
    val displayMonths = remember(data, today) {
        when {
            data.monthCounts.isEmpty() -> emptyList()
            data.monthCounts.size >= MinMonths -> data.monthCounts
            else -> {
                val first = data.monthCounts.first()
                val earliest = LocalDate(first.year, first.month, 1)
                val paddingNeeded = MinMonths - data.monthCounts.size
                val paddedMonths = (paddingNeeded downTo 1).map { offset ->
                    val date = earliest.minus(offset, DateTimeUnit.MONTH)
                    MonthCount(year = date.year, month = date.month.number, count = 0)
                }
                paddedMonths + data.monthCounts
            }
        }
    }

    // Scroll to the rightmost (most recent) bar whenever the dataset changes.
    LaunchedEffect(displayMonths.size) {
        scrollState.scrollTo(Int.MAX_VALUE)
    }

    StatCard(
        modifier = modifier,
        title = "Monthly Trend",
    ) {
        if (displayMonths.isEmpty()) {
            ChartEmptyState()
        } else {
            val bars = displayMonths.map { monthCount ->
                val isCurrent = monthCount.year == today.year && monthCount.month == today.month.number
                val isEmpty = monthCount.count == 0
                BarEntry(
                    value = monthCount.count.toFloat(),
                    label = monthAbbreviations[monthCount.month - 1],
                    valueLabel = if (!isEmpty) "${monthCount.count}" else "",
                    color = when {
                        isCurrent -> color
                        !isEmpty -> color.copy(alpha = 0.45f)
                        else -> null  // bar has height 0, color is irrelevant
                    },
                    labelColor = if (isCurrent) color else onSurfaceVariant,
                    valueLabelColor = if (isCurrent) color else onSurfaceVariant,
                )
            }

            // chartWidth ensures BarChart's internal bar-width formula yields exactly BarWidth:
            //   barWidth = (chartWidth − BarSpacing × (n−1)) / n = BarWidth  ✓
            val chartWidth = BarWidth * bars.size + BarSpacing * (bars.size - 1).coerceAtLeast(0)

            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                BarChart(
                    modifier = Modifier.width(chartWidth).height(ChartHeight),
                    bars = bars,
                    style = BarChartStyle.default(
                        barSpacing = BarSpacing,
                        barCornerRadius = 4.dp,
                    ),
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(
                monthCounts = listOf(
                    MonthCount(year = 2025, month = 2, count = 18),
                    MonthCount(year = 2025, month = 3, count = 24),
                    MonthCount(year = 2025, month = 4, count = 31),
                    MonthCount(year = 2025, month = 5, count = 27),
                    MonthCount(year = 2025, month = 6, count = 35),
                    MonthCount(year = 2025, month = 7, count = 12),
                ),
            ),
            color = HabitColor.PURPLE.color,
        )
    }
}

@Preview(name = "Few Months (padded) — Light", showBackground = true)
@Preview(name = "Few Months (padded) — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FewMonthsPreview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(
                monthCounts = listOf(
                    MonthCount(year = 2025, month = 6, count = 8),
                    MonthCount(year = 2025, month = 7, count = 21),
                ),
            ),
            color = HabitColor.TEAL.color,
        )
    }
}

@Preview(name = "Single Month (padded) — Light", showBackground = true)
@Preview(name = "Single Month (padded) — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SingleMonthPreview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(
                monthCounts = listOf(
                    MonthCount(year = 2025, month = 7, count = 14),
                ),
            ),
            color = HabitColor.ORANGE.color,
        )
    }
}

@Preview(name = "Year Spanning — Light", showBackground = true)
@Preview(name = "Year Spanning — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun YearSpanningPreview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(
                monthCounts = listOf(
                    MonthCount(year = 2024, month = 10, count = 9),
                    MonthCount(year = 2024, month = 11, count = 14),
                    MonthCount(year = 2024, month = 12, count = 22),
                    MonthCount(year = 2025, month = 1, count = 30),
                    MonthCount(year = 2025, month = 2, count = 28),
                    MonthCount(year = 2025, month = 3, count = 35),
                    MonthCount(year = 2025, month = 4, count = 41),
                    MonthCount(year = 2025, month = 5, count = 38),
                    MonthCount(year = 2025, month = 6, count = 45),
                    MonthCount(year = 2025, month = 7, count = 17),
                ),
            ),
            color = HabitColor.GREEN.color,
        )
    }
}

@Preview(name = "Growing Trend — Light", showBackground = true)
@Preview(name = "Growing Trend — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GrowingTrendPreview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(
                monthCounts = listOf(
                    MonthCount(year = 2025, month = 2, count = 5),
                    MonthCount(year = 2025, month = 3, count = 10),
                    MonthCount(year = 2025, month = 4, count = 18),
                    MonthCount(year = 2025, month = 5, count = 25),
                    MonthCount(year = 2025, month = 6, count = 38),
                    MonthCount(year = 2025, month = 7, count = 44),
                ),
            ),
            color = HabitColor.GREEN.color,
        )
    }
}

@Preview(name = "Empty — Light", showBackground = true)
@Preview(name = "Empty — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        MonthlyTrendChart(
            modifier = Modifier.padding(16.dp),
            data = MonthlyTrendData(monthCounts = emptyList()),
            color = HabitColor.BLUE.color,
        )
    }
}

// endregion
