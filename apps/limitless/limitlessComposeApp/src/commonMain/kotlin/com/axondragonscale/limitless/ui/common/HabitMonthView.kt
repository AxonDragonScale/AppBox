// Created by Ronak Harkhani on 04/03/26

package com.axondragonscale.limitless.ui.common

import androidx.compose.ui.tooling.preview.AndroidUiModes
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.commons.abbreviated
import com.axondragonscale.commons.compose.thenIf
import com.axondragonscale.commons.dayOfWeekLetters
import com.axondragonscale.commons.daysInMonth
import com.axondragonscale.commons.startOfMonth
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlin.random.Random

private val MonthCellSize = 28.dp
private const val SWIPEABLE_MONTH_PAGE_COUNT = 500
private const val SWIPEABLE_MONTH_INITIAL_PAGE = SWIPEABLE_MONTH_PAGE_COUNT - 1

/**
 * A swipeable version of [HabitMonthView] that supports paging back through previous months.
 *
 * Wraps [HabitMonthView] in a [HorizontalPager]. The initial (rightmost) page shows the current
 * month. Swiping right reveals older months; swiping left past the current month is blocked
 * because the pager starts at its last page.
 *
 * @param dayCounts           Map of date → record count used for cell tinting.
 * @param maxCount            Maximum count across all dates, used to derive intensity levels.
 * @param color               Habit color used for active-day cell backgrounds.
 * @param highlightCurrentDay Whether to outline today's cell with a border.
 * @param onDayClick          Called when the user taps a past-or-today day.
 */
@Composable
fun SwipeableHabitMonthView(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    color: Color,
    highlightCurrentDay: Boolean = true,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val today = remember { LocalDate.today() }
    val pagerState = rememberPagerState(
        initialPage = SWIPEABLE_MONTH_INITIAL_PAGE,
        pageCount = { SWIPEABLE_MONTH_PAGE_COUNT },
    )

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 1,
    ) { page ->
        // page == INITIAL means current month (offset 0), each step left = one month further back
        val monthsBack = SWIPEABLE_MONTH_INITIAL_PAGE - page
        val referenceDate = remember(today, monthsBack) {
            today.minus(monthsBack, DateTimeUnit.MONTH)
        }

        HabitMonthView(
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
 * A compact read-only calendar showing the current calendar month.
 *
 * Displays a Mon–Sun day-of-week header, followed by a grid of day cells. Past days with
 * activity are tinted using the habit color; today is highlighted; future days are dimmed.
 * Tapping any past (non-future) day calls [onDayClick].
 *
 * @param dayCounts           Map of date → record count used for cell tinting.
 * @param maxCount            Maximum count across all dates, used to derive intensity levels.
 * @param color               Habit color used for active-day cell backgrounds.
 * @param referenceDate       The month to display, anchored to this date. Defaults to today.
 * @param highlightCurrentDay Whether to outline today's cell with a border.
 * @param onDayClick          Called when the user taps a past-or-today day.
 */
@Composable
fun HabitMonthView(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    color: Color,
    referenceDate: LocalDate? = null,
    highlightCurrentDay: Boolean = true,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val today = remember { LocalDate.today() }
    val anchorDate = referenceDate ?: today

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left label: Month Label, abbreviated month name + year stacked, aligned to top of grid
        Column(
            modifier = Modifier.padding(end = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Month",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            Text(
                text = anchorDate.month.abbreviated + " " + anchorDate.year.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        MonthGrid(
            dayCounts = dayCounts,
            maxCount = maxCount,
            today = today,
            anchorDate = anchorDate,
            highlightCurrentDay = highlightCurrentDay,
            habitColor = color,
            onDayClick = onDayClick,
        )
    }
}

@Composable
private fun MonthGrid(
    modifier: Modifier = Modifier,
    dayCounts: Map<LocalDate, Int>,
    maxCount: Int,
    today: LocalDate,
    anchorDate: LocalDate,
    highlightCurrentDay: Boolean,
    habitColor: Color,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstOfMonth = anchorDate.startOfMonth
    val startOffset = firstOfMonth.dayOfWeek.ordinal
    val daysInMonth = daysInMonth(anchorDate.year, anchorDate.month.number)
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dayOfWeekLetters.forEach { day ->
                Text(
                    modifier = Modifier.width(MonthCellSize),
                    text = day,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1

                    if (dayNumber !in 1..daysInMonth) {
                        Box(modifier = Modifier.size(MonthCellSize))
                    } else {
                        val date = LocalDate(anchorDate.year, anchorDate.month.number, dayNumber)
                        val count = dayCounts[date] ?: 0
                        val isFuture = date > today
                        val isToday = highlightCurrentDay && date == today

                        MonthDayCell(
                            dayNumber = dayNumber,
                            count = count,
                            maxCount = maxCount,
                            isToday = isToday,
                            isFuture = isFuture,
                            habitColor = habitColor,
                            onClick = { if (!isFuture) onDayClick(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    modifier: Modifier = Modifier,
    dayNumber: Int,
    count: Int,
    maxCount: Int,
    isToday: Boolean,
    isFuture: Boolean,
    habitColor: Color,
    onClick: () -> Unit,
) {
    val hasActivity = count > 0
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val cellColor = when {
        isFuture -> Color.Transparent
        else     -> heatMapCellColor(count, maxCount, emptyColor, habitColor)
    }
    // Text should be legible on the cell background:
    // - on a colored (active) cell: use onSurface so it contrasts the habit-color fill
    // - on today's empty cell: use onSurface normally
    // - future / inactive: dim onSurface
    val textColor = when {
        isFuture    -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        else        -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .size(MonthCellSize)
            .clip(HeatMapCellShape)
            .thenIf(isToday) {
                border(1.5.dp, textColor, HeatMapCellShape)
            }
            .background(cellColor)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            fontWeight = if (isToday || hasActivity) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun HabitMonthViewPreview() {
    LimitlessTheme {
        Surface {
            val today = LocalDate.today()
            val sampleCounts = buildMap {
                for (i in 0..today.day) {
                    val date = LocalDate(today.year, today.month, (i + 1).coerceAtMost(today.day))
                    put(date, Random.nextInt(5))
                }
            }
            HabitMonthView(
                modifier = Modifier.padding(16.dp),
                dayCounts = sampleCounts,
                maxCount = sampleCounts.values.maxOrNull() ?: 0,
                color = HabitColor.TEAL.color,
            )
        }
    }
}

@Preview(name = "Empty Light", showBackground = true)
@Preview(name = "Empty Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun HabitMonthViewEmptyPreview() {
    LimitlessTheme {
        Surface {
            HabitMonthView(
                modifier = Modifier.padding(16.dp),
                dayCounts = emptyMap(),
                maxCount = 0,
                color = HabitColor.ORANGE.color,
            )
        }
    }
}

// endregion
