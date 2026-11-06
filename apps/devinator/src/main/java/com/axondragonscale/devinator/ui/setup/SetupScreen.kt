package com.axondragonscale.devinator.ui.setup

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */
@Composable
fun SetupScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: SetupVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SetupScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event -> viewModel.onEvent(event) },
    )
}

@Composable
private fun SetupScreen(
    modifier: Modifier = Modifier,
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "One-time setup",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetupScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        SetupScreen(uiState = SetupUiState(), onEvent = {})
    }
}
