package com.axondragonscale.limitless.ui.home

import androidx.compose.ui.tooling.preview.AndroidUiModes
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.HabitHeatMapData
import com.axondragonscale.limitless.model.HabitView
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.common.HabitCard
import com.axondragonscale.limitless.ui.common.RecordHabitSheet
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 01/11/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Home(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                is HomeUiEvent.NavigateToStats -> navController.navigate(Route.Stats)
                is HomeUiEvent.AddHabit -> navController.navigate(Route.Editor())
                is HomeUiEvent.NavigateToHabit -> navController.navigate(Route.Habit(event.habitId))
                is HomeUiEvent.NavigateToDayDetail -> navController.navigate(Route.HabitDay(event.habitId, event.date.toString()))

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
    var selectedHabitData by remember { mutableStateOf<HabitHeatMapData?>(null) }

    selectedHabitData?.let { habitData ->
        RecordHabitSheet(
            habit = habitData.habit,
            onRecord = { record -> onEvent(HomeUiEvent.SaveRecord(record)) },
            onDismiss = { selectedHabitData = null },
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
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.habits.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No habits yet.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    key = { it.habit.id },
                ) { habitData ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEvent(HomeUiEvent.NavigateToHabit(habitData.habit.id)) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp,
                    ) {
                        HabitCard(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            data = habitData,
                            highlightCurrentDay = uiState.highlightCurrentDay,
                            onRecordClick = { selectedHabitData = habitData },
                            onDayClick = { date -> onEvent(HomeUiEvent.NavigateToDayDetail(habitData.habit.id, date)) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val sampleCounts = buildMap {
            for (i in 0 until 180) {
                val date = LocalDate.today().minus(i, DateTimeUnit.DAY)
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
                isLoading = false,
                habits = listOf(
                    HabitHeatMapData(
                        habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.RED),
                        dayCounts = sampleCounts,
                        maxCount = 4,
                    ),
                    HabitHeatMapData(
                        habit = HabitEntity(id = 2, name = "Exercise", description = "", color = HabitColor.BLUE, view = HabitView.MONTH),
                        dayCounts = sampleCounts.filterKeys { it.day % 2 == 0 },
                        maxCount = 4,
                    ),
                    HabitHeatMapData(
                        habit = HabitEntity(id = 3, name = "Reading", description = "", color = HabitColor.GREEN, view = HabitView.WEEK),
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
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        val uiState = remember { HomeUiState(isLoading = false) }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingPreview() {
    LimitlessTheme {
        val uiState = remember { HomeUiState(isLoading = true) }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}
