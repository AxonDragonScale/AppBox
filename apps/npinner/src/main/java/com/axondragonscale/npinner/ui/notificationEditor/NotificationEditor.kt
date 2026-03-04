package com.axondragonscale.npinner.ui.notificationEditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.npinner.model.NPinnerNotification
import com.axondragonscale.npinner.model.Schedule
import com.axondragonscale.npinner.model.ScheduleType
import com.axondragonscale.npinner.ui.common.BottomBar
import com.axondragonscale.npinner.ui.common.BottomButton
import com.axondragonscale.npinner.ui.common.Divider
import com.axondragonscale.npinner.ui.common.IconActionButton
import com.axondragonscale.npinner.ui.common.TopBar
import com.axondragonscale.npinner.ui.theme.Dimen
import com.axondragonscale.npinner.ui.theme.NPinnerTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Created by Ronak Harkhani on 22/04/23
 */

@Composable
fun NotificationEditor(
    navController: NavController,
    viewModel: NotificationEditorViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationEditor(
        uiState = uiState,
        onBackClick = { navController.popBackStack() },
        onSaveClick = {
            viewModel.onSaveClick()
            navController.popBackStack()
        },
        onDeleteClick = {
            viewModel.onDeleteClick()
            navController.popBackStack()
        },
        onContentChange = viewModel::onContentChange,
        onScheduleChange = viewModel::onScheduleChange,
    )
}

@Composable
fun NotificationEditor(
    uiState: NotificationEditorUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContentChange: (String, String) -> Unit,
    onScheduleChange: (Schedule?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState !is NotificationEditorUiState.Success) return

    Box(modifier = modifier.fillMaxSize()) {
        TopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1F),
            title = uiState.topBarTitle
        )

        Column(
            modifier = Modifier
                .padding(
                    top = Dimen.TOP_BAR_HEIGHT,
                    bottom = Dimen.BOTTOM_BAR_HEIGHT
                )
        ) {
            // This nested column is to make the inner content scrollable using verticalScroll and
            // weight modifiers when the keyboard pushes the bottom bar up.
            // TODO: Check if we can make the topmost box a Column to avoid the unnecessary column
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1F, fill = false)
            ) {
                Editor(
                    title = uiState.notification.title,
                    description = uiState.notification.description,
                    onContentChange = onContentChange,
                )

                Divider()

                Scheduler(
                    schedule = uiState.notification.schedule,
                    onScheduleChange = onScheduleChange,
                )
            }
        }

        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1F),
            mainAction = {
                BottomButton(
                    modifier = Modifier.weight(1f),
                    title = uiState.bottomButtonText,
                    onClick = onSaveClick,
                    enabled = uiState.bottomButtonEnabled,
                )
            },
            leftAction = {
                IconActionButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = onBackClick,
                )
            },
            rightAction = {
                if (uiState.showDeleteButton) {
                    IconActionButton(
                        icon = Icons.Outlined.Delete,
                        onClick = onDeleteClick
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        )

    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorPreview() {
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Success(
                    createNew = true,
                    notification = NPinnerNotification.newInstance()
                ),
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}

@Preview(name = "Light Mode - With Content", showBackground = true)
@Preview(name = "Dark Mode - With Content", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorWithContentPreview() {
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Success(
                    createNew = true,
                    notification = NPinnerNotification.newInstance().copy(
                        title = "Buy groceries",
                        description = "Milk, eggs, bread, and coffee beans",
                    ),
                ),
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Future Schedule (Recurring)", showBackground = true)
@Preview(name = "Dark Mode - Future Schedule (Recurring)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorFutureSchedulePreview() {
    val future = Clock.System.now().plus(2.hours)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Success(
                    createNew = true,
                    notification = NPinnerNotification.newInstance().copy(
                        title = "Take medication",
                        description = "Vitamin D supplement after lunch",
                        schedule = Schedule(
                            date = future.date,
                            time = future.time,
                            type = ScheduleType.DAY,
                        ),
                    ),
                ),
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Future Schedule (One-time)", showBackground = true)
@Preview(name = "Dark Mode - Future Schedule (One-time)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorOneTimeSchedulePreview() {
    val future = Clock.System.now().plus(2.hours)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Success(
                    createNew = true,
                    notification = NPinnerNotification.newInstance().copy(
                        title = "Dentist appointment",
                        description = "Dr. Smith, Room 204",
                        schedule = Schedule(
                            date = future.date,
                            time = future.time,
                            type = null,
                        ),
                    ),
                ),
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Past Schedule", showBackground = true)
@Preview(name = "Dark Mode - Past Schedule", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorPastSchedulePreview() {
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Success(
                    createNew = false,
                    notification = NPinnerNotification.newInstance().copy(
                        title = "Submit report",
                        description = "Quarterly review document",
                        schedule = Schedule(
                            date = LocalDate(2024, 1, 1),
                            time = LocalTime(9, 0),
                            type = null,
                        ),
                    ),
                ),
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Loading", showBackground = true)
@Preview(name = "Dark Mode - Loading", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationEditorLoadingPreview() {
    NPinnerTheme {
        Surface {
            NotificationEditor(
                uiState = NotificationEditorUiState.Loading,
                onBackClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                onContentChange = { _, _ -> },
                onScheduleChange = {}
            )
        }
    }
}
