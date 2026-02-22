package com.axondragonscale.limitless.ui.day

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.formatShort
import com.axondragonscale.commons.formatTime
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.common.RecordItem
import com.axondragonscale.limitless.ui.common.RecordHabitSheet
import com.axondragonscale.limitless.ui.theme.HabitTheme
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 29/06/25
 */

private sealed interface RecordSheetMode {
    data object Add : RecordSheetMode
    data class Edit(val record: RecordEntity) : RecordSheetMode
}

@Composable
fun HabitDay(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HabitDayVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    HabitTheme(habitColor = uiState.habit.color) {
        HabitDay(
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            uiState = uiState,
            onEvent = { event ->
                when (event) {
                    is HabitDayUiEvent.NavigateBack -> navController.popBackStack()
                    else -> vm.onEvent(event)
                }
            },
        )
    }
}

@Composable
private fun HabitDay(
    modifier: Modifier = Modifier,
    uiState: HabitDayUiState,
    onEvent: (HabitDayUiEvent) -> Unit,
) {
    val formattedDate = remember(uiState.date) { uiState.date?.formatShort() ?: "" }
    var sheetMode by remember { mutableStateOf<RecordSheetMode?>(null) }

    when (val mode = sheetMode) {
        is RecordSheetMode.Add -> RecordHabitSheet(
            habit = uiState.habit,
            showTimePicker = true,
            initialDate = uiState.date,
            onRecord = { record -> onEvent(HabitDayUiEvent.SaveRecord(record)) },
            onDismiss = { sheetMode = null },
        )
        is RecordSheetMode.Edit -> RecordHabitSheet(
            habit = uiState.habit,
            initialRecord = mode.record,
            onRecord = { record -> onEvent(HabitDayUiEvent.SaveRecord(record)) },
            onDelete = { onEvent(HabitDayUiEvent.DeleteRecord(mode.record.id)) },
            onDismiss = { sheetMode = null },
        )
        null -> Unit
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        bottomBar = {
            AppBar(
                title = formattedDate,
                uiAlign = UIAlign.Bottom,
                leadingItems = listOf(
                    AppBarItem(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = { onEvent(HabitDayUiEvent.NavigateBack) },
                    ),
                ),
                trailingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.AddCircle,
                        onClick = { sheetMode = RecordSheetMode.Add },
                    ),
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    DaySummaryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        records = uiState.records,
                    )

                    if (uiState.records.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No records for this day",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            items(items = uiState.records, key = { it.id }) { record ->
                                RecordItem(record = record, onClick = { sheetMode = RecordSheetMode.Edit(record) })
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                )
                            }

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
    }
}

@Composable
private fun DaySummaryCard(
    modifier: Modifier = Modifier,
    records: List<RecordEntity>,
) {
    val count = records.size
    val firstTime = remember(records) {
        records.minOfOrNull { it.timestamp }?.let { Instant.fromEpochMilliseconds(it).formatTime() }
    }
    val lastTime = remember(records) {
        records.maxOfOrNull { it.timestamp }?.let { Instant.fromEpochMilliseconds(it).formatTime() }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = when (count) {
                        0 -> "No records"
                        1 -> "1 record"
                        else -> "$count records"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (count > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )

                if (count > 0 && firstTime != null && lastTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (count == 1 || firstTime == lastTime) firstTime else "$firstTime – $lastTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitDayPreview() {
    LimitlessTheme {
        Surface {
            HabitDay(
                uiState = HabitDayUiState(
                    habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL),
                    date = LocalDate(2026, 2, 12),
                    isLoading = false,
                    records = listOf(
                        RecordEntity(id = 1, habitId = 1, timestamp = 1739354400000, message = "Morning session"),
                        RecordEntity(id = 2, habitId = 1, timestamp = 1739340000000, message = ""),
                        RecordEntity(id = 3, habitId = 1, timestamp = 1739325600000, message = "Felt really focused"),
                    ),
                ),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Empty Light", showBackground = true)
@Preview(name = "Empty Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitDayEmptyPreview() {
    LimitlessTheme {
        Surface {
            HabitDay(
                uiState = HabitDayUiState(
                    habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL),
                    date = LocalDate(2026, 2, 5),
                    isLoading = false,
                    records = emptyList(),
                ),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun HabitDayLoadingPreview() {
    LimitlessTheme {
        Surface {
            HabitDay(
                uiState = HabitDayUiState(isLoading = true),
                onEvent = {},
            )
        }
    }
}

// endregion
