package com.axondragonscale.devinator.ui.tiles

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Icon as AndroidIcon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.log
import com.axondragonscale.devinator.R
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import java.util.function.Consumer
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

// Kept as strings so preview rendering never has to load the platform TileService class.
private const val DEVELOPER_OPTIONS_TILE = "com.axondragonscale.devinator.tile.DeveloperOptionsTileService"
private const val USB_DEBUGGING_TILE = "com.axondragonscale.devinator.tile.UsbDebuggingTileService"
private const val WIRELESS_DEBUGGING_TILE = "com.axondragonscale.devinator.tile.WirelessDebuggingTileService"

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickSettingsTilesScreen(
    modifier: Modifier = Modifier,
    uiState: QuickSettingsTilesUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onEvent: (QuickSettingsTilesUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val specs = remember(context) { devTileSpecs(context) }
    val scope = rememberCoroutineScope()

    fun showError(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun add(spec: DevTileSpec) {
        // The platform's own prompt places the tile where the system wants it.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onEvent(QuickSettingsTilesUiEvent.Add(spec.component))
        } else {
            requestAddTile(context, spec, ::showError)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Quick Settings Tiles", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    specs.forEachIndexed { index, spec ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            )
                        }
                        TileRow(
                            spec = spec,
                            added = spec.component.className in uiState.addedClassNames,
                            onClick = {
                                if (spec.component.className in uiState.addedClassNames) {
                                    onEvent(QuickSettingsTilesUiEvent.Remove(spec.component))
                                } else {
                                    add(spec)
                                }
                            },
                        )
                    }
                }
            }
            Text(
                text = "New tiles land at the end — rearrange them in the Quick Settings editor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddTile(
    context: Context,
    spec: DevTileSpec,
    onError: (String) -> Unit,
) {
    val manager = context.getSystemService(StatusBarManager::class.java)
    if (manager == null) {
        onError("Quick Settings tiles aren't available on this device.")
        return
    }
    try {
        manager.requestAddTileService(
            spec.component,
            spec.title,
            AndroidIcon.createWithResource(context, spec.iconRes),
            context.mainExecutor,
            Consumer { result ->
                when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED,
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED,
                    -> Unit
                    // Some builds report this while the tile list stays unreadable to us.
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                        onError("Already in Quick Settings.")

                    else -> onError("Couldn't add this tile.")
                }
            },
        )
    } catch (e: Exception) {
        context.log { e }
        onError("Couldn't add this tile.")
    }
}

@Composable
private fun TileRow(
    modifier: Modifier = Modifier,
    spec: DevTileSpec,
    added: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            // Tap and ripple own the whole row, padding included; the insets only place the content.
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = spec.description,
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

private data class DevTileSpec(
    val component: ComponentName,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconRes: Int,
)

private fun devTileSpecs(context: Context): List<DevTileSpec> = listOf(
    DevTileSpec(
        component = ComponentName(context.packageName, DEVELOPER_OPTIONS_TILE),
        title = "Developer options",
        description = "Master switch for ADB",
        icon = Icons.Filled.DeveloperMode,
        iconRes = R.drawable.ic_tile_developer_mode,
    ),
    DevTileSpec(
        component = ComponentName(context.packageName, USB_DEBUGGING_TILE),
        title = "USB debugging",
        description = "ADB over USB",
        icon = Icons.Filled.Usb,
        iconRes = R.drawable.ic_tile_usb,
    ),
    DevTileSpec(
        component = ComponentName(context.packageName, WIRELESS_DEBUGGING_TILE),
        title = "Wireless debugging",
        description = "ADB over Wi-Fi or hotspot",
        icon = Icons.Filled.WifiTethering,
        iconRes = R.drawable.ic_tile_wireless,
    ),
)

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
                addedClassNames = setOf(DEVELOPER_OPTIONS_TILE),
            ),
            onBack = {},
            onEvent = {},
        )
    }
}
