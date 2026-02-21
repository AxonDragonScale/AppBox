package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.StreakData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A row of two stat cards showing [StreakData.currentStreak] and [StreakData.bestStreak].
 * Both cards use the habit's [HabitColor] as an accent on the streak number.
 */
@Composable
fun StreakCards(
    modifier: Modifier = Modifier,
    data: StreakData,
    habitColor: HabitColor,
) {
    val color = habitColor.color
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StreakCard(
            modifier = Modifier.weight(1f),
            emoji = "🔥",
            count = data.currentStreak,
            label = "Current Streak",
            color = color,
        )
        StreakCard(
            modifier = Modifier.weight(1f),
            emoji = "🏆",
            count = data.bestStreak,
            label = "Best Streak",
            color = color,
        )
    }
}

@Composable
private fun StreakCard(
    modifier: Modifier = Modifier,
    emoji: String,
    count: Int,
    label: String,
    color: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = if (count == 1) "day" else "days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
@Preview(name = "Zero Streaks — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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

// endregion
