package com.axondragonscale.limitless.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 01/11/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    Home(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                HomeUiEvent.NavigateToStats -> navController.navigate(Route.Stats)
                HomeUiEvent.AddHabit -> navController.navigate(Route.Editor())

                else -> Unit
            }
        }
    )
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        bottomBar = {
            AppBar(
                title = "Limitless",
                uiAlign = UIAlign.Bottom,
                leadingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.Settings,
                        onClick = { onEvent(HomeUiEvent.NavigateToSettings) },
                    ),
                ),
                trailingItems = listOf(
                    AppBarItem(
                        icon = Icons.Default.BarChart,
                        onClick = { onEvent(HomeUiEvent.NavigateToStats) },
                    ),
                    AppBarItem(
                        icon = Icons.Default.AddCircle,
                        onClick = { onEvent(HomeUiEvent.AddHabit) },
                    ),
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {

        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState = remember { HomeUiState() }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}
