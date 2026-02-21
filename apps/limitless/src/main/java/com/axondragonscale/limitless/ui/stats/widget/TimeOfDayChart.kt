package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.hourLabel
import com.axondragonscale.commons.compose.ChartEmptyState
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartOrientation
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.TimeOfDayData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color

// Hours at which axis tick labels are displayed; all other hours are unlabelled.
private val TICK_HOURS = setOf(0, 3, 6, 9, 12, 15, 18, 21)

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A card showing a 24-bar vertical histogram of record counts per hour of the day.
 * Axis tick labels are shown only at key hours (every 3 hours). The peak hour bar is
 * rendered at full habit color opacity; all other bars fade proportionally so the
 * distribution gradient is immediately visible. A "Peak: X AM/PM" label is displayed
 * in the card header when a peak hour exists.
 */
@Composable
fun TimeOfDayChart(
    modifier: Modifier = Modifier,
    data: TimeOfDayData,
    habitColor: HabitColor,
) {

    val peakLabel = if (data.peakHour >= 0) "Peak: ${hourLabel(data.peakHour)}" else null

    StatCard(
        modifier = modifier,
        title = "Time of Day",
        trailingContent = peakLabel?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = habitColor.color,
                )
            }
        },
    ) {
        if (data.hourCounts.isEmpty() || data.hourCounts.all { it == 0 }) {
            ChartEmptyState()
        } else {
            val bars = data.hourCounts.mapIndexed { hour, count ->
                // Fade from 20% at sparse hours up to 100% at the peak
                val fraction = count.toFloat() / (data.hourCounts.maxOrNull() ?: 0)
                BarEntry(
                    value = count.toFloat(),
                    label = if (hour in TICK_HOURS) hourLabel(hour) else "",
                    color = habitColor.color.copy(alpha = 0.2f + 0.8f * fraction),
                )
            }
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                bars = bars,
                style = BarChartStyle.default(
                    orientation = BarChartOrientation.Vertical,
                    barSpacing = 2.dp,
                    barCornerRadius = 2.dp,
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
        TimeOfDayChart(
            modifier = Modifier.padding(16.dp),
            data = TimeOfDayData(
                hourCounts = List(24) { hour ->
                    when (hour) {
                        in 0..5 -> 0
                        in 6..8 -> (hour - 5) * 3
                        9 -> 12
                        in 10..11 -> 8
                        12 -> 6
                        in 13..17 -> 4
                        18 -> 10
                        19 -> 14
                        20 -> 9
                        in 21..23 -> 2
                        else -> 0
                    }
                },
                peakHour = 19,
            ),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "Morning Peak — Light", showBackground = true)
@Preview(name = "Morning Peak — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MorningPeakPreview() {
    LimitlessTheme {
        TimeOfDayChart(
            modifier = Modifier.padding(16.dp),
            data = TimeOfDayData(
                hourCounts = List(24) { hour ->
                    when (hour) {
                        6 -> 5
                        7 -> 18
                        8 -> 22
                        9 -> 15
                        10 -> 8
                        else -> if (hour in 11..23) 2 else 0
                    }
                },
                peakHour = 8,
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
        TimeOfDayChart(
            modifier = Modifier.padding(16.dp),
            data = TimeOfDayData(
                hourCounts = List(24) { 0 },
                peakHour = -1,
            ),
            habitColor = HabitColor.TEAL,
        )
    }
}

// endregion
