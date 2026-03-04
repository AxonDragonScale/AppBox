package com.axondragonscale.limitless.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.HabitHeatMapData
import com.axondragonscale.limitless.model.HabitView
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 09/11/24
 *
 * A habit card that shows the habit name, a record button, and a view determined by the habit's
 * [HabitView] preference. All grid rendering is delegated to
 * the appropriate composable ([HeatMapGrid], [HabitMonthView], or [HabitWeekView]); this
 * composable owns only the habit-specific header row.
 */
@Composable
fun HabitCard(
    modifier: Modifier = Modifier,
    data: HabitHeatMapData,
    highlightCurrentDay: Boolean = true,
    onRecordClick: () -> Unit = {},
    onDayClick: (LocalDate) -> Unit = {},
) {
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

        // Delegate grid rendering based on the habit's chosen view
        when (data.habit.view) {
            HabitView.HEAT_MAP -> HeatMapGrid(
                dayCounts = data.dayCounts,
                maxCount = data.maxCount,
                color = data.habit.color.color,
                highlightCurrentDay = highlightCurrentDay,
            )
            HabitView.MONTH -> SwipeableHabitMonthView(
                dayCounts = data.dayCounts,
                maxCount = data.maxCount,
                color = data.habit.color.color,
                highlightCurrentDay = highlightCurrentDay,
                onDayClick = onDayClick,
            )
            HabitView.WEEK -> SwipeableHabitWeekView(
                dayCounts = data.dayCounts,
                maxCount = data.maxCount,
                color = data.habit.color.color,
                highlightCurrentDay = highlightCurrentDay,
                onDayClick = onDayClick,
            )
        }
    }
}

// region Previews

private fun previewSampleCounts(): Map<LocalDate, Int> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return buildMap {
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
}

@Preview(name = "HeatMap — Light", showBackground = true)
@Preview(name = "HeatMap — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCardHeatMapPreview() {
    LimitlessTheme {
        Surface {
            HabitCard(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL, view = HabitView.HEAT_MAP),
                    dayCounts = previewSampleCounts(),
                    maxCount = 4,
                ),
                highlightCurrentDay = true,
            )
        }
    }
}

@Preview(name = "Month — Light", showBackground = true)
@Preview(name = "Month — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCardMonthPreview() {
    LimitlessTheme {
        Surface {
            HabitCard(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 2, name = "Reading", description = "", color = HabitColor.BLUE, view = HabitView.MONTH),
                    dayCounts = previewSampleCounts(),
                    maxCount = 4,
                ),
            )
        }
    }
}

@Preview(name = "Week — Light", showBackground = true)
@Preview(name = "Week — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCardWeekPreview() {
    LimitlessTheme {
        Surface {
            HabitCard(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 3, name = "Exercise", description = "", color = HabitColor.ORANGE, view = HabitView.WEEK),
                    dayCounts = previewSampleCounts(),
                    maxCount = 4,
                ),
            )
        }
    }
}

@Preview(name = "Empty — Light", showBackground = true)
@Preview(name = "Empty — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyHabitCardPreview() {
    LimitlessTheme {
        Surface {
            HabitCard(
                modifier = Modifier.padding(16.dp),
                data = HabitHeatMapData(
                    habit = HabitEntity(id = 4, name = "Journaling", description = "", color = HabitColor.ORANGE),
                    dayCounts = emptyMap(),
                    maxCount = 0,
                ),
            )
        }
    }
}

// endregion
