package com.axondragonscale.wallmatic.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.UIMode
import com.axondragonscale.wallmatic.BuildConfig
import com.axondragonscale.wallmatic.R
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.HorizontalRule
import com.axondragonscale.wallmatic.ui.component.Masthead
import com.axondragonscale.wallmatic.ui.component.SettingsRow
import com.axondragonscale.wallmatic.ui.component.SettingsSectionHeader
import com.axondragonscale.wallmatic.ui.component.WallmaticIconSegmented
import com.axondragonscale.wallmatic.ui.component.WallmaticSegmented
import com.axondragonscale.wallmatic.ui.component.WallmaticSwitch
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.util.Link
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/06/24
 */

private val GridSizes = listOf(2, 3, 4)

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: SettingsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Settings(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is SettingsUiEvent.NavigateToDevTools ->
                    navController.navigate(Route.DevTools)

                else -> vm.onEvent(event)
            }
        },
    )
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(title = "Appearance")

        SettingsRow(
            title = "Theme",
            subtitle = "Light, system or dark",
            leadingContent = { SettingsIcon(Icons.Filled.Brightness6) },
            trailing = {
                WallmaticIconSegmented(
                    icons = listOf(
                        Icons.Filled.LightMode,
                        Icons.Filled.BrightnessAuto,
                        Icons.Filled.DarkMode,
                    ),
                    contentDescriptions = UIMode.entries.map { it.displayName },
                    selectedIndex = uiState.uiMode.ordinal,
                    onSelect = { onEvent(SettingsUiEvent.UIModeUpdated(UIMode.entries[it])) },
                )
            },
        )

        HorizontalRule()

        SettingsRow(
            title = "Dynamic Theme",
            subtitle = "Use Material You",
            leadingContent = { SettingsIcon(Icons.Filled.Palette) },
            trailing = {
                WallmaticSwitch(
                    checked = uiState.dynamicTheme,
                    onCheckedChange = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
                )
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionHeader(title = "Preferences")

        SettingsRow(
            title = "Mirror Home Screen",
            subtitle = "Use the same album on Lock",
            leadingContent = { SettingsIcon(Icons.Filled.SyncLock) },
            trailing = {
                WallmaticSwitch(
                    checked = uiState.mirrorHomeForLock,
                    onCheckedChange = {
                        onEvent(SettingsUiEvent.MirrorHomeForLockToggled(it))
                    },
                )
            },
        )

        HorizontalRule()

        SettingsRow(
            title = "Grid Size",
            subtitle = "Columns in the Grid",
            leadingContent = { SettingsIcon(Icons.Filled.GridView) },
            trailing = {
                WallmaticSegmented(
                    options = GridSizes,
                    selected = uiState.gridSize,
                    onSelect = { onEvent(SettingsUiEvent.GridSizedUpdated(it)) },
                    label = { it.toString() },
                )
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionHeader(title = "About")

        SettingsRow(
            title = "Wallmatic",
            subtitle = "v${BuildConfig.VERSION_NAME}",
            compact = true,
            leadingContent = { SettingsIcon(Icons.Filled.Android, size = 22.dp) },
        )

        HorizontalRule()

        SettingsRow(
            title = "Developer",
            subtitle = "Ronak Harkhani",
            compact = true,
            leadingContent = { SettingsIcon(Icons.Filled.Person, size = 22.dp) },
        )

        HorizontalRule()

        SettingsRow(
            title = "GitHub",
            subtitle = "AxonDragonScale",
            compact = true,
            onClick = { uriHandler.openUri(Link.AXON_DRAGON_SCALE) },
            leadingContent = {
                Image(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(R.drawable.ic_github),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.wallmatic.primary),
                )
            },
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.wallmatic.inkMuted,
                    modifier = Modifier.size(20.dp),
                )
            },
        )

        if (BuildConfig.DEBUG) {
            HorizontalRule()

            SettingsRow(
                title = "Dev Tools",
                subtitle = "Debug utilities and settings",
                compact = true,
                onClick = { onEvent(SettingsUiEvent.NavigateToDevTools) },
                leadingContent = { SettingsIcon(Icons.Filled.Android, size = 22.dp) },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.wallmatic.inkMuted,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, size: androidx.compose.ui.unit.Dp = 24.dp) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.wallmatic.primary,
        modifier = Modifier.size(size),
    )
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun SettingsPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Settings")
            Settings(
                uiState = SettingsUiState(),
                onEvent = {},
            )
        }
    }
}
