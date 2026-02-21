package com.axondragonscale.limitless.ui.habit.stats

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.limitless.ui.habit.stats.widget.GoalCompletionRings
import com.axondragonscale.limitless.ui.habit.stats.widget.MonthlyTrendChart
import com.axondragonscale.limitless.ui.habit.stats.widget.StreakCards
import com.axondragonscale.limitless.ui.habit.stats.widget.SummaryStatsGrid
import com.axondragonscale.limitless.ui.habit.stats.widget.TimeOfDayChart
import com.axondragonscale.limitless.ui.habit.stats.widget.WeeklyPatternChart
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 03/11/24
 */

@Composable
fun Stats(modifier: Modifier = Modifier) {
    val vm: HabitStatsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Stats(
        modifier = modifier,
        uiState = uiState,
        onEvent = vm::onEvent,
    )
}

@Composable
private fun Stats(
    modifier: Modifier = Modifier,
    uiState: HabitStatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.streakData?.let { data ->
            item { StreakCards(data = data, habitColor = uiState.habitColor) }
        }

        uiState.summaryData?.let { data ->
            item { SummaryStatsGrid(data = data, habitColor = uiState.habitColor) }
        }

//        uiState.dailyHistoryData?.let { data ->
//            item { DailyHistoryChart(data = data, habitColor = uiState.habitColor) }
//        }

        uiState.timeOfDayData?.let { data ->
            item { TimeOfDayChart(data = data, habitColor = uiState.habitColor) }
        }

        uiState.weeklyPatternData?.let { data ->
            item { WeeklyPatternChart(data = data, habitColor = uiState.habitColor) }
        }

        uiState.monthlyTrendData?.let { data ->
            item { MonthlyTrendChart(data = data, habitColor = uiState.habitColor) }
        }

        uiState.goalCompletionData?.let { data ->
            item { GoalCompletionRings(data = data, habitColor = uiState.habitColor) }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(HabitStatsUiState()) }
        Stats(
            uiState = uiState,
            onEvent = {},
        )
    }
}
