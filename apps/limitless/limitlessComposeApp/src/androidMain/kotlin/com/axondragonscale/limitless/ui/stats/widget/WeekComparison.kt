package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.axondragonscale.commons.compose.chart.BarChart
import com.axondragonscale.commons.compose.chart.BarChartStyle
import com.axondragonscale.commons.compose.chart.BarEntry
import com.axondragonscale.commons.dayOfWeekLetters
import com.axondragonscale.limitless.ui.stats.model.WeekComparisonData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

/**
 * Created by Ronak Harkhani on 23/07/25
 *
 * A card comparing this week's habit records against last week's.
 *
 * When the user has at least 7 days of history ([WeekComparisonData.hasLastWeekData] = true)
 * the layout is a three-part row:
 *   - Left column  — "This Week" label, total count, and a 7-day mini bar chart.
 *   - Centre       — [DeltaBadge] showing the signed week-over-week difference.
 *   - Right column — "Last Week" label, total count, and a 7-day mini bar chart.
 *
 * Both mini bar charts share the same vertical scale so bar heights are directly comparable.
 * This-week bars are rendered at full primary-color opacity; last-week bars are dimmed to 40 %.
 *
 * When [WeekComparisonData.hasLastWeekData] is false (first week of usage) only the "This Week"
 * column is shown with a short note encouraging the user to return once they have a full week.
 */
@Composable
fun WeekComparison(
    modifier: Modifier = Modifier,
    data: WeekComparisonData,
) {
    StatCard(
        modifier = modifier,
        title = "This Week vs Last Week",
    ) {
        if (data.hasLastWeekData) FullComparison(data = data)
        else FirstWeekContent(data = data)
    }
}

// region Full comparison (≥ 7 days of data)

@Composable
private fun FullComparison(data: WeekComparisonData) {
    val primary = MaterialTheme.colorScheme.primary
    // Shared max so both mini charts use the same vertical scale
    val sharedMax = maxOf(
        data.thisWeekDailyCounts.maxOrNull() ?: 0,
        data.lastWeekDailyCounts.maxOrNull() ?: 0,
        1,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeekColumn(
            modifier = Modifier.weight(1f),
            label = "This Week",
            total = data.thisWeekTotal,
            dailyCounts = data.thisWeekDailyCounts,
            sharedMax = sharedMax,
            barColor = primary,
            horizontalAlignment = Alignment.Start,
        )

        DeltaBadge(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .align(Alignment.Top),
            delta = data.delta,
        )

        WeekColumn(
            modifier = Modifier.weight(1f),
            label = "Last Week",
            total = data.lastWeekTotal,
            dailyCounts = data.lastWeekDailyCounts,
            sharedMax = sharedMax,
            barColor = primary.copy(alpha = 0.4f),
            horizontalAlignment = Alignment.End,
        )
    }
}

@Composable
private fun WeekColumn(
    modifier: Modifier = Modifier,
    label: String,
    total: Int,
    dailyCounts: List<Int>,
    sharedMax: Int,
    barColor: Color,
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        // Normalize to shared scale so both charts are visually comparable
        val bars = dailyCounts.mapIndexed { index, count ->
            BarEntry(
                value = if (sharedMax > 0) count.toFloat() / sharedMax else 0f,
                label = dayOfWeekLetters[index],
                color = barColor,
            )
        }
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            bars = bars,
            style = BarChartStyle.default(
                barSpacing = 3.dp,
                barCornerRadius = 3.dp,
            ),
        )
    }
}

@Composable
private fun DeltaBadge(
    modifier: Modifier = Modifier,
    delta: Int,
) {
    val (color, arrow, label) = when {
        delta > 0 -> Triple(Color(0xFF4CAF50), "↑", "+$delta")
        delta < 0 -> Triple(MaterialTheme.colorScheme.error, "↓", "$delta")
        else -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, "=", "Same")
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = arrow,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

// endregion

// region First-week content (< 7 days of data)

@Composable
private fun FirstWeekContent(data: WeekComparisonData) {
    val primary = MaterialTheme.colorScheme.primary
    val sharedMax = maxOf(data.thisWeekDailyCounts.maxOrNull() ?: 0, 1)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${data.thisWeekTotal} records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(10.dp))

        val bars = data.thisWeekDailyCounts.mapIndexed { index, count ->
            BarEntry(
                value = if (sharedMax > 0) count.toFloat() / sharedMax else 0f,
                label = dayOfWeekLetters[index],
                color = primary,
            )
        }
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            bars = bars,
            style = BarChartStyle.default(
                barSpacing = 4.dp,
                barCornerRadius = 3.dp,
            ),
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Complete your first full week to see comparisons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

// endregion

// region Previews

@Preview(name = "Light — Positive Delta", showBackground = true)
@Preview(name = "Dark — Positive Delta", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PositiveDeltaPreview() {
    LimitlessTheme {
        WeekComparison(
            modifier = Modifier.padding(16.dp),
            data = WeekComparisonData(
                thisWeekTotal = 42,
                lastWeekTotal = 39,
                thisWeekDailyCounts = listOf(7, 5, 6, 8, 7, 5, 4),
                lastWeekDailyCounts = listOf(6, 5, 5, 7, 6, 5, 5),
                hasLastWeekData = true,
            ),
        )
    }
}

@Preview(name = "Light — Negative Delta", showBackground = true)
@Preview(name = "Dark — Negative Delta", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NegativeDeltaPreview() {
    LimitlessTheme {
        WeekComparison(
            modifier = Modifier.padding(16.dp),
            data = WeekComparisonData(
                thisWeekTotal = 22,
                lastWeekTotal = 35,
                thisWeekDailyCounts = listOf(4, 3, 4, 5, 3, 2, 1),
                lastWeekDailyCounts = listOf(5, 5, 5, 6, 5, 5, 4),
                hasLastWeekData = true,
            ),
        )
    }
}

@Preview(name = "Light — No Change", showBackground = true)
@Preview(name = "Dark — No Change", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SameDeltaPreview() {
    LimitlessTheme {
        WeekComparison(
            modifier = Modifier.padding(16.dp),
            data = WeekComparisonData(
                thisWeekTotal = 30,
                lastWeekTotal = 30,
                thisWeekDailyCounts = listOf(4, 4, 4, 5, 5, 4, 4),
                lastWeekDailyCounts = listOf(4, 4, 4, 5, 5, 4, 4),
                hasLastWeekData = true,
            ),
        )
    }
}

@Preview(name = "Light — First Week", showBackground = true)
@Preview(name = "Dark — First Week", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FirstWeekPreview() {
    LimitlessTheme {
        WeekComparison(
            modifier = Modifier.padding(16.dp),
            data = WeekComparisonData(
                thisWeekTotal = 14,
                lastWeekTotal = 0,
                thisWeekDailyCounts = listOf(3, 2, 4, 5, 0, 0, 0),
                lastWeekDailyCounts = listOf(0, 0, 0, 0, 0, 0, 0),
                hasLastWeekData = false,
            ),
        )
    }
}

@Preview(name = "Light — All Zeros", showBackground = true)
@Preview(name = "Dark — All Zeros", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AllZerosPreview() {
    LimitlessTheme {
        WeekComparison(
            modifier = Modifier.padding(16.dp),
            data = WeekComparisonData(
                thisWeekTotal = 0,
                lastWeekTotal = 0,
                thisWeekDailyCounts = listOf(0, 0, 0, 0, 0, 0, 0),
                lastWeekDailyCounts = listOf(0, 0, 0, 0, 0, 0, 0),
                hasLastWeekData = true,
            ),
        )
    }
}

// endregion
