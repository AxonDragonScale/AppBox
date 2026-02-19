package com.axondragonscale.limitless.ui.habit.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 20/02/26
 */

@Composable
fun History(modifier: Modifier = Modifier) {
    val vm: HistoryVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    History(
        modifier = modifier,
        uiState = uiState,
        onEvent = vm::onEvent,
    )
}

@Composable
private fun History(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit,
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }

        uiState.records.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No records yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                items(items = uiState.records, key = { it.id }) { record ->
                    RecordItem(record = record)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }

                if (uiState.hasMore) {
                    item(key = "load_more") {
                        LaunchedEffect(uiState.records.size) {
                            onEvent(HistoryUiEvent.LoadMore)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    item(key = "end_of_list") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No more records",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordItem(
    modifier: Modifier = Modifier,
    record: RecordEntity,
) {
    val formattedDateTime = remember(record.timestamp) { formatTimestamp(record.timestamp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Text(
            text = formattedDateTime,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (record.message.isNotBlank()) {
            Text(
                text = record.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        } else {
            Text(
                text = "No description",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    val instant = kotlin.time.Instant.fromEpochMilliseconds(epochMillis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val day = dateTime.day
    val year = dateTime.year
    val hour = if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (dateTime.hour < 12) "AM" else "PM"

    return "$month $day, $year at $hour:$minute $amPm"
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryPreview() {
    LimitlessTheme {
        Surface {
            val uiState = HistoryUiState(
                isLoading = false,
                records = listOf(
                    RecordEntity(id = 1, habitId = 1, timestamp = 1750900200000, message = "Morning meditation session"),
                    RecordEntity(id = 2, habitId = 1, timestamp = 1750857000000, message = ""),
                    RecordEntity(id = 3, habitId = 1, timestamp = 1750813800000, message = "Felt really focused today"),
                    RecordEntity(id = 4, habitId = 1, timestamp = 1750770600000, message = "Quick 5 minute session"),
                    RecordEntity(id = 5, habitId = 1, timestamp = 1750684200000, message = ""),
                ),
                hasMore = true,
            )
            History(uiState = uiState, onEvent = {})
        }
    }
}

@Preview(name = "Empty Light", showBackground = true)
@Preview(name = "Empty Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyHistoryPreview() {
    LimitlessTheme {
        Surface {
            History(
                uiState = HistoryUiState(isLoading = false),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun LoadingHistoryPreview() {
    LimitlessTheme {
        Surface {
            History(
                uiState = HistoryUiState(isLoading = true),
                onEvent = {},
            )
        }
    }
}
