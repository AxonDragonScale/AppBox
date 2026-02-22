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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.commons.compose.thenIf
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.HabitHeatMapData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import com.axondragonscale.commons.abbreviated
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 09/11/24
 */

private val CellSize = 14.dp
private val CellSpacing = 3.dp
private val CellCornerRadius = 3.dp
private val DayLabelWidth = 28.dp

private val DayLabels = listOf("M", "", "W", "", "F", "", "S")

/**
 * A week column in the heatmap grid.
 */
private data class WeekData(
    val days: List<LocalDate?>, // 7 entries, null for days outside the range
    val weekStartDate: LocalDate,
)

@Composable
fun HabitHeatMap(
    modifier: Modifier = Modifier,
    data: HabitHeatMapData,
    highlightCurrentDay: Boolean = true,
    totalWeeks: Int = 26,
    onRecordClick: () -> Unit = {},
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
        // Habit name + record button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            IconButton(
                onClick = onRecordClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Record habit",
                )
            }
        }

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
                horizontalArrangement = Arrangement.spacedBy(CellSpacing),
            ) {
                items(weeks, key = { it.weekStartDate }) { week ->
                    WeekColumn(
                        weekData = week,
                        dayCounts = data.dayCounts,
                        maxCount = data.maxCount,
                        today = today,
                        highlightCurrentDay = highlightCurrentDay,
                        habitColor = data.habit.color.color,
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
    // We need to figure out which weeks are visible and show month labels accordingly.
    // For simplicity, render a static row of month labels aligned with the LazyRow.
    // We'll use the same LazyRow state to keep them in sync by placing them in their own LazyRow.
    val monthLabelListState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        monthLabelListState.scrollToItem(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset,
        )
    }

    Row {
        // Spacer for day label column width
        Spacer(modifier = Modifier.width(DayLabelWidth))

        LazyRow(
            state = monthLabelListState,
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(CellSpacing),
        ) {
            items(weeks.size, key = { it }) { weekIndex ->
                val label = monthLabels.find { it.first == weekIndex }
                Box(
                    modifier = Modifier
                        .width(CellSize)
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
        modifier = Modifier.width(DayLabelWidth),
        verticalArrangement = Arrangement.spacedBy(CellSpacing),
    ) {
        DayLabels.forEach { label ->
            Box(
                modifier = Modifier.size(CellSize),
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
        verticalArrangement = Arrangement.spacedBy(CellSpacing),
    ) {
        weekData.days.forEach { date ->
            if (date != null && date <= today) {
                val count = dayCounts[date] ?: 0
                val color = cellColor(count, maxCount, emptyColor, habitColor)
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
private fun HeatMapCell(
    color: Color,
    size: Dp = CellSize,
    highlighted: Boolean = false,
    highlightColor: Color = Color.Unspecified,
) {
    val shape = RoundedCornerShape(CellCornerRadius)
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
private fun cellColor(
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
 * Builds a list of [WeekData]s covering `totalWeeks` weeks ending on [today].
 * Each week starts on Monday.
 * The rows represent: Mon, Tue, Wed, Thu, Fri, Sat, Sun.
 */
private fun buildWeeks(today: LocalDate, totalWeeks: Int): List<WeekData> {
    // Find the Monday of the current week
    val daysSinceMonday = today.dayOfWeek.ordinal
    val currentWeekMonday = today.minus(daysSinceMonday, DateTimeUnit.DAY)

    // Go back totalWeeks-1 weeks from the current week's Monday
    val startMonday = currentWeekMonday.minus((totalWeeks - 1).toLong(), DateTimeUnit.WEEK)

    return (0 until totalWeeks).map { weekIdx ->
        val weekStart = startMonday.plus(weekIdx.toLong(), DateTimeUnit.WEEK)
        val days = (0 until 7).map { dayOffset ->
            val date = weekStart.plus(dayOffset, DateTimeUnit.DAY)
            date
        }
        WeekData(days = days, weekStartDate = weekStart)
    }
}

/**
 * Builds month labels: returns a list of (weekIndex, monthAbbreviation) for weeks
 * where a new month starts (i.e., the week contains the 1st of a month).
 */
private fun buildMonthLabels(weeks: List<WeekData>): List<Pair<Int, String>> {
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
private fun HabitHeatMapPreview() {
    LimitlessTheme {
        Surface {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val sampleCounts = buildMap {
                // Scatter some records across the last few months
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

            HabitHeatMap(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL),
                    dayCounts = sampleCounts,
                    maxCount = 4,
                ),
                highlightCurrentDay = true,
            )
        }
    }
}

@Preview(name = "Empty Heatmap Light Mode", showBackground = true)
@Preview(name = "Empty Heatmap Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyHabitHeatMapPreview() {
    LimitlessTheme {
        Surface {
            HabitHeatMap(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 2, name = "Exercise", description = "", color = HabitColor.ORANGE),
                    dayCounts = emptyMap(),
                    maxCount = 0,
                ),
            )
        }
    }
}

// endregion
