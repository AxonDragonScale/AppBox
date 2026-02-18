package com.axondragonscale.limitless.ui.editor

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.canPostNotifications
import com.axondragonscale.commons.canScheduleExactAlarms
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.openExactAlarmSettings
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.BitWeek
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.common.Dropdown
import com.axondragonscale.limitless.ui.common.DropdownItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

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

            GoalsSection(
                modifier = Modifier.padding(top = 8.dp),
                goals = uiState.goals,
                onAddGoal = { onEvent(EditorUiEvent.AddGoal) },
                onUpdateGoal = { onEvent(EditorUiEvent.UpdateGoal(it)) },
                onDeleteGoal = { onEvent(EditorUiEvent.DeleteGoal(it)) },
            )

            RemindersSection(
                modifier = Modifier.padding(top = 8.dp),
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
        .padding(bottom = 16.dp),
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

// region Goals Section

@Composable
private fun GoalsSection(
    modifier: Modifier = Modifier,
    goals: List<GoalEntity>,
    onAddGoal: () -> Unit,
    onUpdateGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (Long) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = if (goals.isEmpty()) 16.dp else 8.dp),
) {
    val allTypesUsed = goals.size >= GoalType.entries.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp).size(20.dp),
            imageVector = Icons.Default.RadioButtonChecked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Goals",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = onAddGoal,
            enabled = !allTypesUsed,
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add Goal",
                tint = if (allTypesUsed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                       else LocalContentColor.current,
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (goals.isEmpty()) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "No goals set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    goals.forEach { goal ->
        AnimatedVisibility(
            visible = true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            GoalCard(
                modifier = Modifier.padding(bottom = 8.dp),
                goal = goal,
                usedTypes = goals.map { it.type }.toSet(),
                onUpdate = onUpdateGoal,
                onDelete = { onDeleteGoal(goal.id) },
            )
        }
    }
}

@Composable
private fun GoalCard(
    modifier: Modifier = Modifier,
    goal: GoalEntity,
    usedTypes: Set<GoalType>,
    onUpdate: (GoalEntity) -> Unit,
    onDelete: () -> Unit,
) {
    // Available types = all types minus those already used by other goals (current type always selectable)
    val typeItems = remember(usedTypes, goal.type) {
        GoalType.entries
            .filter { it == goal.type || it !in usedTypes }
            .map { DropdownItem(name = it.displayName, value = it) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Dropdown(
                    selectedItem = typeItems.first { it.value == goal.type },
                    items = typeItems,
                    onItemSelected = { onUpdate(goal.copy(type = it.value)) },
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
                        contentDescription = "Delete Goal",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            TargetCounter(
                modifier = Modifier.fillMaxWidth(),
                value = goal.target,
                onValueChange = { onUpdate(goal.copy(target = it)) },
            )
        }
    }
}

@Composable
private fun TargetCounter(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .clip(RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
                onClick = {
                    val newVal = value - 1
                    if (newVal >= 1) onValueChange(newVal)
                },
                enabled = value > 1,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease target",
                )
            }

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val parsed = textValue.toIntOrNull()
                            textValue = if (parsed != null && parsed >= 1) parsed.toString()
                            else value.toString()
                        }
                    },
                value = textValue,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }
                    textValue = filtered
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null && parsed >= 1) onValueChange(parsed)
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            IconButton(
                modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
                onClick = { onValueChange(value + 1) },
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase target",
                )
            }
        }
    }
}

// endregion

// region Reminders Section

@Composable
private fun RemindersSection(
    modifier: Modifier = Modifier,
    reminders: List<ReminderEntity>,
    onAddReminder: () -> Unit,
    onUpdateReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
) {
    val context = LocalContext.current
    var canScheduleExactAlarms by remember { mutableStateOf(context.canScheduleExactAlarms) }
    var canPostNotifications by remember { mutableStateOf(context.canPostNotifications) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        canPostNotifications = granted
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canScheduleExactAlarms = context.canScheduleExactAlarms
        canPostNotifications = context.canPostNotifications
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = if (reminders.isEmpty()) 16.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = 8.dp).size(20.dp),
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = onAddReminder,
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add Reminder",
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!canPostNotifications) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Notification permission required for reminders to appear. Tap to grant.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        if (!canScheduleExactAlarms) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { context.openExactAlarmSettings() },
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Alarm permission required for reminders to fire on time. Tap to grant.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        if (reminders.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = "No reminders set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    } // Column
} // RemindersSection

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
                                daysOfWeek = null,
                                dayOfMonth = null,
                            )

                            ReminderType.DAILY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeek = null,
                                dayOfMonth = null,
                            )

                            ReminderType.WEEKLY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeek = BitWeek.of(
                                    Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date.dayOfWeek.isoDayNumber
                                ),
                                dayOfMonth = null,
                            )

                            ReminderType.MONTHLY -> reminder.copy(
                                type = selected.value,
                                date = null,
                                daysOfWeek = null,
                                dayOfMonth = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date.day,
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
                        selectedBitmask = reminder.daysOfWeek ?: BitWeek.EMPTY,
                        onBitmaskChanged = { onUpdate(reminder.copy(daysOfWeek = it)) },
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
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
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
    selectedBitmask: BitWeek,
    onBitmaskChanged: (BitWeek) -> Unit,
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
                    .clickable { onBitmaskChanged(selectedBitmask.toggleDay(dayOfWeek.isoDayNumber)) },
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
        modifier = modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(120.dp),
            text = "Day of Month",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        Dropdown(
            selectedItem = dayItems.first { it.value == currentDay },
            items = dayItems,
            onItemSelected = { onDaySelected(it.value) },
            size = DpSize(160.dp, 32.dp),
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
