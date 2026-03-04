package com.axondragonscale.commons.compose

import android.content.res.Configuration
import androidx.compose.material3.DatePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import androidx.compose.material3.DatePickerDialog as M3DatePickerDialog

/**
 * Created by Ronak Harkhani on 29/06/25
 *
 * A reusable Material3 date picker wrapped in a [DatePickerDialog].
 * Works with [kotlinx.datetime.LocalDate] — handles epoch millis conversion internally.
 */
@Composable
fun DatePickerDialog(
    modifier: Modifier = Modifier,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val initialMillis = initialDate?.atStartOfDayIn(tz)?.toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    M3DatePickerDialog(
        modifier = modifier,
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

// region Previews

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DatePickerDialogPreview() {
    MaterialTheme {
        // Render the date picker surface directly — Dialog() itself
        // doesn't display inside the preview canvas.
        Surface(color = MaterialTheme.colorScheme.surface) {
            DatePicker(state = rememberDatePickerState())
        }
    }
}

// endregion
