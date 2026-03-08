package com.axondragonscale.limitless.ui.habit.history

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.common.RecordHabitSheet
import com.axondragonscale.limitless.ui.common.RecordItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.compose.viewmodel.koinViewModel

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
    var selectedRecord by remember { mutableStateOf<RecordEntity?>(null) }

    selectedRecord?.let { record ->
        RecordHabitSheet(
            habit = uiState.habit,
            initialRecord = record,
            onRecord = { record -> onEvent(HistoryUiEvent.SaveRecord(record)) },
            onDelete = { onEvent(HistoryUiEvent.DeleteRecord(record.id)) },
            onDismiss = { selectedRecord = null },
        )
    }

    val listState = rememberLazyListState()

    // Trigger load-more when the user is within 5 items of the end of the loaded records
    val shouldLoadMore by remember(uiState) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            uiState.hasMore && !uiState.isLoadingMore && lastVisible >= uiState.records.size - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onEvent(HistoryUiEvent.LoadMore)
    }

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
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                items(items = uiState.records, key = { it.id }) { record ->
                    RecordItem(record = record, onClick = { selectedRecord = record })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }

                if (uiState.hasMore) {
                    item(key = "load_more") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    item(key = "end_of_list") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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



@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryPreview() {
    LimitlessTheme {
        Surface {
            val uiState = HistoryUiState(
                habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL),
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

@Preview(name = "End reached Light Mode", showBackground = true)
@Preview(name = "End reached Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryEndPreview() {
    LimitlessTheme {
        Surface {
            val uiState = HistoryUiState(
                habit = HabitEntity(id = 1, name = "Meditation", description = "", color = HabitColor.TEAL),
                isLoading = false,
                records = listOf(
                    RecordEntity(id = 1, habitId = 1, timestamp = 1750900200000, message = "Morning meditation session"),
                    RecordEntity(id = 2, habitId = 1, timestamp = 1750857000000, message = ""),
                    RecordEntity(id = 3, habitId = 1, timestamp = 1750813800000, message = "Felt really focused today"),
                    RecordEntity(id = 4, habitId = 1, timestamp = 1750770600000, message = "Quick 5 minute session"),
                    RecordEntity(id = 5, habitId = 1, timestamp = 1750684200000, message = ""),
                ),
                hasMore = false,
            )
            History(uiState = uiState, onEvent = {})
        }
    }
}

@Preview(name = "Empty Light", showBackground = true)
@Preview(name = "Empty Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
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
@Preview(name = "Loading Dark", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
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
