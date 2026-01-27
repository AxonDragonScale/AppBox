package com.axondragonscale.wallmatic.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.axondragonscale.wallmatic.BuildConfig
import com.axondragonscale.wallmatic.util.Link
import com.axondragonscale.wallmatic.R
import com.axondragonscale.wallmatic.model.UIMode
import com.axondragonscale.wallmatic.ui.bottombar.BOTTOM_BAR_HEIGHT
import com.axondragonscale.wallmatic.ui.common.SettingsCard
import com.axondragonscale.wallmatic.ui.common.TabHeader
import com.axondragonscale.wallmatic.ui.common.WallmaticCard
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Settings(modifier: Modifier = Modifier) {
    val vm: SettingsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Settings(
        modifier = modifier,
        uiState = uiState,
        onEvent = { vm.onEvent(it) }
    )
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        TabHeader(
            modifier = Modifier.padding(start = 8.dp, top = 48.dp, bottom = 16.dp),
            text = "Settings"
        )

        ThemeCard(
            uiMode = uiState.uiMode,
            dynamicTheme = uiState.dynamicTheme,
            onUiModeUpdate = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
            onDynamicThemeToggle = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) }
        )

        PreferencesCard(
            modifier = Modifier.padding(top = 16.dp),
            gridSize = uiState.gridSize,
            onGridSizeChanged = { onEvent(SettingsUiEvent.GridSizedUpdated(it)) }
        )

        DevToolsCard(
            modifier = Modifier.padding(top = 16.dp),
            fastAutoCycle = uiState.fastAutoCycle,
            onClearData = { onEvent(SettingsUiEvent.ClearData) },
            onFastAutoCycleToggled = { onEvent(SettingsUiEvent.FastAutoCycleToggled(it)) },
            onSyncAlbums = { onEvent(SettingsUiEvent.SyncAlbums) }
        )

        AboutCard(
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Spacer(modifier = Modifier.height(BOTTOM_BAR_HEIGHT))
    }
}

@Composable
private fun ThemeCard(
    modifier: Modifier = Modifier,
    uiMode: UIMode,
    dynamicTheme: Boolean,
    onUiModeUpdate: (UIMode) -> Unit,
    onDynamicThemeToggle: (Boolean) -> Unit,
) = WallmaticCard(modifier = modifier, title = "Appearance") {

    DarkModeCard(
        modifier = Modifier.padding(bottom = 8.dp),
        uiMode = uiMode,
        onUiModeUpdate = onUiModeUpdate,
    )

    DynamicThemeCard(
        modifier = Modifier,
        dynamicTheme = dynamicTheme,
        onDynamicThemeToggle = onDynamicThemeToggle,
    )
}

private fun UIMode.getIcon(isActive: Boolean) = when (this) {
    UIMode.LIGHT -> if (isActive) Icons.Filled.LightMode else Icons.Outlined.LightMode
    UIMode.DARK -> if (isActive) Icons.Filled.DarkMode else Icons.Outlined.DarkMode
    UIMode.AUTO -> if (isActive) Icons.Filled.BrightnessAuto else Icons.Outlined.BrightnessAuto
}

@Composable
private fun DarkModeCard(
    modifier: Modifier = Modifier,
    uiMode: UIMode,
    onUiModeUpdate: (UIMode) -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Brightness6,
                contentDescription = "Dark Mode",
            )
        },
        headlineContent = {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Easy on the eyes",
                style = MaterialTheme.typography.labelSmall
            )
        },
        trailingContent = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.size(width = 128.dp, height = 32.dp),
            ) {
                UIMode.entries.forEach { mode ->
                    val isActive = mode == uiMode
                    SegmentedButton(
                        selected = isActive,
                        onClick = { onUiModeUpdate(mode) },
                        label = {
                            Icon(
                                imageVector = mode.getIcon(isActive),
                                contentDescription = "",
                                tint = if (isActive) MaterialTheme.colorScheme.primary
                                else LocalContentColor.current
                            )
                        },
                        icon = { /* Don't show tick */ },
                        shape = SegmentedButtonDefaults.itemShape(
                            mode.ordinal,
                            UIMode.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors()
                    )
                }
            }
        },
    )
}

@Composable
private fun DynamicThemeCard(
    modifier: Modifier = Modifier,
    dynamicTheme: Boolean,
    onDynamicThemeToggle: (Boolean) -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = "Dynamic Theme",
            )
        },
        headlineContent = {
            Text(
                text = "Dynamic Theme",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Use Material You",
                style = MaterialTheme.typography.labelSmall
            )
        },
        trailingContent = {
            Switch(
                checked = dynamicTheme,
                onCheckedChange = onDynamicThemeToggle,
            )
        }
    )
}

@Composable
private fun PreferencesCard(
    modifier: Modifier = Modifier,
    gridSize: Int,
    onGridSizeChanged: (Int) -> Unit,
) = WallmaticCard(modifier = modifier, title = "Preferences") {
    GridSizeCard(
        modifier = Modifier,
        gridSize = gridSize,
        onGridSizeChanged = onGridSizeChanged
    )
}

private val GridSizes = listOf(2, 3, 4)

@Composable
private fun GridSizeCard(
    modifier: Modifier = Modifier,
    gridSize: Int,
    onGridSizeChanged: (Int) -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.GridView,
                contentDescription = "Grid",
            )
        },
        headlineContent = {
            Text(
                text = "Grid Size",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Columns in the Grid",
                style = MaterialTheme.typography.labelSmall
            )
        },
        trailingContent = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.size(width = 128.dp, height = 32.dp),
            ) {
                GridSizes.forEachIndexed { index, size ->
                    val isActive = size == gridSize
                    SegmentedButton(
                        selected = isActive,
                        onClick = { onGridSizeChanged(size) },
                        label = {
                            Text(
                                modifier = Modifier.wrapContentHeight(unbounded = true),
                                text = size.toString(),
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.primary
                                else LocalContentColor.current
                            )
                        },
                        icon = { /* Don't show tick */ },
                        shape = SegmentedButtonDefaults.itemShape(index, GridSizes.size),
                        colors = SegmentedButtonDefaults.colors()
                    )
                }
            }
        },
    )
}

@Composable
private fun DevToolsCard(
    modifier: Modifier = Modifier,
    fastAutoCycle: Boolean,
    onClearData: () -> Unit,
    onFastAutoCycleToggled: (Boolean) -> Unit,
    onSyncAlbums: () -> Unit,
) = WallmaticCard(modifier = modifier, title = "Dev Tools") {

    ClearDataCard(
        modifier = Modifier.padding(bottom = 8.dp),
        onClick = onClearData,
    )

    FastAutoCycleCard(
        modifier = Modifier.padding(bottom = 8.dp),
        fastAutoCycle = fastAutoCycle,
        onToggled = onFastAutoCycleToggled,
    )

    SyncAlbums(
        onClick = onSyncAlbums,
    )
}

@Composable
private fun ClearDataCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = "Clear Data",
            )
        },
        headlineContent = {
            Text(
                text = "Clear Data",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    )
}

@Composable
private fun FastAutoCycleCard(
    modifier: Modifier = Modifier,
    fastAutoCycle: Boolean,
    onToggled: (Boolean) -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = "Fast Autocycle",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Use one minute Autocycle interval",
                style = MaterialTheme.typography.labelSmall
            )
        },
        trailingContent = {
            Switch(
                checked = fastAutoCycle,
                onCheckedChange = onToggled,
            )
        }
    )
}

@Composable
private fun SyncAlbums(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Sync,
                contentDescription = "Sync Albums",
            )
        },
        headlineContent = {
            Text(
                text = "Sync Albums",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Sync Albums with Disk Storage",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    )
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier,
) = WallmaticCard(modifier = modifier, title = "About") {
    val uriHandler = LocalUriHandler.current

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Android,
                contentDescription = "Android",
            )
        },
        headlineContent = {
            Text(
                text = "Wallmatic",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Author",
            )
        },
        headlineContent = {
            Text(
                text = "Developer",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "RONAK HARKHANI",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        onClick = { uriHandler.openUri(Link.AXON_DRAGON_SCALE) },
        leadingContent = {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_github),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        },
        headlineContent = {
            Text(
                text = "Github",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = "AxonDragonScale",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    )

    SettingsCard(
        modifier = Modifier,
        onClick = { uriHandler.openUri(Link.WALLMATIC) },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Source Code",
            )
        },
        headlineContent = {
            Text(
                text = "Source Code",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember { SettingsUiState() }
            Settings(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}
