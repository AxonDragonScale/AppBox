package com.axondragonscale.limitless.ui.stats.widget

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.RingChart
import com.axondragonscale.commons.compose.chart.RingChartLabelStyle
import com.axondragonscale.commons.compose.chart.RingChartTrackStyle
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.TodayHabitStatus
import com.axondragonscale.limitless.ui.stats.model.TodayProgressData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color

/**
 * Created by Ronak Harkhani on 23/07/25
 *
 * A card showing today's habit completion at a glance.
 *
 * Layout: a [RingChart] on the left (completed / total) beside a compact habit list on the
 * right. Pending habits appear at the top in full opacity; completed habits sink to the bottom
 * at reduced opacity so the eye is drawn to what still needs to be done. When every habit is
 * complete a single celebratory line appears at the bottom of the list.
 *
 * This widget is read-only. The Home screen handles recording; this is a dashboard view only.
 */
@Composable
fun TodayProgress(
    modifier: Modifier = Modifier,
    data: TodayProgressData,
) {
    StatCard(
        modifier = modifier,
        title = "Today's Progress",
    ) {
        ProgressContent(data = data)
    }
}

// region Content

@Composable
private fun ProgressContent(data: TodayProgressData) {
    val allDone = data.completed == data.total && data.total > 0
    val primary = MaterialTheme.colorScheme.primary
    val progress = if (data.total > 0) data.completed.toFloat() / data.total else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ring chart — sized to exactly half the card width via weight + aspectRatio
        RingChart(
            modifier = Modifier.weight(1f).aspectRatio(1f),
            progress = progress,
            label = "${data.completed} / ${data.total}",
            trackStyle = RingChartTrackStyle.default(
                color = primary,
                strokeWidth = 10.dp,
            ),
            labelStyle = RingChartLabelStyle.default(
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Spacer(Modifier.width(16.dp))

        // Habit list: pending first, then completed (dimmed)
        val (pending, completed) = data.habits.partition { !it.isCompleted }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            pending.forEach { status ->
                HabitStatusRow(status = status, dimmed = false)
            }
            if (pending.isNotEmpty() && completed.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
            }
            completed.forEach { status ->
                HabitStatusRow(status = status, dimmed = true)
            }
            if (allDone) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "All done for today! \uD83C\uDF89",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// endregion

// region Habit row

@Composable
private fun HabitStatusRow(
    modifier: Modifier = Modifier,
    status: TodayHabitStatus,
    dimmed: Boolean,
) {
    val habitColor = status.color.color
    val contentAlpha = if (dimmed) 0.8f else 1f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = if (status.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = habitColor.copy(alpha = contentAlpha),
        )

        Text(
            text = status.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (dimmed) FontWeight.Normal else FontWeight.Medium,
            color = habitColor.copy(alpha = contentAlpha),
            maxLines = 1,
        )
    }
}

// endregion

// region Previews

@Preview(name = "Light — Partial", showBackground = true)
@Preview(name = "Dark — Partial", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun PartialPreview() {
    LimitlessTheme {
        TodayProgress(
            modifier = Modifier.padding(16.dp),
            data = TodayProgressData(
                completed = 2,
                total = 5,
                habits = listOf(
                    TodayHabitStatus("Meditation", HabitColor.TEAL, isCompleted = false),
                    TodayHabitStatus("Exercise", HabitColor.ORANGE, isCompleted = false),
                    TodayHabitStatus("Reading", HabitColor.PURPLE, isCompleted = false),
                    TodayHabitStatus("Journaling", HabitColor.BLUE, isCompleted = true),
                    TodayHabitStatus("Cold Shower", HabitColor.SKY, isCompleted = true),
                ),
            ),
        )
    }
}

@Preview(name = "Light — All Done", showBackground = true)
@Preview(name = "Dark — All Done", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AllDonePreview() {
    LimitlessTheme {
        TodayProgress(
            modifier = Modifier.padding(16.dp),
            data = TodayProgressData(
                completed = 4,
                total = 4,
                habits = listOf(
                    TodayHabitStatus("Meditation", HabitColor.TEAL, isCompleted = true),
                    TodayHabitStatus("Exercise", HabitColor.ORANGE, isCompleted = true),
                    TodayHabitStatus("Reading", HabitColor.PURPLE, isCompleted = true),
                    TodayHabitStatus("Journaling", HabitColor.BLUE, isCompleted = true),
                ),
            ),
        )
    }
}

@Preview(name = "Light — None Done", showBackground = true)
@Preview(name = "Dark — None Done", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun NoneDonePreview() {
    LimitlessTheme {
        TodayProgress(
            modifier = Modifier.padding(16.dp),
            data = TodayProgressData(
                completed = 0,
                total = 3,
                habits = listOf(
                    TodayHabitStatus("Meditation", HabitColor.TEAL, isCompleted = false),
                    TodayHabitStatus("Exercise", HabitColor.ORANGE, isCompleted = false),
                    TodayHabitStatus("Reading", HabitColor.PURPLE, isCompleted = false),
                ),
            ),
        )
    }
}

@Preview(name = "Light — Single Habit", showBackground = true)
@Preview(name = "Dark — Single Habit", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SingleHabitPreview() {
    LimitlessTheme {
        TodayProgress(
            modifier = Modifier.padding(16.dp),
            data = TodayProgressData(
                completed = 0,
                total = 1,
                habits = listOf(
                    TodayHabitStatus("Morning Run", HabitColor.GREEN, isCompleted = false),
                ),
            ),
        )
    }
}

// endregion
