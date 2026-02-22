package com.axondragonscale.limitless.ui.stats

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.stats.model.TodayHabitStatus
import com.axondragonscale.limitless.ui.stats.model.TodayProgressData
import com.axondragonscale.limitless.ui.stats.widget.TodayProgress
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 22/07/25
 */

@Composable
fun Stats(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: StatsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Stats(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                StatsUiEvent.NavigateBack -> navController.popBackStack()
            }
        },
    )
}

@Composable
private fun Stats(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
) = Surface(modifier.systemBarsPadding()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        StatsContent(
            modifier = Modifier.weight(1f),
            uiState = uiState,
        )

        AppBar(
            title = "Statistics",
            leadingItems = listOf(
                AppBarItem(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onEvent(StatsUiEvent.NavigateBack) },
                ),
            ),
        )
    }
}

@Composable
private fun StatsContent(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
) {
    when {
        uiState.isLoading -> LoadingState(modifier = modifier)
        uiState.isEmpty -> EmptyState(modifier = modifier)
        else -> StatsList(modifier = modifier, uiState = uiState)
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                text = "Create a habit and start tracking to see your statistics here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatsList(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.todayProgress?.let { data ->
            item { TodayProgress(data = data) }
        }

        // Sections 2–6 will be added in subsequent steps
    }
}

// region Previews

@Preview(name = "Light — Content", showBackground = true)
@Preview(name = "Dark — Content", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContentPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(
                isLoading = false,
                isEmpty = false,
                todayProgress = TodayProgressData(
                    completed = 2,
                    total = 4,
                    habits = listOf(
                        TodayHabitStatus("Meditation", HabitColor.TEAL, isCompleted = false),
                        TodayHabitStatus("Exercise", HabitColor.ORANGE, isCompleted = false),
                        TodayHabitStatus("Reading", HabitColor.PURPLE, isCompleted = true),
                        TodayHabitStatus("Journaling", HabitColor.BLUE, isCompleted = true),
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light — Empty", showBackground = true)
@Preview(name = "Dark — Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(isLoading = false, isEmpty = true),
            onEvent = {},
        )
    }
}

@Preview(name = "Light — Loading", showBackground = true)
@Preview(name = "Dark — Loading", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingPreview() {
    LimitlessTheme {
        Stats(
            uiState = StatsUiState(isLoading = true),
            onEvent = {},
        )
    }
}

// endregion
