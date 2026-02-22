package com.axondragonscale.limitless.ui.habit

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.model.GoalStatus
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.habit.calendar.HabitCalendar
import com.axondragonscale.limitless.ui.habit.history.History
import com.axondragonscale.limitless.ui.common.RecordHabitSheet
import com.axondragonscale.limitless.ui.habit.stats.Stats
import com.axondragonscale.limitless.ui.theme.HabitTheme
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/**
 * Created by Ronak Harkhani on 17/02/26
 */

@Composable
fun Habit(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HabitVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    HabitTheme(habitColor = uiState.habit.color) {
        Habit(
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            uiState = uiState,
            onEvent = { event ->
                when (event) {
                    is HabitUiEvent.NavigateBack -> navController.popBackStack()
                    is HabitUiEvent.NavigateToEditor -> navController.navigate(Route.Editor(habitId = uiState.habit.id))
                    is HabitUiEvent.NavigateToDayDetail -> navController.navigate(Route.HabitDay(habitId = uiState.habit.id, date = event.date.toString()))

                    else -> vm.onEvent(event)
                }
            },
        )
    }
}

@Composable
private fun Habit(
    modifier: Modifier = Modifier,
    uiState: HabitUiState,
    onEvent: (HabitUiEvent) -> Unit,
) {
    var showRecordSheet by remember { mutableStateOf(false) }

    if (showRecordSheet) {
        RecordHabitSheet(
            habit = uiState.habit,
            onRecord = { record -> onEvent(HabitUiEvent.SaveRecord(record)) },
            onDismiss = { showRecordSheet = false },
        )
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        bottomBar = {
            AppBar(
                title = "Habit",
                uiAlign = UIAlign.Bottom,
                leadingItems = listOf(
                    AppBarItem(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = { onEvent(HabitUiEvent.NavigateBack) },
                    ),
                ),
                trailingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.Edit,
                        onClick = { onEvent(HabitUiEvent.NavigateToEditor) },
                    ),
                ),
            )
        },
    ) { padding ->
        var headerHeightPx by remember { mutableFloatStateOf(0f) }
        var headerOffsetPx by remember { mutableFloatStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // Scrolling up → collapse header first, then let inner content scroll
                    if (available.y < 0f) {
                        val newOffset = (headerOffsetPx + available.y).coerceIn(-headerHeightPx, 0f)
                        val consumed = newOffset - headerOffsetPx
                        headerOffsetPx = newOffset
                        return Offset(0f, consumed)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    // Scrolling down → inner content scrolls first, then expand header with leftovers
                    if (available.y > 0f) {
                        val newOffset = (headerOffsetPx + available.y).coerceIn(-headerHeightPx, 0f)
                        val expandedBy = newOffset - headerOffsetPx
                        headerOffsetPx = newOffset
                        return Offset(0f, expandedBy)
                    }
                    return Offset.Zero
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(nestedScrollConnection),
        ) {
            // Collapsible header
            Column(
                modifier = Modifier
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val height = (placeable.height + headerOffsetPx.roundToInt()).coerceAtLeast(0)
                        layout(placeable.width, height) {
                            placeable.place(0, headerOffsetPx.roundToInt())
                        }
                    }
                    .onSizeChanged { headerHeightPx = it.height.toFloat() }
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
            ) {
                Text(
                    text = uiState.habit.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                if (uiState.habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.habit.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TodaySummary(
                    todayCount = uiState.todayCount,
                    onRecordClick = { showRecordSheet = true },
                )

                if (uiState.goals.isNotEmpty()) {
                    GoalsSection(
                        modifier = Modifier.padding(top = 16.dp),
                        goals = uiState.goals,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            HabitTabPager(
                modifier = Modifier.weight(1f),
                onNavigateToDayDetail = { onEvent(HabitUiEvent.NavigateToDayDetail(it)) },
            )
        }
    }
}


@Composable
private fun TodaySummary(
    modifier: Modifier = Modifier,
    todayCount: Int,
    onRecordClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (todayCount) {
                        0 -> "No activity yet"
                        1 -> "1 time"
                        else -> "$todayCount times"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (todayCount > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }

            IconButton(
                onClick = onRecordClick,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Record habit",
                )
            }
        }
    }
}

@Composable
private fun GoalsSection(
    modifier: Modifier = Modifier,
    goals: List<GoalStatus>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Goals",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        goals.forEach { goalStatus ->
            GoalCard(
                modifier = Modifier.padding(bottom = 8.dp),
                goalStatus = goalStatus,
            )
        }
    }
}

@Composable
private fun GoalCard(
    modifier: Modifier = Modifier,
    goalStatus: GoalStatus,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp).size(18.dp),
                    imageVector = if (goalStatus.isAchieved) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Goal achieved",
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "${goalStatus.goal.type.displayName} Goal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${goalStatus.current} / ${goalStatus.goal.target}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (goalStatus.isAchieved) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { goalStatus.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
            )
        }
    }
}

@Composable
private fun HabitTabPager(
    modifier: Modifier = Modifier,
    onNavigateToDayDetail: (date: LocalDate) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { HabitTab.entries.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            HabitTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = tab.title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (HabitTab.entries[page]) {
                HabitTab.History -> History()

                HabitTab.Calendar -> Box(modifier = Modifier.fillMaxSize()) {
                    HabitCalendar(onNavigateToDayDetail = onNavigateToDayDetail)
                }

                HabitTab.Stats -> Stats()
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState = remember {
            HabitUiState(
                isLoading = false,
                habit = HabitEntity(
                    id = 1,
                    name = "Meditation",
                    description = "10 minutes of mindfulness every day",
                    color = HabitColor.RED,
                ),
                todayCount = 3,
                goals = listOf(
                    GoalStatus(GoalEntity(habitId = 1, type = GoalType.DAILY, target = 1), current = 3),
                    GoalStatus(GoalEntity(habitId = 1, type = GoalType.WEEKLY, target = 4), current = 3),
                    GoalStatus(GoalEntity(habitId = 1, type = GoalType.MONTHLY, target = 20), current = 12),
                ),
            )
        }
        Habit(uiState = uiState, onEvent = {})
    }
}

@Preview(name = "No Goals", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoGoalsPreview() {
    LimitlessTheme {
        val uiState = remember {
            HabitUiState(
                isLoading = false,
                habit = HabitEntity(
                    id = 2,
                    name = "Exercise",
                    description = "",
                    color = HabitColor.BLUE,
                ),
                todayCount = 1,
            )
        }
        Habit(uiState = uiState, onEvent = {})
    }
}

@Preview(name = "No Activity", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        val uiState = remember {
            HabitUiState(
                isLoading = false,
                habit = HabitEntity(
                    id = 2,
                    name = "Exercise",
                    description = "",
                    color = HabitColor.GREEN,
                ),
                todayCount = 0,
            )
        }
        Habit(uiState = uiState, onEvent = {})
    }
}
