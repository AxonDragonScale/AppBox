package com.axondragonscale.limitless.ui.habit.stats.widget

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.habit.stats.model.StreakData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A single [StatCard] showing [StreakData.currentStreak] and [StreakData.bestStreak]
 * as two side-by-side cells separated by a [VerticalDivider].
 *
 * Both cells use the habit's [HabitColor] as an accent on the streak number.
 * The card shell matches every other widget on the Stats screen for visual consistency.
 */
@Composable
fun StreakCards(
    modifier: Modifier = Modifier,
    data: StreakData,
    habitColor: HabitColor,
) {
    val color = habitColor.color

    StatCard(
        modifier = modifier,
        title = "Streaks",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreakCell(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                count = data.currentStreak,
                label = "Current Streak",
                color = color,
            )
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
            StreakCell(
                modifier = Modifier.weight(1f),
                emoji = "🏆",
                count = data.bestStreak,
                label = "Best Streak",
                color = color,
            )
        }
    }
}

@Composable
private fun StreakCell(
    modifier: Modifier = Modifier,
    emoji: String,
    count: Int,
    label: String,
    color: Color,
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = if (count == 1) "day" else "days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
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
        StreakCards(
            modifier = Modifier.padding(16.dp),
            data = StreakData(currentStreak = 14, bestStreak = 42),
            habitColor = HabitColor.PURPLE,
        )
    }
}

@Preview(name = "Zero Streaks — Light", showBackground = true)
@Preview(name = "Zero Streaks — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun ZeroStreakPreview() {
    LimitlessTheme {
        StreakCards(
            modifier = Modifier.padding(16.dp),
            data = StreakData(currentStreak = 0, bestStreak = 0),
            habitColor = HabitColor.TEAL,
        )
    }
}

@Preview(name = "Single Day — Light", showBackground = true)
@Preview(name = "Single Day — Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SingleDayPreview() {
    LimitlessTheme {
        StreakCards(
            modifier = Modifier.padding(16.dp),
            data = StreakData(currentStreak = 1, bestStreak = 1),
            habitColor = HabitColor.ORANGE,
        )
    }
}

// endregion
