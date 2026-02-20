package com.axondragonscale.limitless.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.compose.thenIf
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.BitWeek
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.color
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 03/11/24
 */

@Composable
fun Editor(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: EditorVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    vm.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            EditorUiEffect.NavigateBack -> navController.popBackStack()
        }
    }

    Editor(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                EditorUiEvent.NavigateBack -> navController.popBackStack()

                else -> vm.onEvent(event)
            }
        }
    )
}

@Composable
private fun Editor(
    modifier: Modifier = Modifier,
    uiState: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit,
) = Surface(
    modifier = modifier
        .systemBarsPadding()
        .imePadding(),
    color = MaterialTheme.colorScheme.background,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .weight(1f, fill = false),
        ) {
            EditorFields(
                modifier = Modifier.padding(top = 16.dp),
                habit = uiState.habit,
                onHabitUpdated = { onEvent(EditorUiEvent.UpdateHabit(it)) },
            )

            ColorSection(
                modifier = Modifier.padding(top = 8.dp),
                selected = uiState.habit.color,
                onColorSelected = { onEvent(EditorUiEvent.UpdateHabit(uiState.habit.copy(color = it))) },
            )

            GoalsSection(
                modifier = Modifier.padding(top = 8.dp),
                goals = uiState.goals,
                onAddGoal = { onEvent(EditorUiEvent.AddGoal) },
                onUpdateGoal = { onEvent(EditorUiEvent.UpdateGoal(it)) },
                onDeleteGoal = { onEvent(EditorUiEvent.DeleteGoal(it)) },
            )

            RemindersSection(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                reminders = uiState.reminders,
                onAddReminder = { onEvent(EditorUiEvent.AddReminder) },
                onUpdateReminder = { onEvent(EditorUiEvent.UpdateReminder(it)) },
                onDeleteReminder = { onEvent(EditorUiEvent.DeleteReminder(it)) },
            )
        }

        AppBar(
            title = "Habit",
            leadingItems = listOf(
                AppBarItem(
                    icon = Icons.Default.Close,
                    onClick = { onEvent(EditorUiEvent.NavigateBack) }
                )
            ),
            trailingItems = listOf(
                AppBarItem(
                    icon = Icons.Default.Check,
                    onClick = { onEvent(EditorUiEvent.SaveHabit(uiState.habit)) }
                )
            )
        )
    }
}

@Composable
private fun EditorFields(
    modifier: Modifier = Modifier,
    habit: HabitEntity,
    onHabitUpdated: (HabitEntity) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = 8.dp),
    verticalArrangement = Arrangement.Bottom,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = habit.name,
        onValueChange = { onHabitUpdated(habit.copy(name = it)) },
        leadingIcon = { Icon(Icons.Default.Flag, null) },
        label = { Text("Name") },
        singleLine = true,
        maxLines = 1,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        value = habit.description,
        onValueChange = { onHabitUpdated(habit.copy(description = it)) },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
        label = { Text("Description") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSection(
    modifier: Modifier = Modifier,
    selected: HabitColor,
    onColorSelected: (HabitColor) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = 8.dp),
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp).size(20.dp),
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = selected.color,
        )
        Text(
            text = "Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HabitColor.entries.forEach { habitColor ->
                val isSelected = habitColor == selected
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color = habitColor.color)
                        .thenIf(isSelected) {
                            this.border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape,
                            )
                        }
                        .clickable { onColorSelected(habitColor) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        // Use a contrast-aware tint so the check is visible on both
                        val checkTint = if (habitColor.color.luminance() > 0.5f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = checkTint,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(EditorUiState()) }
        Editor(
            uiState = uiState,
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode - With Goals and Reminders", showBackground = true)
@Preview(
    name = "Dark Mode - With Goals and Reminders",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWithGoalsAndReminders() {
    LimitlessTheme {
        val uiState by remember {
            mutableStateOf(
                EditorUiState(
                    isLoading = false,
                    habit = HabitEntity(
                        id = 1,
                        name = "Meditation",
                        description = "Daily mindfulness practice",
                        color = HabitColor.RED,
                    ),
                    goals = listOf(
                        GoalEntity(id = 1, habitId = 1, type = GoalType.DAILY, target = 1),
                        GoalEntity(id = 2, habitId = 1, type = GoalType.WEEKLY, target = 5),
                    ),
                    reminders = listOf(
                        ReminderEntity(
                            id = 1,
                            habitId = 1,
                            type = ReminderType.DAILY,
                            time = LocalTime(9, 0),
                        ),
                        ReminderEntity(
                            id = 2,
                            habitId = 1,
                            type = ReminderType.WEEKLY,
                            time = LocalTime(18, 30),
                            daysOfWeek = BitWeek.of(1, 3, 5),
                        ),
                    ),
                )
            )
        }
        Editor(
            uiState = uiState,
            onEvent = {},
        )
    }
}

// endregion
