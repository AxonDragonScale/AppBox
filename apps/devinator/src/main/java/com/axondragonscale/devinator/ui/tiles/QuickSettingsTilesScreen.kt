package com.axondragonscale.devinator.ui.tiles

import android.app.StatusBarManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Icon as AndroidIcon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.catching
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.onFailure
import com.axondragonscale.devinator.tile.DevTile
import com.axondragonscale.devinator.ui.components.BackTopBar
import com.axondragonscale.devinator.ui.components.DevinatorCard
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.DevinatorSnackbarHost
import com.axondragonscale.devinator.ui.components.IconBadge
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import java.util.function.Consumer
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

@Composable
fun QuickSettingsTilesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: QuickSettingsTilesVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.effect.collectWithLifecycle { effect ->
        when (effect) {
            is QuickSettingsTilesUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    QuickSettingsTilesScreen(
        modifier = modifier,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = { navController.popBackStack() },
        onEvent = { event -> viewModel.onEvent(event) },
    )
}

@Composable
private fun QuickSettingsTilesScreen(
    modifier: Modifier = Modifier,
    uiState: QuickSettingsTilesUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onEvent: (QuickSettingsTilesUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun showError(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun add(tile: DevTile) {
        // Before API 33 there is no public add API; the system editor is the only path.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showError("Open Quick Settings and add the tile from the edit screen.")
        } else {
            requestAddTile(context, tile, ::showError)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { DevinatorSnackbarHost(snackbarHostState) },
        topBar = { BackTopBar(title = "Quick Settings Tiles", onBack = onBack) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Choose which Devinator tiles appear in the shade.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DevinatorCard {
                DevTile.entries.forEachIndexed { index, tile ->
                    if (index > 0) DevinatorDivider()
                    TileRow(
                        tile = tile,
                        description = tile.description(uiState.timedToggleDuration),
                        added = tile.className in uiState.addedClassNames,
                        onClick = {
                            if (tile.className in uiState.addedClassNames) {
                                onEvent(QuickSettingsTilesUiEvent.Remove(tile.component(context)))
                            } else {
                                add(tile)
                            }
                        },
                    )
                }
            }
            Text(
                text = "New tiles land at the end. Drag them where you want in Quick Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddTile(
    context: Context,
    tile: DevTile,
    onError: (String) -> Unit,
) {
    val manager = context.getSystemService(StatusBarManager::class.java)
    if (manager == null) {
        onError("Quick Settings tiles aren't available on this device.")
        return
    }
    "QuickSettingsTiles".catching {
        manager.requestAddTileService(
            tile.component(context),
            tile.title(context),
            AndroidIcon.createWithResource(context, tile.iconRes),
            context.mainExecutor,
            Consumer { result ->
                when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> Unit
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                        onError("Already in Quick Settings.")

                    else -> onError("Couldn't add this tile.")
                }
            },
        )
    }.onFailure {
        onError("Couldn't add this tile.")
    }
}

@Composable
private fun TileRow(
    modifier: Modifier = Modifier,
    tile: DevTile,
    description: String,
    added: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            // Tap and ripple own the whole row, padding included; the insets only place the content.
            .clickable(
                role = Role.Button,
                onClickLabel = if (added) "Remove tile" else "Add tile",
                onClick = onClick,
            )
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(painter = painterResource(tile.iconRes))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = tile.title(context),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (added) "Remove" else "Add",
            style = MaterialTheme.typography.titleSmall,
            color = if (added) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuickSettingsTilesScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        QuickSettingsTilesScreen(
            uiState = QuickSettingsTilesUiState(),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuickSettingsTilesScreenAddedPreview() {
    DevinatorTheme(dynamicColor = false) {
        QuickSettingsTilesScreen(
            uiState = QuickSettingsTilesUiState(
                addedClassNames = setOf(DevTile.DEVELOPER_OPTIONS.className),
            ),
            onBack = {},
            onEvent = {},
        )
    }
}
