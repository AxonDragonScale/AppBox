package com.axondragonscale.limitless.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Created by Ronak Harkhani on 03/11/24
 */

@Composable
fun Editor(
    modifier: Modifier = Modifier,
    navController: NavController,
    editorRoute: Route.Editor,
) {
    val vm: EditorVM = koinViewModel(parameters = { parametersOf(editorRoute) })
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    vm.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            EditorUiEffect.NavigateBack -> navController.popBackStack()
        }
    }

    Editor(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                EditorUiEvent.NavigateBack -> navController.popBackStack()

                else -> vm.onEvent(event)
            }
        }
    )
}

@Composable
private fun Editor(
    modifier: Modifier = Modifier,
    uiState: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit,
) = Surface(
    modifier = modifier
        .systemBarsPadding()
        .imePadding(),
    color = MaterialTheme.colorScheme.background,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        val scrollState = rememberScrollState()
        EditorFields(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 16.dp)
                .verticalScroll(scrollState),
            focusRequester = focusRequester,
            habit = uiState.habit,
            onHabitUpdated = { onEvent(EditorUiEvent.UpdateHabit(it)) },
        )

        AppBar(
            title = "Habit",
            leadingItems = listOf(
                AppBarItem(
                    icon = Icons.Default.Close,
                    onClick = { onEvent(EditorUiEvent.NavigateBack) }
                )
            ),
            trailingItems = listOf(
                AppBarItem(
                    icon = Icons.Default.Check,
                    onClick = { onEvent(EditorUiEvent.SaveHabit(uiState.habit)) }
                )
            )
        )
    }
}

@Composable
private fun EditorFields(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    habit: HabitEntity,
    onHabitUpdated: (HabitEntity) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    verticalArrangement = Arrangement.Bottom,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = habit.name,
        onValueChange = { onHabitUpdated(habit.copy(name = it)) },
        leadingIcon = { Icon(Icons.Default.Flag, null) },
        label = { Text("Name") },
        singleLine = true,
        maxLines = 1,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        value = habit.description,
        onValueChange = { onHabitUpdated(habit.copy(description = it)) },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
        label = { Text("Description") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(EditorUiState()) }
        Editor(
            uiState = uiState,
            onEvent = { },
        )
    }
}
