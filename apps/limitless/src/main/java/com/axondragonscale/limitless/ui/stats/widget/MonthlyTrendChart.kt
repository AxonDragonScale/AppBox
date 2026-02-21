package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.ChartEmptyState
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartOrientation
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.commons.monthAbbreviations
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.MonthCount
import com.axondragonscale.limitless.ui.stats.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A card showing a vertical bar chart of total records per calendar month.
 * The current month's bar is rendered at full habit color opacity to draw the eye
 * to recency; all past months use a muted variant at reduced opacity.
 * Data is rendered in chronological order (oldest → newest, left → right).
 */
@Composable
fun MonthlyTrendChart(
    modifier: Modifier = Modifier,
    data: MonthlyTrendData,
    habitColor: HabitColor,
) {
    val color = habitColor.color
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    StatCard(
        modifier = modifier,
        title = "Monthly Trend",
    ) {
        if (data.monthCounts.isEmpty()) {
            ChartEmptyState()
        } else {
            val bars = data.monthCounts.map { monthCount ->
                val isCurrent = monthCount.year == today.year && monthCount.month == today.month.number
                BarEntry(
                    value = monthCount.count.toFloat(),
                    label = monthAbbreviations[monthCount.month - 1],
                    color = if (isCurrent) color else color.copy(alpha = 0.4f),
                )
            }
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                bars = bars,
                style = BarChartStyle.default(
                    orientation = BarChartOrientation.Vertical,
                    barSpacing = 8.dp,
                    barCornerRadius = 4.dp,
                ),
            )
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
            habitColor = HabitColor.PURPLE,
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
            habitColor = HabitColor.GREEN,
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
            habitColor = HabitColor.BLUE,
        )
    }
}

// endregion
