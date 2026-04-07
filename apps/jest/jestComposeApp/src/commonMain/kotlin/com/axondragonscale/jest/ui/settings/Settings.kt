package com.axondragonscale.jest.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import com.axondragonscale.jest.ui.util.WindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.icon
import com.axondragonscale.jest.resources.Res
import com.axondragonscale.jest.resources.ic_github
import com.axondragonscale.jest.ui.theme.DarkSecondary
import com.axondragonscale.jest.ui.theme.DarkTextTertiary
import com.axondragonscale.jest.ui.theme.JestTheme
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private const val LINK_GITHUB = "https://github.com/AxonDragonScale"
private const val LINK_JEST = "https://github.com/AxonDragonScale/Jest"

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    windowSize: WindowSize = WindowSize.Compact,
    onNavigateToDevTools: () -> Unit,
) {
    val vm: SettingsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Settings(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onEvent = { event ->
            when (event) {
                SettingsUiEvent.NavigateToDevTools -> onNavigateToDevTools()
                else -> vm.onEvent(event)
            }
        },
    )
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    windowSize: WindowSize,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val isCompact = windowSize == WindowSize.Compact
    val isExpanded = windowSize == WindowSize.Expanded
    val horizontalPadding = when (windowSize) {
        WindowSize.Compact -> 20.dp
        WindowSize.Medium -> 24.dp
        WindowSize.Expanded -> 32.dp
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .then(if (isCompact) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = horizontalPadding)
            .padding(top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Screen title (mobile only — tablet/desktop use top bar)
        if (isCompact) {
            SettingsTitle()
        }

        when {
            isExpanded -> {
                // Desktop: 3-column layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    AppearanceSection(
                        modifier = Modifier.weight(1f),
                        uiMode = uiState.uiMode,
                        dynamicTheme = uiState.dynamicTheme,
                        onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                        onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
                    )
                    AboutSection(modifier = Modifier.weight(1f))
                    DevToolsSection(
                        modifier = Modifier.weight(1f),
                        onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
                    )
                }
            }

            !isCompact -> {
                // Tablet: 2-column layout (Appearance left, About + DevTools right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    AppearanceSection(
                        modifier = Modifier.weight(1f),
                        uiMode = uiState.uiMode,
                        dynamicTheme = uiState.dynamicTheme,
                        onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                        onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        AboutSection()
                        DevToolsSection(
                            onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
                        )
                    }
                }
            }

            else -> {
                // Mobile: single column stacked
                AppearanceSection(
                    uiMode = uiState.uiMode,
                    dynamicTheme = uiState.dynamicTheme,
                    onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                    onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
                )
                AboutSection()
                DevToolsSection(
                    onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
                )
            }
        }
    }
}

@Composable
private fun SettingsTitle(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Customize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionLabel(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier.padding(bottom = 8.dp, start = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = DarkTextTertiary,
        letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp),
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

@Composable
private fun AppearanceSection(
    modifier: Modifier = Modifier,
    uiMode: UIMode,
    dynamicTheme: Boolean,
    onUiModeUpdated: (UIMode) -> Unit,
    onDynamicThemeToggled: (Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        SectionLabel(text = "APPEARANCE")
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Dark Mode",
                description = "Easy on the eyes",
                checked = uiMode == UIMode.DARK,
                onCheckedChange = {
                    onUiModeUpdated(if (it) UIMode.DARK else UIMode.LIGHT)
                },
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "UI Mode",
                description = uiMode.displayName,
                showChevron = false,
                trailingContent = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.size(width = 120.dp, height = 32.dp),
                    ) {
                        UIMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = mode == uiMode,
                                onClick = { onUiModeUpdated(mode) },
                                label = {
                                    androidx.compose.material3.Icon(
                                        imageVector = mode.icon,
                                        contentDescription = mode.displayName,
                                    )
                                },
                                icon = { /* no tick */ },
                                shape = SegmentedButtonDefaults.itemShape(index, UIMode.entries.size),
                            )
                        }
                    }
                },
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Default.Palette,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Dynamic Theme",
                description = "Use Material You",
                checked = dynamicTheme,
                onCheckedChange = onDynamicThemeToggled,
            )
        }
    }
}

@Composable
private fun AboutSection(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier) {
        SectionLabel(text = "ABOUT")
        SettingsCard {
            SettingsLinkRow(
                icon = Icons.Default.Android,
                iconTint = DarkSecondary,
                title = "Jest",
                description = "Version ${AppInfo.versionName}",
                showChevron = false,
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.Default.Person,
                iconTint = DarkSecondary,
                title = "Developer",
                description = "Ronak Harkhani",
                showChevron = false,
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.Default.Info,
                iconTint = DarkSecondary,
                title = "Github",
                description = "AxonDragonScale",
                trailingContent = {
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(Res.drawable.ic_github),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                },
                onClick = { uriHandler.openUri(LINK_GITHUB) },
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                iconTint = DarkSecondary,
                title = "Source Code",
                description = "View on GitHub",
                onClick = { uriHandler.openUri(LINK_JEST) },
            )
        }
    }
}

@Composable
private fun DevToolsSection(
    modifier: Modifier = Modifier,
    onOpenDevTools: () -> Unit,
) {
    Column(modifier = modifier) {
        SectionLabel(text = "DEV TOOLS")
        SettingsCard {
            SettingsLinkRow(
                icon = Icons.Default.BugReport,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Open Dev Tools",
                description = "Debug utilities and settings",
                onClick = onOpenDevTools,
            )
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    JestTheme(darkTheme = true) {
        val uiState by remember { mutableStateOf(SettingsUiState()) }
        Settings(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            uiState = uiState,
            windowSize = WindowSize.Compact,
            onEvent = {},
        )
    }
}
