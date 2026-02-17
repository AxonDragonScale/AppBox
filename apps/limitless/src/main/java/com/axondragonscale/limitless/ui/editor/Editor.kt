package com.axondragonscale.limitless.ui.editor

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.database.entity.isDaySelected
import com.axondragonscale.limitless.database.entity.toggleDay
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.common.Dropdown
import com.axondragonscale.limitless.ui.common.DropdownItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock

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

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 16.dp)
                .verticalScroll(scrollState)
                .weight(1f, fill = false),
        ) {
            EditorFields(
                focusRequester = focusRequester,
                habit = uiState.habit,
                onHabitUpdated = { onEvent(EditorUiEvent.UpdateHabit(it)) },
            )

            RemindersSection(
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
    focusRequester: FocusRequester,
    habit: HabitEntity,
    onHabitUpdated: (HabitEntity) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    verticalArrangement = Arrangement.Bottom,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
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

// region Reminders Section

@Composable
private fun RemindersSection(
    modifier: Modifier = Modifier,
    reminders: List<ReminderEntity>,
    onAddReminder: () -> Unit,
    onUpdateReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(top = 20.dp),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        FilledTonalIconButton(
            onClick = onAddReminder,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Reminder",
                modifier = Modifier.size(18.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (reminders.isEmpty()) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "No reminders set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }

    reminders.forEach { reminder ->
        AnimatedVisibility(
            visible = true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            ReminderCard(
                modifier = Modifier.padding(bottom = 8.dp),
                reminder = reminder,
                onUpdate = onUpdateReminder,
                onDelete = { onDeleteReminder(reminder.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(
    modifier: Modifier = Modifier,
    reminder: ReminderEntity,
    onUpdate: (ReminderEntity) -> Unit,
    onDelete: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val typeItems = remember {
        ReminderType.entries.map { DropdownItem(name = it.displayName, value = it) }
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top row: Type dropdown + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Dropdown(
                    selectedItem = typeItems.first { it.value == reminder.type },
                    items = typeItems,
                    onItemSelected = { selected ->
                        val updated = when (selected.value) {
                            ReminderType.ONE_TIME -> reminder.copy(
                                type = selected.value,
                                date = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                daysOfWeekBitmask = null,
                                dayOfMonth = null,
                            )

                            ReminderType.DAILY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeekBitmask = null,
                                dayOfMonth = null,
                            )

                            ReminderType.WEEKLY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeekBitmask = ReminderEntity.dayToBit(
                                    Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date.dayOfWeek.isoDayNumber
                                ),
                                dayOfMonth = null,
                            )

                            ReminderType.MONTHLY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeekBitmask = null,
                                dayOfMonth = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date.dayOfMonth,
                            )
                        }
                        onUpdate(updated)
                    },
                    size = DpSize(width = 128.dp, height = 36.dp),
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Reminder",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Type-specific pickers
            when (reminder.type) {
                ReminderType.ONE_TIME -> {
                    // Date + Time side by side, full width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PickerChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CalendarMonth,
                            label = reminder.date?.let { formatDate(it) } ?: "Select date",
                            onClick = { showDatePicker = true },
                        )
                        PickerChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Schedule,
                            label = formatTime(reminder.time),
                            onClick = { showTimePicker = true },
                        )
                    }
                }

                ReminderType.DAILY -> {
                    PickerChip(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Schedule,
                        label = formatTime(reminder.time),
                        onClick = { showTimePicker = true },
                    )
                }

                ReminderType.WEEKLY -> {
                    PickerChip(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Schedule,
                        label = formatTime(reminder.time),
                        onClick = { showTimePicker = true },
                    )
                    DayOfWeekSelector(
                        selectedBitmask = reminder.daysOfWeekBitmask ?: 0,
                        onBitmaskChanged = {
                            onUpdate(reminder.copy(daysOfWeekBitmask = it))
                        },
                    )
                }

                ReminderType.MONTHLY -> {
                    PickerChip(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Schedule,
                        label = formatTime(reminder.time),
                        onClick = { showTimePicker = true },
                    )
                    DayOfMonthSelector(
                        selectedDay = reminder.dayOfMonth,
                        onDaySelected = { onUpdate(reminder.copy(dayOfMonth = it)) },
                    )
                }
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = reminder.time.hour,
            initialMinute = reminder.time.minute,
            onConfirm = { hour, minute ->
                onUpdate(reminder.copy(time = LocalTime(hour, minute)))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    // Date picker dialog (for one-time reminders)
    if (showDatePicker) {
        ReminderDatePickerDialog(
            initialDate = reminder.date,
            onDateSelected = {
                onUpdate(reminder.copy(date = it))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun PickerChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val initialMillis = initialDate?.atStartOfDayIn(tz)?.toEpochMilliseconds()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selected = Instant.fromEpochMilliseconds(selectedMillis)
                            .toLocalDateTime(tz).date
                        onDateSelected(selected)
                    }
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun DayOfWeekSelector(
    modifier: Modifier = Modifier,
    selectedBitmask: Int,
    onBitmaskChanged: (Int) -> Unit,
) {
    val days = remember {
        listOf(
            DayOfWeek.MONDAY to "M",
            DayOfWeek.TUESDAY to "T",
            DayOfWeek.WEDNESDAY to "W",
            DayOfWeek.THURSDAY to "T",
            DayOfWeek.FRIDAY to "F",
            DayOfWeek.SATURDAY to "S",
            DayOfWeek.SUNDAY to "S",
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        days.forEach { (dayOfWeek, label) ->
            val isSelected = selectedBitmask.isDaySelected(dayOfWeek.isoDayNumber)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable {
                        onBitmaskChanged(selectedBitmask.toggleDay(dayOfWeek.isoDayNumber))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DayOfMonthSelector(
    modifier: Modifier = Modifier,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
) {
    val dayItems = remember {
        (1..31).map { DropdownItem(name = it.toString(), value = it) }
    }
    val currentDay = selectedDay ?: 1

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Day of Month",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        Dropdown(
            selectedItem = dayItems.first { it.value == currentDay },
            items = dayItems,
            onItemSelected = { onDaySelected(it.value) },
            size = DpSize(88.dp, 32.dp),
        )
    }
}

// endregion

// region Dialogs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )

                TimePicker(state = timePickerState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = "OK",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Utilities

private fun formatTime(time: LocalTime): String {
    val hour12 = when {
        time.hour == 0 -> 12
        time.hour > 12 -> time.hour - 12
        else -> time.hour
    }
    val amPm = if (time.hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(hour12, time.minute, amPm)
}

private fun formatDate(date: LocalDate): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}

// endregion

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(EditorUiState()) }
        Editor(
            uiState = uiState,
            onEvent = { },
        )
    }
}

@Preview(name = "Light Mode - With Reminders", showBackground = true)
@Preview(
    name = "Dark Mode - With Reminders",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWithReminders() {
    LimitlessTheme {
        val uiState by remember {
            mutableStateOf(
                EditorUiState(
                    isLoading = false,
                    habit = HabitEntity(
                        id = 1,
                        name = "Meditation",
                        description = "Daily mindfulness practice",
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
                            daysOfWeekBitmask = ReminderEntity.dayToBit(1) or ReminderEntity.dayToBit(3) or ReminderEntity.dayToBit(5),
                        ),
                        ReminderEntity(
                            id = 3,
                            habitId = 1,
                            type = ReminderType.ONE_TIME,
                            time = LocalTime(14, 0),
                            date = LocalDate(2025, 7, 1),
                        ),
                    ),
                )
            )
        }
        Editor(
            uiState = uiState,
            onEvent = { },
        )
    }
}
