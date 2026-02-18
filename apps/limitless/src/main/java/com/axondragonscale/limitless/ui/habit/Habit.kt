package com.axondragonscale.limitless.ui.habit

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.home.RecordHabitSheet
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 17/02/26
 */

@Composable
fun Habit(
    modifier: Modifier = Modifier,
    navController: NavController,
    habitId: Long,
) {
    val vm: HabitVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    Habit(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                is HabitUiEvent.NavigateBack -> navController.popBackStack()
                is HabitUiEvent.NavigateToEditor -> navController.navigate(Route.Editor(habitId = habitId))

                else -> vm.onEvent(event)
            }
        },
    )
}

@Composable
private fun Habit(
    modifier: Modifier = Modifier,
    uiState: HabitUiState,
    onEvent: (HabitUiEvent) -> Unit,
) {
    var showRecordSheet by remember { mutableStateOf(false) }

    if (showRecordSheet) {
        RecordHabitSheet(
            habitName = uiState.habit.name,
            onRecord = { message ->
                onEvent(HabitUiEvent.RecordHabit(message))
                showRecordSheet = false
            },
            onDismiss = { showRecordSheet = false },
        )
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        bottomBar = {
            AppBar(
                title = "Habit",
                uiAlign = UIAlign.Bottom,
                leadingItems = listOf(
                    AppBarItem(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = { onEvent(HabitUiEvent.NavigateBack) },
                    ),
                ),
                trailingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.Edit,
                        onClick = { onEvent(HabitUiEvent.NavigateToEditor) },
                    ),
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Header
            Text(
                text = uiState.habit.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (uiState.habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.habit.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (uiState.todayCount) {
                                0 -> "No activity yet"
                                1 -> "1 time"
                                else -> "${uiState.todayCount} times"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.todayCount > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }

                    IconButton(
                        onClick = { showRecordSheet = true },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Record habit",
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
        val uiState = remember {
            HabitUiState(
                isLoading = false,
                habit = HabitEntity(
                    id = 1,
                    name = "Meditation",
                    description = "10 minutes of mindfulness every day",
                ),
                todayCount = 3,
            )
        }
        Habit(uiState = uiState, onEvent = {})
    }
}

@Preview(name = "No Activity", showBackground = true)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        val uiState = remember {
            HabitUiState(
                isLoading = false,
                habit = HabitEntity(
                    id = 2,
                    name = "Exercise",
                    description = "",
                ),
                todayCount = 0,
            )
        }
        Habit(uiState = uiState, onEvent = {})
    }
}
