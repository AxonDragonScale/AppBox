package com.axondragonscale.limitless.ui.habit.stats.widget

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.compose.chart.RingChart
import com.axondragonscale.commons.compose.chart.RingChartLabelStyle
import com.axondragonscale.commons.compose.chart.RingChartTrackStyle
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.habit.stats.model.GoalCompletionData
import com.axondragonscale.limitless.ui.habit.stats.model.GoalCompletionRate
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlin.math.roundToInt

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A card showing one [RingChart] per configured goal type (Daily / Weekly / Monthly).
 * Each ring displays the fraction of periods where the goal was met, with the percentage
 * printed at the center. The goal type name and per-period target are shown below each
 * ring for context.
 *
 * This widget is only rendered when the habit has at least one goal configured —
 * the nullable [GoalCompletionData] field in [StatsUiState] handles that guard.
 */
@Composable
fun GoalCompletionRings(
    modifier: Modifier = Modifier,
    data: GoalCompletionData,
    habitColor: HabitColor,
) {
    val color = habitColor.color

    StatCard(
        modifier = modifier,
        title = "Goal Completion",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            data.rates.forEach { rate ->
                GoalRingItem(rate = rate, color = color)
            }
        }
    }
}

@Composable
private fun GoalRingItem(
    modifier: Modifier = Modifier,
    rate: GoalCompletionRate,
    color: Color,
) {
    val percentage = (rate.percentage * 100).roundToInt()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RingChart(
            modifier = Modifier.size(88.dp),
            progress = rate.percentage,
            label = "$percentage%",
            trackStyle = RingChartTrackStyle.default(
                color = color,
                strokeWidth = 10.dp,
            ),
            labelStyle = RingChartLabelStyle.default(
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Text(
            text = rate.type.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${rate.target}× / ${rate.type.unit}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// region Previews

@Preview(name = "All Three Goals — Light", showBackground = true)
@Preview(name = "All Three Goals — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AllGoalsPreview() {
    LimitlessTheme {
        GoalCompletionRings(
            modifier = Modifier.padding(16.dp),
            data = GoalCompletionData(
                rates = listOf(
                    GoalCompletionRate(type = GoalType.DAILY, target = 1, percentage = 0.82f),
                    GoalCompletionRate(type = GoalType.WEEKLY, target = 5, percentage = 0.67f),
                    GoalCompletionRate(type = GoalType.MONTHLY, target = 20, percentage = 0.91f),
                ),
            ),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "Single Goal — Light", showBackground = true)
@Preview(name = "Single Goal — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SingleGoalPreview() {
    LimitlessTheme {
        GoalCompletionRings(
            modifier = Modifier.padding(16.dp),
            data = GoalCompletionData(
                rates = listOf(
                    GoalCompletionRate(type = GoalType.DAILY, target = 2, percentage = 0.55f),
                ),
            ),
            habitColor = HabitColor.TEAL,
        )
    }
}

@Preview(name = "Two Goals — Light", showBackground = true)
@Preview(name = "Two Goals — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun TwoGoalsPreview() {
    LimitlessTheme {
        GoalCompletionRings(
            modifier = Modifier.padding(16.dp),
            data = GoalCompletionData(
                rates = listOf(
                    GoalCompletionRate(type = GoalType.DAILY, target = 1, percentage = 1.0f),
                    GoalCompletionRate(type = GoalType.WEEKLY, target = 7, percentage = 0.43f),
                ),
            ),
            habitColor = HabitColor.ORANGE,
        )
    }
}

@Preview(name = "Zero Completion — Light", showBackground = true)
@Preview(name = "Zero Completion — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun ZeroCompletionPreview() {
    LimitlessTheme {
        GoalCompletionRings(
            modifier = Modifier.padding(16.dp),
            data = GoalCompletionData(
                rates = listOf(
                    GoalCompletionRate(type = GoalType.DAILY, target = 3, percentage = 0f),
                    GoalCompletionRate(type = GoalType.WEEKLY, target = 15, percentage = 0f),
                    GoalCompletionRate(type = GoalType.MONTHLY, target = 60, percentage = 0f),
                ),
            ),
            habitColor = HabitColor.RED,
        )
    }
}

// endregion
