package com.axondragonscale.limitless.ui.habit.calendar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.dayOfWeekAbbreviations
import com.axondragonscale.commons.daysInMonth
import com.axondragonscale.commons.displayName
import com.axondragonscale.limitless.ui.common.RecordHabitSheet
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 29/06/25
 */

@Composable
fun HabitCalendar(
    modifier: Modifier = Modifier,
    onNavigateToDayDetail: (date: LocalDate) -> Unit,
) {
    val vm: HabitCalendarVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    HabitCalendar(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HabitCalendarUiEvent.NavigateToDayDetail ->
                    onNavigateToDayDetail(event.date)

                else -> vm.onEvent(event)
            }
        },
    )
}

@Composable
private fun HabitCalendar(
    modifier: Modifier = Modifier,
    uiState: HabitCalendarUiState,
    onEvent: (HabitCalendarUiEvent) -> Unit,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val isCurrentMonth = uiState.year == today.year && uiState.month == today.month.number

    var longPressedDate by remember { mutableStateOf<LocalDate?>(null) }

    longPressedDate?.let { date ->
        RecordHabitSheet(
            habit = uiState.habit,
            showTimePicker = true,
            initialDate = date,
            onRecord = { record -> onEvent(HabitCalendarUiEvent.SaveRecord(record)) },
            onDismiss = { longPressedDate = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        MonthNavigationRow(
            year = uiState.year,
            month = uiState.month,
            isNextDisabled = isCurrentMonth,
            onPrevious = { onEvent(HabitCalendarUiEvent.PreviousMonth) },
            onNext = { onEvent(HabitCalendarUiEvent.NextMonth) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        DayOfWeekHeader()

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            CalendarGrid(
                year = uiState.year,
                month = uiState.month,
                dayCounts = uiState.dayCounts,
                today = today,
                onDayTap = { onEvent(HabitCalendarUiEvent.NavigateToDayDetail(it)) },
                onDayLongPress = { longPressedDate = it },
            )
        }
    }
}

// region Day-of-week header

@Composable
private fun DayOfWeekHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        dayOfWeekAbbreviations.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

// endregion

// region Calendar grid

@Composable
private fun CalendarGrid(
    modifier: Modifier = Modifier,
    year: Int,
    month: Int,
    dayCounts: Map<LocalDate, Int>,
    today: LocalDate,
    onDayTap: (LocalDate) -> Unit,
    onDayLongPress: (LocalDate) -> Unit,
) {
    val firstOfMonth = LocalDate(year, month, 1)
    // Mon=1 … Sun=7 → offset = isoDayNumber - 1, so Monday lands in column 0
    val startOffset = firstOfMonth.dayOfWeek.isoDayNumber - 1
    val daysInMonth = daysInMonth(year, month)
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7 // ceiling division

    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1

                    if (dayNumber !in 1..daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = LocalDate(year, month, dayNumber)
                        val count = dayCounts[date] ?: 0
                        val isToday = date == today
                        val isFuture = date > today

                        DayCell(
                            modifier = Modifier.weight(1f),
                            dayNumber = dayNumber,
                            count = count,
                            isToday = isToday,
                            isFuture = isFuture,
                            onTap = { if (!isFuture) onDayTap(date) },
                            onLongPress = { if (!isFuture) onDayLongPress(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    dayNumber: Int,
    count: Int,
    isToday: Boolean,
    isFuture: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val hasActivity = count > 0
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceContainerHighest

    val bgAlpha = when {
        isFuture -> 0f
        hasActivity -> (0.15f + count.coerceAtMost(10) * 0.07f).coerceAtMost(0.85f)
        isToday -> 1f
        else -> 0f
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isFuture || (!hasActivity && !isToday) -> Color.Transparent
                    hasActivity -> primaryColor.copy(alpha = bgAlpha)
                    else -> surfaceVariant
                }
            )
            .combinedClickable(
                enabled = !isFuture,
                onClick = onTap,
                onLongClick = onLongPress,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!isFuture && (isToday || hasActivity)) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    hasActivity -> primaryColor
                    isToday -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
            )

            if (!isFuture && hasActivity)
                Row(verticalAlignment = Alignment.CenterVertically,) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(4.dp)
                            .background(primaryColor, CircleShape),
                    )
                    Text(
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        ),
                        text = count.toString()
                    )
                }
        }
    }
}

// endregion

// region Month navigation

@Composable
private fun MonthNavigationRow(
    modifier: Modifier = Modifier,
    year: Int,
    month: Int,
    isNextDisabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val monthName = remember(month) { Month(month).displayName }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = "$monthName $year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            IconButton(
                onClick = onNext,
                enabled = !isNextDisabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = if (isNextDisabled)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// endregion

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCalendarPreview() {
    LimitlessTheme {
        Surface {
            HabitCalendar(
                uiState = HabitCalendarUiState(
                    year = 2026,
                    month = 2,
                    isLoading = false,
                    dayCounts = mapOf(
                        LocalDate(2026, 2, 3) to 1,
                        LocalDate(2026, 2, 4) to 3,
                        LocalDate(2026, 2, 5) to 2,
                        LocalDate(2026, 2, 10) to 1,
                        LocalDate(2026, 2, 11) to 1,
                        LocalDate(2026, 2, 12) to 5,
                        LocalDate(2026, 2, 17) to 2,
                        LocalDate(2026, 2, 18) to 1,
                        LocalDate(2026, 2, 24) to 1,
                        LocalDate(2026, 2, 25) to 4,
                    ),
                ),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Loading Light", showBackground = true)
@Preview(name = "Loading Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCalendarLoadingPreview() {
    LimitlessTheme {
        Surface {
            HabitCalendar(
                uiState = HabitCalendarUiState(
                    year = 2026,
                    month = 2,
                    isLoading = true,
                ),
                onEvent = {},
            )
        }
    }
}

// endregion
