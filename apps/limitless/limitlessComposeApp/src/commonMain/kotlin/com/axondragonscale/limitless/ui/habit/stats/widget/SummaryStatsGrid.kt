package com.axondragonscale.limitless.ui.habit.stats.widget

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.formatMonthYear
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.common.SummaryStatCard
import com.axondragonscale.limitless.ui.habit.stats.model.SummaryData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A 2×2 grid of compact stat cards, each showing an icon alongside a key aggregate
 * metric for the habit: total records, active days, average per active day, and tracking
 * start date.
 */
@Composable
fun SummaryStatsGrid(
    modifier: Modifier = Modifier,
    data: SummaryData,
    habitColor: HabitColor,
) {
    val color = habitColor.color

    val avgLabel = run {
        val v = data.avgPerActiveDay
        val whole = v.toLong()
        val decimal = ((v - whole) * 10).toLong()
        "$whole.$decimal"
    }
    val sinceLabel = data.trackingSince?.formatMonthYear() ?: "—"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Numbers,
                value = data.totalRecords.toString(),
                label = "Total Records",
                color = color,
            )
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarMonth,
                value = data.activeDays.toString(),
                label = "Active Days",
                color = color,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                value = avgLabel,
                label = "Avg / Active Day",
                color = color,
            )
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                value = sinceLabel,
                label = "Tracking Since",
                color = color,
            )
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        SummaryStatsGrid(
            modifier = Modifier.padding(16.dp),
            data = SummaryData(
                totalRecords = 247,
                activeDays = 89,
                avgPerActiveDay = 2.8f,
                trackingSince = LocalDate(2024, 3, 1),
            ),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "No Records — Light", showBackground = true)
@Preview(name = "No Records — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun NoRecordsPreview() {
    LimitlessTheme {
        SummaryStatsGrid(
            modifier = Modifier.padding(16.dp),
            data = SummaryData(
                totalRecords = 0,
                activeDays = 0,
                avgPerActiveDay = 0f,
                trackingSince = null,
            ),
            habitColor = HabitColor.BLUE,
        )
    }
}

// endregion
