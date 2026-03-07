package com.axondragonscale.limitless.ui.editor

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.axondragonscale.commons.canPostNotifications
import com.axondragonscale.commons.canScheduleExactAlarms
import com.axondragonscale.commons.compose.datetime.DatePickerDialog
import com.axondragonscale.commons.compose.Dropdown
import com.axondragonscale.commons.compose.DropdownItem
import com.axondragonscale.commons.compose.datetime.TimePickerDialog
import com.axondragonscale.commons.formatAmPm
import com.axondragonscale.commons.formatShort
import com.axondragonscale.commons.openExactAlarmSettings
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.BitWeek
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.ui.common.PickerChip
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 03/11/24
 */

private val reminderTypeItems = ReminderType.entries.map { DropdownItem(name = it.displayName, value = it) }

@Composable
internal fun RemindersSection(
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
            .padding(bottom = if (reminders.isEmpty()) 8.dp else 0.dp)
            .animateContentSize(),
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
            ReminderCard(
                modifier = Modifier.padding(bottom = 8.dp),
                reminder = reminder,
                onUpdate = onUpdateReminder,
                onDelete = { onDeleteReminder(reminder.id) },
            )
        }
    }
}

@Composable
private fun ReminderCard(
    modifier: Modifier = Modifier,
    reminder: ReminderEntity,
    onUpdate: (ReminderEntity) -> Unit,
    onDelete: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                    modifier = Modifier.size(width = 128.dp, height = 36.dp),
                    selectedItem = reminderTypeItems.first { it.value == reminder.type },
                    items = reminderTypeItems,
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
                            label = reminder.date?.formatShort() ?: "Select date",
                            onClick = { showDatePicker = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                        PickerChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Schedule,
                            label = reminder.time.formatAmPm(),
                            onClick = { showTimePicker = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                ReminderType.DAILY -> {
                    PickerChip(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Schedule,
                        label = reminder.time.formatAmPm(),
                        onClick = { showTimePicker = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                ReminderType.WEEKLY -> {
                    PickerChip(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Schedule,
                        label = reminder.time.formatAmPm(),
                        onClick = { showTimePicker = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
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
                        label = reminder.time.formatAmPm(),
                        onClick = { showTimePicker = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    DayOfMonthSelector(
                        selectedDay = reminder.dayOfMonth,
                        onDaySelected = { onUpdate(reminder.copy(dayOfMonth = it)) },
                    )
                }
            }
        }
    }

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

    if (showDatePicker) {
        DatePickerDialog(
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
            modifier = Modifier.size(160.dp, 32.dp),
            selectedItem = dayItems.first { it.value == currentDay },
            items = dayItems,
            onItemSelected = { onDaySelected(it.value) },
        )
    }
}

// region Previews

@Preview(name = "Light Mode — Empty", showBackground = true)
@Preview(name = "Dark Mode — Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionEmptyPreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = emptyList(),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

@Preview(name = "Light Mode — Daily", showBackground = true)
@Preview(name = "Dark Mode — Daily", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionDailyPreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = listOf(
                    ReminderEntity(id = 1, habitId = 1, type = ReminderType.DAILY, time = LocalTime(8, 0)),
                ),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

@Preview(name = "Light Mode — Weekly", showBackground = true)
@Preview(name = "Dark Mode — Weekly", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionWeeklyPreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = listOf(
                    ReminderEntity(
                        id = 1,
                        habitId = 1,
                        type = ReminderType.WEEKLY,
                        time = LocalTime(18, 30),
                        daysOfWeek = BitWeek.of(
                            DayOfWeek.MONDAY.isoDayNumber,
                            DayOfWeek.WEDNESDAY.isoDayNumber,
                            DayOfWeek.FRIDAY.isoDayNumber,
                        ),
                    ),
                ),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

@Preview(name = "Light Mode — Monthly", showBackground = true)
@Preview(name = "Dark Mode — Monthly", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionMonthlyPreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = listOf(
                    ReminderEntity(
                        id = 1,
                        habitId = 1,
                        type = ReminderType.MONTHLY,
                        time = LocalTime(9, 0),
                        dayOfMonth = 15,
                    ),
                ),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

@Preview(name = "Light Mode — One Time", showBackground = true)
@Preview(name = "Dark Mode — One Time", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionOneTimePreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = listOf(
                    ReminderEntity(
                        id = 1,
                        habitId = 1,
                        type = ReminderType.ONE_TIME,
                        time = LocalTime(10, 0),
                        date = LocalDate(2025, 12, 25),
                    ),
                ),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

@Preview(name = "Light Mode — Multiple", showBackground = true)
@Preview(name = "Dark Mode — Multiple", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersSectionMultiplePreview() {
    LimitlessTheme {
        Surface {
            RemindersSection(
                reminders = listOf(
                    ReminderEntity(id = 1, habitId = 1, type = ReminderType.DAILY, time = LocalTime(7, 0)),
                    ReminderEntity(
                        id = 2,
                        habitId = 1,
                        type = ReminderType.WEEKLY,
                        time = LocalTime(20, 0),
                        daysOfWeek = BitWeek.of(
                            DayOfWeek.SATURDAY.isoDayNumber,
                            DayOfWeek.SUNDAY.isoDayNumber,
                        ),
                    ),
                ),
                onAddReminder = {},
                onUpdateReminder = {},
                onDeleteReminder = {},
            )
        }
    }
}

// endregion
