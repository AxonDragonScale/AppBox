package com.axondragonscale.npinner.ui.notificationEditor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.unit.sp
import com.axondragonscale.npinner.model.Schedule
import com.axondragonscale.npinner.model.ScheduleType
import com.axondragonscale.npinner.ui.common.PickerButton
import com.axondragonscale.npinner.ui.common.SegmentedToggleButton
import com.axondragonscale.npinner.ui.common.TimePickerDialog
import com.axondragonscale.npinner.ui.theme.NPinnerTheme
import com.axondragonscale.npinner.util.formatted
import com.axondragonscale.npinner.util.toEpochMillis
import com.axondragonscale.npinner.util.toLocalDate
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by Ronak Harkhani on 29/04/23
 */

@Composable
fun Scheduler(
    schedule: Schedule?,
    onScheduleChange: (Schedule?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (schedule != null) {
                DateTimePicker(
                    schedule = schedule,
                    onScheduleChange = onScheduleChange,
                )
            } else {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "Schedule",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            
            IconToggleButton(
                modifier = Modifier.padding(12.dp),
                checked = schedule != null,
                onCheckedChange = {
                    onScheduleChange(if (it) Schedule.newInstance() else null)
                },
            ) {
                Icon(
                    imageVector = if (schedule != null) Icons.Outlined.Delete else Icons.Filled.Add,
                    contentDescription = if (schedule != null) "Delete Schedule" else "Add Schedule",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        
        if (schedule != null) {
            if (!schedule.isFuture) {
                InvalidScheduleWarning()
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = schedule.type != null,
                    onCheckedChange = {
                        onScheduleChange(schedule.copy(type = if (it) ScheduleType.DAY else null))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                
                Text(
                    text = "REPEAT EVERY...",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (schedule.type != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SegmentedToggleButton(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                items = ScheduleType.stringValues,
                defaultSelectedItemIndex = schedule.type?.ordinal ?: 0,
                onItemSelected = {
                    onScheduleChange(schedule.copy(type = ScheduleType.fromOrdinal(it)))
                },
                enabled = schedule.type != null,
            )
        }

        HorizontalDivider()
    }
}

@Composable
fun InvalidScheduleWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Warning",
            tint = MaterialTheme.colorScheme.onError,
        )
        
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = "This schedule is in the past. Please delete or edit your schedule.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    schedule: Schedule,
    onScheduleChange: (Schedule?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        var showDatePicker by remember { mutableStateOf(false) }
        PickerButton(
            text = schedule.date.formatted,
            onClick = { showDatePicker = true },
        )
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = schedule.asLocalDateTime.toEpochMillis(),
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis ?: return@TextButton
                            showDatePicker = false
                            onScheduleChange(schedule.copy(date = millis.toLocalDate()))
                        },
                    ) {
                        Text("Ok")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        var showTimePicker by remember { mutableStateOf(false) }
        PickerButton(
            text = schedule.time.formatted,
            onClick = { showTimePicker = true },
        )
        
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = schedule.time.hour,
                initialMinute = schedule.time.minute,
                is24Hour = false,
            )
            TimePickerDialog(
                onCancel = { showTimePicker = false },
                onConfirm = {
                    showTimePicker = false
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onScheduleChange(schedule.copy(time = newTime))
                },
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SchedulerNullPreview() {
    NPinnerTheme {
        Surface {
            Scheduler(
                schedule = null,
                onScheduleChange = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SchedulerPreview() {
    NPinnerTheme {
        Surface {
            Scheduler(
                schedule = Schedule(
                    date = LocalDate.now(),
                    time = LocalTime.now(),
                    type = ScheduleType.DAY,
                ),
                onScheduleChange = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SchedulerNoRepeatPreview() {
    NPinnerTheme {
        Surface {
            Scheduler(
                schedule = Schedule(
                    date = LocalDate.now(),
                    time = LocalTime.now(),
                    type = null,
                ),
                onScheduleChange = {},
            )
        }
    }
}
