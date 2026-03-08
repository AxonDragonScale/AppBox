package com.axondragonscale.limitless.ui.habit.stats.widget

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.ChartEmptyState
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.commons.compose.chart.TrendLine
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.habit.stats.HistoryPeriod
import com.axondragonscale.limitless.ui.habit.stats.model.DailyHistoryData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A card showing daily record counts as a bar chart over a selectable time window.
 *
 * Four [FilterChip]s at the top (1M / 3M / 6M / 1Y, defaulting to 3M) let the user
 * zoom in or out. Chip selection is purely **local** UI state — no ViewModel event is
 * fired. The full 1-year dataset is loaded once; filtering to the chosen window is done
 * client-side using [derivedStateOf] so recomposition only triggers when the visible
 * dataset actually changes.
 *
 * A [TrendLine] is overlaid on the same [Box] as the [BarChart]. Because no bar labels
 * are used both primitives share the same coordinate space, keeping the line aligned
 * with bar tops.
 */
@Composable
fun DailyHistoryChart(
    modifier: Modifier = Modifier,
    data: DailyHistoryData,
    habitColor: HabitColor,
) {
    var selectedPeriod by remember { mutableStateOf(HistoryPeriod.THREE_MONTHS) }
    val color = habitColor.color
    // Computed once per composition session; date doesn't change while the screen is open.
    val today = remember { LocalDate.today() }

    // Build the full date sequence for the selected window.
    // `remember(data, today)` recreates the derivedStateOf block whenever the dataset
    // changes. Inside the lambda, reading `selectedPeriod` (a Compose State) makes the
    // derivedStateOf track it — so switching chips recomputes without recreating the block.
    val filteredEntries: List<Pair<LocalDate, Int>> by remember(data, today) {
        derivedStateOf {
            val cutoff = today.minus(selectedPeriod.months, DateTimeUnit.MONTH)
            buildList {
                var current = cutoff
                while (current <= today) {
                    add(current to (data.dayCounts[current] ?: 0))
                    current = current.plus(1, DateTimeUnit.DAY)
                }
            }
        }
    }

    StatCard(
        modifier = modifier,
        title = "Daily History",
    ) {
        // Period selector
        if (filteredEntries.all { (_, count) -> count == 0 }) {
            ChartEmptyState()
        } else {
            val bars = filteredEntries.map { (_, count) ->
                // No label — omitting labels keeps BarChart and TrendLine in the same
                // coordinate space so the line aligns with bar tops (see class KDoc).
                BarEntry(
                    value = count.toFloat(),
                    color = color.copy(alpha = 0.55f),
                )
            }
            val trendPoints = filteredEntries.map { (_, count) -> count.toFloat() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                BarChart(
                    modifier = Modifier.matchParentSize(),
                    bars = bars,
                    style = BarChartStyle.default(
                        barColor = color.copy(alpha = 0.55f),
                        barSpacing = 0.dp,
                        barCornerRadius = 1.dp,
                    ),
                )
//                TrendLine(
//                    modifier = Modifier.matchParentSize(),
//                    points = trendPoints,
//                    color = color,
//                    strokeWidth = 2.dp,
//                    smooth = true,
//                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryPeriod.entries.forEach { period ->
                FilterChip(
                    modifier = Modifier.height(24.dp),
                    selected = period == selectedPeriod,
                    onClick = { selectedPeriod = period },
                    label = {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode — 3M", showBackground = true)
@Preview(name = "Dark Mode — 3M", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        DailyHistoryChart(
            modifier = Modifier.padding(16.dp),
            data = DailyHistoryData(
                dayCounts = buildMap {
                    // Seed ~60 random-ish days over the last 3 months
                    repeat(60) { i ->
                        val date = LocalDate.today().minus(i * 1, DateTimeUnit.DAY)
                        if (i % 3 != 0) put(date, (1..5).random())
                    }
                },
            ),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "Consistent Habit — Light", showBackground = true)
@Preview(name = "Consistent Habit — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun ConsistentPreview() {
    LimitlessTheme {
        DailyHistoryChart(
            modifier = Modifier.padding(16.dp),
            data = DailyHistoryData(
                dayCounts = buildMap {
                    repeat(90) { i ->
                        val date = LocalDate.today().minus(i, DateTimeUnit.DAY)
                        put(date, if (i % 7 < 5) 3 else 1)
                    }
                },
            ),
            habitColor = HabitColor.GREEN,
        )
    }
}

@Preview(name = "Sparse Data — Light", showBackground = true)
@Preview(name = "Sparse Data — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SparsePreview() {
    LimitlessTheme {
        DailyHistoryChart(
            modifier = Modifier.padding(16.dp),
            data = DailyHistoryData(
                dayCounts = buildMap {
                    listOf(1, 5, 14, 22, 30, 45, 60).forEach { daysAgo ->
                        put(LocalDate.today().minus(daysAgo, DateTimeUnit.DAY), (1..4).random())
                    }
                },
            ),
            habitColor = HabitColor.ORANGE,
        )
    }
}

@Preview(name = "No Data — Light", showBackground = true)
@Preview(name = "No Data — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun NoDataPreview() {
    LimitlessTheme {
        DailyHistoryChart(
            modifier = Modifier.padding(16.dp),
            data = DailyHistoryData(dayCounts = emptyMap()),
            habitColor = HabitColor.TEAL,
        )
    }
}

// endregion
