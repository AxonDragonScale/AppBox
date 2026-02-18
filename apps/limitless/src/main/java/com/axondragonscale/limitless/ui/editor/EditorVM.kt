package com.axondragonscale.limitless.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.core.ReminderScheduler
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 03/11/24
 */
class EditorVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val editorRoute = savedStateHandle.toRoute<Route.Editor>()

    val uiState = MutableStateFlow(EditorUiState())

    private val uiEffectChannel = Channel<EditorUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    // Track original IDs so we can diff on save
    private var originalReminderIds: Set<Long> = emptySet()
    private var originalGoalIds: Set<Long> = emptySet()

    // Counters for temporary IDs assigned to unsaved entities.
    // Starts at -1 and decrements so they never collide with Room's auto-generated positive IDs.
    private val reminderTempIdCounter = AtomicLong(-1L)
    private val goalTempIdCounter = AtomicLong(-1L)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = editorRoute.habitId?.let { repository.getHabit(it) } ?: HabitEntity.new()
            val reminders = if (editorRoute.habitId != null) {
                repository.getRemindersForHabitOnce(editorRoute.habitId)
            } else {
                emptyList()
            }
            val goals = if (editorRoute.habitId != null) {
                repository.getGoalsForHabitOnce(editorRoute.habitId)
            } else {
                emptyList()
            }
            originalReminderIds = reminders.map { it.id }.toSet()
            originalGoalIds = goals.map { it.id }.toSet()
            uiState.update {
                it.copy(isLoading = false, habit = habit, reminders = reminders, goals = goals)
            }
        }
    }

    fun onEvent(event: EditorUiEvent): Any = when (event) {
        is EditorUiEvent.UpdateHabit -> updateHabit(event)
        is EditorUiEvent.SaveHabit -> saveHabit(event)
        is EditorUiEvent.AddReminder -> addReminder()
        is EditorUiEvent.UpdateReminder -> updateReminder(event)
        is EditorUiEvent.DeleteReminder -> deleteReminder(event)
        is EditorUiEvent.AddGoal -> addGoal()
        is EditorUiEvent.UpdateGoal -> updateGoal(event)
        is EditorUiEvent.DeleteGoal -> deleteGoal(event)

        // Handled in compose
        EditorUiEvent.NavigateBack -> Unit
    }

    // region: Habit

    private fun updateHabit(event: EditorUiEvent.UpdateHabit) {
        uiState.update { it.copy(habit = event.habit) }
    }

    // endregion

    // region: Reminders

    private fun addReminder() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val defaultTime = LocalTime(
            hour = (now.hour + 1) % 24,
            minute = 0,
        )
        val newReminder = ReminderEntity(
            id = reminderTempIdCounter.getAndDecrement(),
            habitId = uiState.value.habit.id,
            type = ReminderType.DAILY,
            time = defaultTime,
        )
        uiState.update { it.copy(reminders = it.reminders + newReminder) }
    }

    private fun updateReminder(event: EditorUiEvent.UpdateReminder) {
        uiState.update { state ->
            state.copy(
                reminders = state.reminders.map { existing ->
                    if (existing.id == event.reminder.id) event.reminder else existing
                }
            )
        }
    }

    private fun deleteReminder(event: EditorUiEvent.DeleteReminder) {
        uiState.update { state ->
            state.copy(reminders = state.reminders.filter { it.id != event.reminderId })
        }
    }

    // endregion

    // region: Goals

    private fun addGoal() {
        val usedTypes = uiState.value.goals.map { it.type }.toSet()
        val nextType = GoalType.entries.firstOrNull { it !in usedTypes } ?: return
        val newGoal = GoalEntity(
            id = goalTempIdCounter.getAndDecrement(),
            habitId = uiState.value.habit.id,
            type = nextType,
            target = 1,
        )
        uiState.update { it.copy(goals = it.goals + newGoal) }
    }

    private fun updateGoal(event: EditorUiEvent.UpdateGoal) {
        uiState.update { state ->
            state.copy(
                goals = state.goals.map { existing ->
                    if (existing.id == event.goal.id) event.goal else existing
                }
            )
        }
    }

    private fun deleteGoal(event: EditorUiEvent.DeleteGoal) {
        uiState.update { state ->
            state.copy(goals = state.goals.filter { it.id != event.goalId })
        }
    }

    // endregion

    // region: Save

    private fun saveHabit(
        event: EditorUiEvent.SaveHabit
    ) = viewModelScope.launch(Dispatchers.IO) {
        // Save the habit first to get the ID (important for new habits)
        val savedHabitId = repository.saveHabit(habit = event.habit)
        // For new habits (id=0), Room returns the generated row ID.
        // For existing habits, Room returns the existing ID.
        val habitId = if (event.habit.id == 0L) savedHabitId else event.habit.id

        saveReminders(habitId)
        saveGoals(habitId)

        uiEffectChannel.send(EditorUiEffect.NavigateBack)
    }

    private suspend fun saveReminders(habitId: Long) {
        val currentReminders = uiState.value.reminders
        val currentReminderIds = currentReminders.filter { it.id > 0 }.map { it.id }.toSet()

        val deletedIds = originalReminderIds - currentReminderIds
        if (deletedIds.isNotEmpty()) {
            deletedIds.forEach { id -> reminderScheduler.cancel(id, habitId) }
            repository.deleteReminders(deletedIds.toList())
        }

        val remindersToSave = currentReminders.map {
            it.copy(habitId = habitId, id = if (it.id < 0) 0L else it.id)
        }
        for (reminder in remindersToSave) {
            val savedId = repository.saveReminder(reminder)
            val scheduledReminder = if (reminder.id == 0L) reminder.copy(id = savedId) else reminder
            reminderScheduler.schedule(scheduledReminder)
        }
    }

    private suspend fun saveGoals(habitId: Long) {
        val currentGoals = uiState.value.goals
        val currentGoalIds = currentGoals.filter { it.id > 0 }.map { it.id }.toSet()

        val deletedIds = originalGoalIds - currentGoalIds
        if (deletedIds.isNotEmpty()) repository.deleteGoals(deletedIds.toList())

        val goalsToSave = currentGoals.map {
            it.copy(habitId = habitId, id = if (it.id < 0) 0L else it.id)
        }
        repository.saveGoals(goalsToSave)
    }

    // endregion

}
