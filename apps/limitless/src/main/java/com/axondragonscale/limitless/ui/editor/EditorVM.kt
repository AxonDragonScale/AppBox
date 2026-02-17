package com.axondragonscale.limitless.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.core.ReminderScheduler
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
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

    // Track original reminder IDs so we can diff on save
    private var originalReminderIds: Set<Long> = emptySet()

    // Counter for temporary IDs assigned to unsaved reminders.
    // Starts at -1 and decrements so they never collide with Room's auto-generated positive IDs.
    private val tempIdCounter = AtomicLong(-1L)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = editorRoute.habitId?.let { repository.getHabit(it) } ?: HabitEntity.new()
            val reminders = if (editorRoute.habitId != null) {
                repository.getRemindersForHabitOnce(editorRoute.habitId)
            } else {
                emptyList()
            }
            originalReminderIds = reminders.map { it.id }.toSet()
            uiState.update {
                it.copy(isLoading = false, habit = habit, reminders = reminders)
            }
        }
    }

    fun onEvent(event: EditorUiEvent): Any = when (event) {
        is EditorUiEvent.UpdateHabit -> updateHabit(event)
        is EditorUiEvent.SaveHabit -> saveHabit(event)
        is EditorUiEvent.AddReminder -> addReminder()
        is EditorUiEvent.UpdateReminder -> updateReminder(event)
        is EditorUiEvent.DeleteReminder -> deleteReminder(event)

        // Handled in compose
        EditorUiEvent.NavigateBack -> Unit
    }

    private fun updateHabit(event: EditorUiEvent.UpdateHabit) {
        uiState.update { it.copy(habit = event.habit) }
    }

    private fun addReminder() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        // Default to a daily reminder at the next full hour
        val defaultTime = LocalTime(
            hour = (now.hour + 1) % 24,
            minute = 0,
        )
        val newReminder = ReminderEntity(
            // Assign a temporary negative ID so each unsaved reminder is uniquely identifiable.
            // On save, these are reset to 0 so Room auto-generates the real ID.
            id = tempIdCounter.getAndDecrement(),
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

    private fun saveHabit(
        event: EditorUiEvent.SaveHabit
    ) = viewModelScope.launch(Dispatchers.IO) {
        // Save the habit first to get the ID (important for new habits)
        val savedHabitId = repository.saveHabit(habit = event.habit)
        // For new habits (id=0), Room returns the generated row ID.
        // For existing habits, Room returns the existing ID.
        val habitId = if (event.habit.id == 0L) savedHabitId else event.habit.id

        val currentReminders = uiState.value.reminders
        val currentReminderIds = currentReminders.filter { it.id > 0 }.map { it.id }.toSet()

        // Delete reminders that were removed by the user
        val deletedIds = originalReminderIds - currentReminderIds
        if (deletedIds.isNotEmpty()) {
            // Cancel alarms for deleted reminders
            deletedIds.forEach { id -> reminderScheduler.cancel(id, habitId) }
            repository.deleteReminders(deletedIds.toList())
        }

        // Upsert all current reminders, associating them with the habit ID.
        // Reset negative temp IDs to 0 so Room auto-generates real IDs on insert.
        val remindersToSave = currentReminders.map {
            it.copy(
                habitId = habitId,
                id = if (it.id < 0) 0L else it.id,
            )
        }
        for (reminder in remindersToSave) {
            val savedId = repository.saveReminder(reminder)
            // Schedule the alarm (use saved ID for new reminders that had id=0)
            val scheduledReminder = if (reminder.id == 0L) {
                reminder.copy(id = savedId)
            } else {
                reminder
            }
            reminderScheduler.schedule(scheduledReminder)
        }

        uiEffectChannel.send(EditorUiEffect.NavigateBack)
    }

}
