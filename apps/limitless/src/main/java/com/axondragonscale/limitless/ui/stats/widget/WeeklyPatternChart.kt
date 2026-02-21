package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartOrientation
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.WeeklyPatternData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.commons.compose.ChartEmptyState
import com.axondragonscale.commons.dayOfWeekAbbreviations
import com.axondragonscale.limitless.ui.theme.color

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A card showing a horizontal bar chart of average records per day of the week (Mon–Sun).
 * The day with the highest average is highlighted at full habit color opacity; all other
 * bars are shown at reduced opacity so the peak day stands out at a glance.
 */
@Composable
fun WeeklyPatternChart(
    modifier: Modifier = Modifier,
    data: WeeklyPatternData,
    habitColor: HabitColor,
) {
    val maxAvg = data.dayAverages.maxOrNull() ?: 0f
    val isEmpty = maxAvg == 0f

    StatCard(
        modifier = modifier,
        title = "Weekly Pattern",
    ) {
        if (isEmpty) {
            ChartEmptyState()
        } else {
            val bars = data.dayAverages.mapIndexed { index, avg ->
                BarEntry(
                    value = avg,
                    label = dayOfWeekAbbreviations[index],
                    color = habitColor.color.copy(alpha = if (avg == maxAvg) 1f else 0.5f),
                )
            }
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                bars = bars,
                style = BarChartStyle.default(
                    orientation = BarChartOrientation.Horizontal,
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
        WeeklyPatternChart(
            modifier = Modifier.padding(16.dp),
            data = WeeklyPatternData(
                dayAverages = listOf(2.5f, 3.1f, 1.8f, 4.2f, 3.7f, 5.0f, 2.0f),
            ),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "Single Peak — Light", showBackground = true)
@Preview(name = "Single Peak — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SinglePeakPreview() {
    LimitlessTheme {
        WeeklyPatternChart(
            modifier = Modifier.padding(16.dp),
            data = WeeklyPatternData(
                dayAverages = listOf(1.0f, 1.2f, 0.8f, 1.5f, 2.0f, 8.0f, 6.5f),
            ),
            habitColor = HabitColor.ORANGE,
        )
    }
}

@Preview(name = "No Data — Light", showBackground = true)
@Preview(name = "No Data — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoDataPreview() {
    LimitlessTheme {
        WeeklyPatternChart(
            modifier = Modifier.padding(16.dp),
            data = WeeklyPatternData(
                dayAverages = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
            ),
            habitColor = HabitColor.GREEN,
        )
    }
}

// endregion
