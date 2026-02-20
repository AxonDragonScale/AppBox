package com.axondragonscale.limitless.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 09/06/25
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordHabitSheet(
    modifier: Modifier = Modifier,
    habitName: String,
    showDatePicker: Boolean = false,
    showTimePicker: Boolean = false,
    initialDate: LocalDate? = null,
    onRecord: (message: String, timestamp: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { },
    ) {
        // Custom drag handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .size(width = 48.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp),
                ),
        )

        RecordHabitSheetContent(
            habitName = habitName,
            showDatePicker = showDatePicker,
            showTimePicker = showTimePicker,
            initialDate = initialDate,
            onRecord = onRecord,
        )

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordHabitSheetContent(
    modifier: Modifier = Modifier,
    habitName: String,
    showDatePicker: Boolean = false,
    showTimePicker: Boolean = false,
    initialDate: LocalDate? = null,
    onRecord: (message: String, timestamp: Long) -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val now = remember { Clock.System.now() }
    val nowLocal = remember(now) { now.toLocalDateTime(tz) }

    val hasAnyPicker = showDatePicker || showTimePicker

    var message by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(initialDate ?: nowLocal.date) }
    var selectedHour by rememberSaveable { mutableIntStateOf(nowLocal.hour) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(nowLocal.minute) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val selectedInstant = remember(selectedDate, selectedHour, selectedMinute) {
        LocalDateTime(selectedDate, LocalTime(selectedHour, selectedMinute)).toInstant(tz)
    }
    val isInFuture = hasAnyPicker && selectedInstant > Clock.System.now()

    // Date picker dialog
    if (showDatePickerDialog) {
        RecordDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { newDate ->
                selectedDate = newDate
                // Clamp time if the newly selected date is today and the selected
                // time would now be in the future.
                if (newDate == nowLocal.date) {
                    val clampedInstant = LocalDateTime(newDate, LocalTime(selectedHour, selectedMinute))
                        .toInstant(tz)
                    if (clampedInstant > Clock.System.now()) {
                        selectedHour = nowLocal.hour
                        selectedMinute = nowLocal.minute
                    }
                }
                showDatePickerDialog = false
            },
            onDismiss = { showDatePickerDialog = false },
        )
    }

    // Time picker dialog
    if (showTimePickerDialog) {
        RecordTimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onConfirm = { hour, minute ->
                // Clamp to now if selection would be in the future
                val candidateInstant = LocalDateTime(selectedDate, LocalTime(hour, minute))
                    .toInstant(tz)
                if (candidateInstant > Clock.System.now()) {
                    selectedHour = nowLocal.hour
                    selectedMinute = nowLocal.minute
                } else {
                    selectedHour = hour
                    selectedMinute = minute
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false },
        )
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = habitName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        if (hasAnyPicker) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showDatePicker) {
                    PickerChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarMonth,
                        label = formatDate(selectedDate),
                        onClick = { showDatePickerDialog = true },
                    )
                }

                if (showTimePicker) {
                    PickerChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        label = formatTime(selectedHour, selectedMinute),
                        onClick = { showTimePickerDialog = true },
                        containerColor = if (isInFuture)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isInFuture)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            if (isInFuture) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Time cannot be in the future",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = message,
            onValueChange = { message = it },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
            label = { Text("Note (optional)") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            ),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val timestamp = if (hasAnyPicker) {
                    LocalDateTime(selectedDate, LocalTime(selectedHour, selectedMinute))
                        .toInstant(tz)
                        .toEpochMilliseconds()
                } else {
                    Clock.System.now().toEpochMilliseconds()
                }
                onRecord(message.trim(), timestamp)
            },
            enabled = !isInFuture,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(),
        ) {
            Text(
                text = "Record",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun PickerChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
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
private fun RecordDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date

    val initialMillis = remember(initialDate) {
        initialDate.atStartOfDayIn(tz).toEpochMilliseconds()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Allow dates up to and including today in local timezone.
                // DatePicker represents each day as UTC midnight, so we compare
                // the date portion only.
                val dateForMillis = Instant.fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.UTC).date
                return dateForMillis <= today
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selected = Instant.fromEpochMilliseconds(selectedMillis)
                            .toLocalDateTime(TimeZone.UTC).date
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTimePickerDialog(
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

// MARK: Helpers -----------------------------------------------------------------------------------

private fun formatDate(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$month ${date.day}, ${date.year}"
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    val m = minute.toString().padStart(2, '0')
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h:$m $amPm"
}

// MARK: Previews ----------------------------------------------------------------------------------

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordHabitSheetPreview() {
    LimitlessTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RecordHabitSheetContent(
                habitName = "Meditation",
                onRecord = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Time Only Light", showBackground = true)
@Preview(name = "Time Only Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordHabitSheetTimePickerPreview() {
    LimitlessTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RecordHabitSheetContent(
                habitName = "Meditation",
                showTimePicker = true,
                initialDate = LocalDate(2025, 6, 29),
                onRecord = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Date and Time Light", showBackground = true)
@Preview(name = "Date and Time Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordHabitSheetDateTimePreview() {
    LimitlessTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RecordHabitSheetContent(
                habitName = "Meditation",
                showDatePicker = true,
                showTimePicker = true,
                initialDate = LocalDate(2025, 6, 29),
                onRecord = { _, _ -> },
            )
        }
    }
}
