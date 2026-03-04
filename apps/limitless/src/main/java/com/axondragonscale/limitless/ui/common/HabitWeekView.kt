// Created by Ronak Harkhani on 04/03/26

package com.axondragonscale.limitless.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.commons.abbreviated
import com.axondragonscale.commons.compose.thenIf
import com.axondragonscale.commons.letter
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private val WeekCellSize = 28.dp
private const val SWIPEABLE_WEEK_PAGE_COUNT = 500
private const val SWIPEABLE_WEEK_INITIAL_PAGE = SWIPEABLE_WEEK_PAGE_COUNT - 1

/**
 * A swipeable version of [HabitWeekView] that supports paging back through previous weeks.
 *
 * Wraps [HabitWeekView] in a [HorizontalPager]. The initial (rightmost) page shows the current
 * week. Swiping right reveals older weeks; swiping left past the current week is blocked because
 * the pager starts at its last page.
 *
 * @param dayCounts          Map of date → record count used for cell color intensity.
 * @param maxCount           Maximum count across all dates, used to derive intensity levels.
 * @param color              Habit color used for filled cells.
 * @param highlightCurrentDay Whether to outline today's cell with a border.
 * @param onDayClick         Called when the user taps a cell.
 */
@Composable
fun SwipeableHabitWeekView(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    color: Color,
    highlightCurrentDay: Boolean = true,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val pagerState = rememberPagerState(
        initialPage = SWIPEABLE_WEEK_INITIAL_PAGE,
        pageCount = { SWIPEABLE_WEEK_PAGE_COUNT },
    )

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 1,
    ) { page ->
        // page == INITIAL means current week (offset 0), each step left = one week further back
        val weeksBack = SWIPEABLE_WEEK_INITIAL_PAGE - page
        val referenceDate = remember(today, weeksBack) {
            today.minus(weeksBack * 7, DateTimeUnit.DAY)
        }

        HabitWeekView(
            dayCounts = dayCounts,
            maxCount = maxCount,
            color = color,
            referenceDate = referenceDate,
            highlightCurrentDay = highlightCurrentDay,
            onDayClick = onDayClick,
        )
    }
}


/**
 * A rolling 7-day strip showing the last 7 days ending on today.
 *
 * Renders 7 cells in a horizontal row (oldest → newest = left → right). Each cell uses the
 * same color intensity logic as [HeatMapGrid]. A day-of-week letter appears below each cell.
 * Tapping a cell calls [onDayClick] with the corresponding [LocalDate].
 *
 * @param dayCounts          Map of date → record count used for cell color intensity.
 * @param maxCount           Maximum count across all dates, used to derive intensity levels.
 * @param color              Habit color used for filled cells.
 * @param referenceDate      The "end" date of the 7-day window. Defaults to today.
 * @param highlightCurrentDay Whether to outline today's cell with a border.
 * @param onDayClick         Called when the user taps any of the 7 cells.
 */
@Composable
fun HabitWeekView(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    color: Color,
    referenceDate: LocalDate? = null,
    highlightCurrentDay: Boolean = true,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val anchorDate = referenceDate ?: today
    // Build last 7 days ending on anchorDate (oldest first)
    val days = remember(anchorDate) {
        (6 downTo 0).map { offset -> anchorDate.minus(offset, DateTimeUnit.DAY) }
    }

    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val highlightColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left label: "Week/nMar 1 – Mar 7", vertically centered to the cell strip
        Column(
            modifier = Modifier.padding(end = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Week",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            Text(
                text = "${days.first().month.abbreviated} ${days.first().day} – ${days.last().month.abbreviated} ${days.last().day}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        // Cell strip
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            days.forEach { date ->
                val count = dayCounts[date] ?: 0
                val isToday = highlightCurrentDay && date == today
                val cellColor = heatMapCellColor(count, maxCount, emptyColor, color)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Day-of-week letter (above the cell)
                    Text(
                        text = date.dayOfWeek.letter,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )

                    // Colored cell
                    Box(
                        modifier = Modifier
                            .size(WeekCellSize)
                            .background(cellColor, HeatMapCellShape)
                            .thenIf(isToday) {
                                border(1.5.dp, highlightColor, HeatMapCellShape)
                            }
                            .clickable { onDayClick(date) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = date.day.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitWeekViewPreview() {
    LimitlessTheme {
        Surface {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val sampleCounts = buildMap {
                for (i in 0..6) {
                    val date = today.minus(i, DateTimeUnit.DAY)
                    val count = when (i) { 0 -> 3; 2 -> 1; 4 -> 2; 5 -> 2; else -> 0 }
                    if (count > 0) put(date, count)
                }
            }
            HabitWeekView(
                modifier = Modifier.padding(16.dp),
                dayCounts = sampleCounts,
                maxCount = 3,
                color = HabitColor.BLUE.color,
            )
        }
    }
}

@Preview(name = "Empty Light", showBackground = true)
@Preview(name = "Empty Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitWeekViewEmptyPreview() {
    LimitlessTheme {
        Surface {
            HabitWeekView(
                modifier = Modifier.padding(16.dp),
                dayCounts = emptyMap(),
                maxCount = 0,
                color = HabitColor.GREEN.color,
            )
        }
    }
}

// endregion
