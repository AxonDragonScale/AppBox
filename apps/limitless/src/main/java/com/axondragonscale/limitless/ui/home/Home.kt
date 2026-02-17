package com.axondragonscale.limitless.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.model.HabitHeatMapData
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 01/11/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    Home(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                is HomeUiEvent.NavigateToStats -> navController.navigate(Route.Stats)
                is HomeUiEvent.AddHabit -> navController.navigate(Route.Editor())
                is HomeUiEvent.NavigateToHabit -> navController.navigate(Route.Habit(event.habitId))

                else -> vm.onEvent(event)
            }
        }
    )
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    var selectedHabit by remember { mutableStateOf<HabitHeatMapData?>(null) }

    selectedHabit?.let { habit ->
        RecordHabitSheet(
            habitName = habit.habitName,
            onRecord = { message ->
                onEvent(HomeUiEvent.RecordHabit(habit.habitId, message))
                selectedHabit = null
            },
            onDismiss = { selectedHabit = null },
        )
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        bottomBar = {
            AppBar(
                title = "Limitless",
                uiAlign = UIAlign.Bottom,
                leadingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.Settings,
                        onClick = { onEvent(HomeUiEvent.NavigateToSettings) },
                    ),
                ),
                trailingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.BarChart,
                        onClick = { onEvent(HomeUiEvent.NavigateToStats) },
                    ),
                    AppBarItem(
                        icon = Icons.Default.AddCircle,
                        onClick = { onEvent(HomeUiEvent.AddHabit) },
                    ),
                )
            )
        },
    ) { padding ->
        if (uiState.habits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No habits yet.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = uiState.habits,
                    key = { it.habitId },
                ) { habitData ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEvent(HomeUiEvent.NavigateToHabit(habitData.habitId)) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp,
                    ) {
                        HabitHeatMap(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            data = habitData,
                            highlightCurrentDay = uiState.highlightCurrentDay,
                            onRecordClick = { selectedHabit = habitData },
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val sampleCounts = buildMap {
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

        val uiState = remember {
            HomeUiState(
                habits = listOf(
                    HabitHeatMapData(
                        habitId = 1,
                        habitName = "Meditation",
                        dayCounts = sampleCounts,
                        maxCount = 4,
                    ),
                    HabitHeatMapData(
                        habitId = 2,
                        habitName = "Exercise",
                        dayCounts = sampleCounts.filterKeys { it.dayOfMonth % 2 == 0 },
                        maxCount = 4,
                    ),
                    HabitHeatMapData(
                        habitId = 3,
                        habitName = "Reading",
                        dayCounts = emptyMap(),
                        maxCount = 0,
                    ),
                )
            )
        }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        val uiState = remember { HomeUiState() }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}
