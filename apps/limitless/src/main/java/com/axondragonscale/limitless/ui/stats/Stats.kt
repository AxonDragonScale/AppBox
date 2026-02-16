package com.axondragonscale.limitless.ui.stats

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 03/11/24
 */

@Composable
fun Stats(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: StatsVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    Stats(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = vm::onEvent
    )
}

@Composable
private fun Stats(
    modifier: Modifier = Modifier,
    uiState: StatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
) {

}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(StatsUiState()) }
        Stats(
            uiState = uiState,
            onEvent = { },
        )
    }
}
