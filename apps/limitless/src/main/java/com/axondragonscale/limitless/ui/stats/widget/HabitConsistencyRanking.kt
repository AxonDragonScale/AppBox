package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color

/**
 * Created by Ronak Harkhani on 23/07/25
 *
 * A card showing all habits ranked by consistency rate, highest first.
 *
 * Each row contains:
 *   - A filled circle in the habit's own color.
 *   - The habit name and a horizontal progress bar (also habit-colored) reflecting the
 *     consistency rate.
 *   - A percentage label whose color signals performance tier:
 *       - ≥ 80 % → green accent  (top performer)
 *       - < 30 % → amber accent  (needs attention — framed as a gentle nudge, not failure)
 *       - otherwise → neutral [MaterialTheme.colorScheme.onSurface]
 *
 * Consistency rate = active days / days since the habit's first record.
 * Habits are pre-sorted by [HabitConsistencyItem.consistencyRate] descending by the ViewModel.
 */
@Composable
fun HabitConsistencyRanking(
    modifier: Modifier = Modifier,
    data: HabitConsistencyData,
) {
    StatCard(
        modifier = modifier,
        title = "Habit Consistency",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            data.habits.forEach { item ->
                ConsistencyRow(item = item)
            }
        }
    }
}

// region Row

@Composable
private fun ConsistencyRow(
    modifier: Modifier = Modifier,
    item: HabitConsistencyItem,
) {
    val habitColor = item.color.color
    val percentLabel = "${(item.consistencyRate * 100).toInt()}%"
    val tierColor = tierColor(item.consistencyRate)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(habitColor),
        )

        Spacer(Modifier.width(10.dp))

        // Name + progress bar
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.padding(start = 2.dp),
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(6.dp),
                progress = { item.consistencyRate },
                color = habitColor,
                trackColor = habitColor.copy(alpha = 0.15f),
                gapSize = 0.dp,
            )
        }

        Spacer(Modifier.width(10.dp))

        // Percentage label
        Text(
            text = percentLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tierColor,
        )
    }
}

// endregion

// region Tier color

/**
 * Returns a sentiment color for the given [rate]:
 * - ≥ 0.80 → green (positive accent)
 * - < 0.30 → amber (gentle warning)
 * - otherwise → neutral [MaterialTheme.colorScheme.onSurface]
 */
@Composable
private fun tierColor(rate: Float): Color = when {
    rate >= 0.80f -> Color(0xFF4CAF50)   // Material Green 500
    rate < 0.30f  -> Color(0xFFFFA726)   // Material Amber 400
    else          -> MaterialTheme.colorScheme.onSurface
}

// endregion

// region Previews

@Preview(name = "Light — Full List", showBackground = true)
@Preview(name = "Dark — Full List", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FullListPreview() {
    LimitlessTheme {
        HabitConsistencyRanking(
            modifier = Modifier.padding(16.dp),
            data = HabitConsistencyData(
                habits = listOf(
                    HabitConsistencyItem("Meditation",  HabitColor.TEAL,   0.92f, 92,  100),
                    HabitConsistencyItem("Exercise",    HabitColor.ORANGE, 0.85f, 85,  100),
                    HabitConsistencyItem("Reading",     HabitColor.PURPLE, 0.61f, 61,  100),
                    HabitConsistencyItem("Journaling",  HabitColor.BLUE,   0.44f, 44,  100),
                    HabitConsistencyItem("Cold Shower", HabitColor.SKY,    0.27f, 27,  100),
                    HabitConsistencyItem("Stretching",  HabitColor.GREEN,  0.18f, 18,  100),
                ),
            ),
        )
    }
}

@Preview(name = "Light — Single Habit", showBackground = true)
@Preview(name = "Dark — Single Habit", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SingleHabitPreview() {
    LimitlessTheme {
        HabitConsistencyRanking(
            modifier = Modifier.padding(16.dp),
            data = HabitConsistencyData(
                habits = listOf(
                    HabitConsistencyItem("Morning Run", HabitColor.GREEN, 0.75f, 75, 100),
                ),
            ),
        )
    }
}

@Preview(name = "Light — All Top Performers", showBackground = true)
@Preview(name = "Dark — All Top Performers", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AllTopPreview() {
    LimitlessTheme {
        HabitConsistencyRanking(
            modifier = Modifier.padding(16.dp),
            data = HabitConsistencyData(
                habits = listOf(
                    HabitConsistencyItem("Meditation", HabitColor.TEAL,   1.00f, 10, 10),
                    HabitConsistencyItem("Exercise",   HabitColor.ORANGE, 0.90f, 9,  10),
                    HabitConsistencyItem("Reading",    HabitColor.PURPLE, 0.80f, 8,  10),
                ),
            ),
        )
    }
}

@Preview(name = "Light — Mixed Zero", showBackground = true)
@Preview(name = "Dark — Mixed Zero", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ZeroConsistencyPreview() {
    LimitlessTheme {
        HabitConsistencyRanking(
            modifier = Modifier.padding(16.dp),
            data = HabitConsistencyData(
                habits = listOf(
                    HabitConsistencyItem("Exercise",  HabitColor.ORANGE, 0.55f, 55, 100),
                    HabitConsistencyItem("New Habit", HabitColor.ROSE,   0.00f, 0,  1),
                ),
            ),
        )
    }
}

// endregion
