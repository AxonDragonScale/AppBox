package com.axondragonscale.limitless.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.commons.abbreviated
import com.axondragonscale.commons.compose.thenIf
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 21/07/25
 */

internal val HeatMapCellSize = 14.dp
internal val HeatMapCellSpacing = 3.dp
internal val HeatMapCellCornerRadius = 3.dp
internal val HeatMapDayLabelWidth = 28.dp

private val DayLabels = listOf("M", "", "W", "", "F", "", "S")

/**
 * A week column in the heatmap grid.
 */
internal data class WeekData(
    val days: List<LocalDate?>, // 7 entries, null for days outside the range
    val weekStartDate: LocalDate,
)

/**
 * A reusable heat map grid that renders week columns, day cells, month labels, and day-of-week
 * labels. Scrolls to the most recent (trailing) week on first composition.
 *
 * Extracted from [HabitHeatMap] so it can be embedded in any context — per-habit views, global
 * stats cards, etc. — without the habit-name header or record button.
 *
 * @param dayCounts     Map of date → record count for coloring the cells.
 * @param maxCount      The maximum count across all dates, used to determine color intensity.
 * @param color         Primary color used for filled cells; intensity is derived from [maxCount].
 * @param highlightCurrentDay Whether to outline today's cell with a border.
 * @param totalWeeks    Number of week columns to display (default 26 ≈ 6 months).
 */
@Composable
fun HeatMapGrid(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    color: Color,
    highlightCurrentDay: Boolean = true,
    totalWeeks: Int = 26,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val weeks = remember(today, totalWeeks) { buildWeeks(today, totalWeeks) }
    val monthLabels = remember(weeks) { buildMonthLabels(weeks) }
    val listState = rememberLazyListState()

    // Scroll to the end (most recent) on first composition
    LaunchedEffect(weeks.size) {
        if (weeks.isNotEmpty()) listState.scrollToItem(weeks.lastIndex)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month labels row
        MonthLabelsRow(
            monthLabels = monthLabels,
            listState = listState,
            weeks = weeks,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Grid: day labels + heatmap cells
        Row {
            // Day-of-week labels
            DayLabelsColumn()

            // Heatmap grid
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(HeatMapCellSpacing),
            ) {
                items(weeks, key = { it.weekStartDate }) { week ->
                    WeekColumn(
                        weekData = week,
                        dayCounts = dayCounts,
                        maxCount = maxCount,
                        today = today,
                        highlightCurrentDay = highlightCurrentDay,
                        habitColor = color,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthLabelsRow(
    monthLabels: List<Pair<Int, String>>, // weekIndex to month name
    listState: LazyListState,
    weeks: List<WeekData>,
) {
    val monthLabelListState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        monthLabelListState.scrollToItem(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset,
        )
    }

    Row {
        // Spacer for day label column width
        Spacer(modifier = Modifier.width(HeatMapDayLabelWidth))

        LazyRow(
            state = monthLabelListState,
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(HeatMapCellSpacing),
        ) {
            items(weeks.size, key = { it }) { weekIndex ->
                val label = monthLabels.find { it.first == weekIndex }
                Box(
                    modifier = Modifier
                        .width(HeatMapCellSize)
                        .height(16.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    if (label != null) {
                        Text(
                            text = label.second,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayLabelsColumn() {
    Column(
        modifier = Modifier.width(HeatMapDayLabelWidth),
        verticalArrangement = Arrangement.spacedBy(HeatMapCellSpacing),
    ) {
        DayLabels.forEach { label ->
            Box(
                modifier = Modifier.size(HeatMapCellSize),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun WeekColumn(
    weekData: WeekData,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    today: LocalDate,
    highlightCurrentDay: Boolean,
    habitColor: Color,
) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val highlightColor = MaterialTheme.colorScheme.onSurface

    Column(
        verticalArrangement = Arrangement.spacedBy(HeatMapCellSpacing),
    ) {
        weekData.days.forEach { date ->
            if (date != null && date <= today) {
                val count = dayCounts[date] ?: 0
                val color = heatMapCellColor(count, maxCount, emptyColor, habitColor)
                val isToday = highlightCurrentDay && date == today
                HeatMapCell(color = color, highlighted = isToday, highlightColor = highlightColor)
            } else {
                // Empty/transparent cell for dates outside range or in the future
                HeatMapCell(color = Color.Transparent)
            }
        }
    }
}

@Composable
internal fun HeatMapCell(
    color: Color,
    size: Dp = HeatMapCellSize,
    highlighted: Boolean = false,
    highlightColor: Color = Color.Unspecified,
) {
    val shape = RoundedCornerShape(HeatMapCellCornerRadius)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(color)
            .thenIf(highlighted) {
                this.border(width = 1.5.dp, color = highlightColor, shape = shape)
            }
    )
}

/**
 * Determines the cell color based on the count and maxCount.
 * Uses 5 levels (0-4) similar to GitHub's contribution graph.
 */
internal fun heatMapCellColor(
    count: Int,
    maxCount: Int,
    emptyColor: Color,
    primaryColor: Color,
): Color {
    if (count == 0) return emptyColor
    if (maxCount == 0) return emptyColor

    val ratio = count.toFloat() / maxCount.toFloat()
    val level = when {
        ratio <= 0.25f -> 1
        ratio <= 0.50f -> 2
        ratio <= 0.75f -> 3
        else -> 4
    }

    return when (level) {
        1 -> primaryColor.copy(alpha = 0.25f)
        2 -> primaryColor.copy(alpha = 0.50f)
        3 -> primaryColor.copy(alpha = 0.75f)
        4 -> primaryColor
        else -> emptyColor
    }
}

/**
 * Builds a list of [WeekData]s covering [totalWeeks] weeks ending on [today].
 * Each week starts on Monday.
 * The rows represent: Mon, Tue, Wed, Thu, Fri, Sat, Sun.
 */
internal fun buildWeeks(today: LocalDate, totalWeeks: Int): List<WeekData> {
    // Find the Monday of the current week
    val daysSinceMonday = today.dayOfWeek.ordinal
    val currentWeekMonday = today.minus(daysSinceMonday, DateTimeUnit.DAY)

    // Go back totalWeeks-1 weeks from the current week's Monday
    val startMonday = currentWeekMonday.minus((totalWeeks - 1).toLong(), DateTimeUnit.WEEK)

    return (0 until totalWeeks).map { weekIdx ->
        val weekStart = startMonday.plus(weekIdx.toLong(), DateTimeUnit.WEEK)
        val days = (0 until 7).map { dayOffset ->
            weekStart.plus(dayOffset, DateTimeUnit.DAY)
        }
        WeekData(days = days, weekStartDate = weekStart)
    }
}

/**
 * Builds month labels: returns a list of (weekIndex, monthAbbreviation) for weeks
 * where a new month starts (i.e., the week contains the 1st of a month).
 */
internal fun buildMonthLabels(weeks: List<WeekData>): List<Pair<Int, String>> {
    val labels = mutableListOf<Pair<Int, String>>()
    var lastMonth = -1

    weeks.forEachIndexed { index, week ->
        // Check if any day in this week is the first occurrence of a new month
        for (date in week.days) {
            if (date != null && date.month.number != lastMonth) {
                // Only add label if the month actually starts in/near this week
                if (date.day <= 7 || lastMonth == -1) {
                    labels.add(index to date.month.abbreviated)
                    lastMonth = date.month.number
                    break
                } else {
                    lastMonth = date.month.number
                }
            }
        }
    }

    return labels
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeatMapGridPreview() {
    LimitlessTheme {
        Surface {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val sampleCounts = buildMap {
                for (i in 0 until 180) {
                    val date = today.minus(i, DateTimeUnit.DAY)
                    val count = when {
                        i % 7 == 0 -> 4
                        i % 3 == 0 -> 2
                        i % 5 == 0 -> 1
                        i % 2 == 0 -> 3
                        else -> 0
                    }
                    if (count > 0) put(date, count)
                }
            }

            HeatMapGrid(
                modifier = Modifier.padding(16.dp),
                dayCounts = sampleCounts,
                maxCount = 4,
                color = HabitColor.TEAL.color,
                highlightCurrentDay = true,
            )
        }
    }
}

@Preview(name = "Primary Color — Light", showBackground = true)
@Preview(name = "Primary Color — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeatMapGridPrimaryColorPreview() {
    LimitlessTheme {
        Surface {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val sampleCounts = buildMap {
                for (i in 0 until 180) {
                    val date = today.minus(i, DateTimeUnit.DAY)
                    val count = if (i % 4 == 0) 3 else if (i % 2 == 0) 1 else 0
                    if (count > 0) put(date, count)
                }
            }

            HeatMapGrid(
                modifier = Modifier.padding(16.dp),
                dayCounts = sampleCounts,
                maxCount = 3,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                highlightCurrentDay = true,
            )
        }
    }
}

@Preview(name = "Empty — Light", showBackground = true)
@Preview(name = "Empty — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyHeatMapGridPreview() {
    LimitlessTheme {
        Surface {
            HeatMapGrid(
                modifier = Modifier.padding(16.dp),
                dayCounts = emptyMap(),
                maxCount = 0,
                color = HabitColor.ORANGE.color,
            )
        }
    }
}

// endregion
