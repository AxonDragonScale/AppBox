package com.axondragonscale.commons.compose.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 29/06/25
 *
 * A reusable Material3 date picker wrapped in a [DatePickerDialog].
 * Works with [LocalDate] — handles epoch millis conversion internally.
 */
@Composable
fun DatePickerDialog(
    modifier: Modifier = Modifier,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = initialDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selected = Instant.fromEpochMilliseconds(selectedMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date
                            onDateSelected(selected)
                        }
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun DatePickerDialogPreview() {
    MaterialTheme {
        Surface {
            DatePickerDialog(
                onDateSelected = {},
                onDismiss = {},
            )
        }
    }
}

// endregion
