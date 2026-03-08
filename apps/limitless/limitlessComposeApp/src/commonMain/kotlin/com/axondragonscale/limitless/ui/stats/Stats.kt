package com.axondragonscale.limitless.ui.stats

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.habit.stats.widget.MonthlyTrendChart
import com.axondragonscale.limitless.ui.habit.stats.widget.WeeklyPatternChart
import com.axondragonscale.limitless.ui.model.MonthCount
import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.model.WeeklyPatternData
import com.axondragonscale.limitless.ui.stats.model.GlobalHeatMapData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyItem
import com.axondragonscale.limitless.ui.stats.model.TodayHabitStatus
import com.axondragonscale.limitless.ui.stats.model.TodayProgressData
import com.axondragonscale.limitless.ui.stats.model.WeekComparisonData
import com.axondragonscale.limitless.ui.stats.widget.GlobalActivityHeatMap
import com.axondragonscale.limitless.ui.stats.widget.HabitConsistencyRanking
import com.axondragonscale.limitless.ui.stats.widget.TodayProgress
import com.axondragonscale.limitless.ui.stats.widget.WeekComparison
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 22/07/25
 */

@Composable
fun Stats(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: StatsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Stats(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                StatsUiEvent.NavigateBack -> navController.popBackStack()
            }
        },
    )
}

@Composable
private fun Stats(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
) = Surface(modifier.systemBarsPadding()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        StatsContent(
            modifier = Modifier.weight(1f),
            uiState = uiState,
        )

        AppBar(
            title = "Statistics",
            leadingItems = listOf(
                AppBarItem(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onEvent(StatsUiEvent.NavigateBack) },
                ),
            ),
        )
    }
}

@Composable
private fun StatsContent(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
) {
    when {
        uiState.isLoading -> LoadingState(modifier = modifier)
        uiState.isEmpty -> EmptyState(modifier = modifier)
        else -> StatsList(modifier = modifier, uiState = uiState)
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                text = "Create a habit and start tracking to see your statistics here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatsList(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.todayProgress?.let { data ->
            item { TodayProgress(data = data) }
        }

        uiState.weekComparison?.let { data ->
            item { WeekComparison(data = data) }
        }

        uiState.habitConsistency?.let { data ->
            item { HabitConsistencyRanking(data = data) }
        }

        uiState.heatMap?.let { data ->
            item { GlobalActivityHeatMap(data = data) }
        }

        uiState.weeklyPattern?.let { data ->
            item { WeeklyPatternChart(data = data, color = MaterialTheme.colorScheme.primary, title = "Weekly Rhythm") }
        }

        uiState.monthlyTrend?.let { data ->
            item { MonthlyTrendChart(data = data, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

// region Previews

@Preview(name = "Light — Content", showBackground = true)
@Preview(name = "Dark — Content", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun ContentPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(
                isLoading = false,
                isEmpty = false,
                todayProgress = TodayProgressData(
                    completed = 2,
                    total = 4,
                    habits = listOf(
                        TodayHabitStatus("Meditation", HabitColor.TEAL, isCompleted = false),
                        TodayHabitStatus("Exercise", HabitColor.ORANGE, isCompleted = false),
                        TodayHabitStatus("Reading", HabitColor.PURPLE, isCompleted = true),
                        TodayHabitStatus("Journaling", HabitColor.BLUE, isCompleted = true),
                    ),
                ),
                weekComparison = WeekComparisonData(
                    thisWeekTotal = 42,
                    lastWeekTotal = 39,
                    thisWeekDailyCounts = listOf(7, 5, 6, 8, 7, 5, 4),
                    lastWeekDailyCounts = listOf(6, 5, 5, 7, 6, 5, 5),
                    hasLastWeekData = true,
                ),
                habitConsistency = HabitConsistencyData(
                    habits = listOf(
                        HabitConsistencyItem("Meditation",  HabitColor.TEAL,   0.92f, 92, 100),
                        HabitConsistencyItem("Exercise",    HabitColor.ORANGE, 0.85f, 85, 100),
                        HabitConsistencyItem("Reading",     HabitColor.PURPLE, 0.61f, 61, 100),
                        HabitConsistencyItem("Journaling",  HabitColor.BLUE,   0.44f, 44, 100),
                        HabitConsistencyItem("Cold Shower", HabitColor.SKY,    0.18f, 18, 100),
                    ),
                ),
                heatMap = GlobalHeatMapData(
                    dayCounts = emptyMap(),
                    maxCount = 0,
                    totalHabits = 5,
                ),
                weeklyPattern = WeeklyPatternData(
                    dayAverages = listOf(3.2f, 4.1f, 2.8f, 5.0f, 4.4f, 3.6f, 2.1f),
                ),
                monthlyTrend = MonthlyTrendData(
                    monthCounts = listOf(
                        MonthCount(year = 2025, month = 3, count = 28),
                        MonthCount(year = 2025, month = 4, count = 35),
                        MonthCount(year = 2025, month = 5, count = 31),
                        MonthCount(year = 2025, month = 6, count = 42),
                        MonthCount(year = 2025, month = 7, count = 19),
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light — Empty", showBackground = true)
@Preview(name = "Dark — Empty", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(isLoading = false, isEmpty = true),
            onEvent = {},
        )
    }
}

@Preview(name = "Light — Loading", showBackground = true)
@Preview(name = "Dark — Loading", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(isLoading = true),
            onEvent = {},
        )
    }
}

// endregion
