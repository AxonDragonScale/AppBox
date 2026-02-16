package com.axondragonscale.jest.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.jest.BuildConfig
import com.axondragonscale.jest.R
import com.axondragonscale.commons.UIMode
import com.axondragonscale.jest.ui.theme.JestTheme
import org.koin.androidx.compose.koinViewModel

private const val LINK_GITHUB = "https://github.com/AxonDragonScale"
private const val LINK_JEST = "https://github.com/AxonDragonScale/Jest"

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    onNavigateToDevTools: () -> Unit,
) {
    val vm: SettingsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Settings(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                SettingsUiEvent.NavigateToDevTools -> onNavigateToDevTools()
                else -> vm.onEvent(event)
            }
        }
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
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        SettingsHeader(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp)
        )

        AppearanceSection(
            modifier = Modifier.padding(top = 24.dp),
            uiMode = uiState.uiMode,
            dynamicTheme = uiState.dynamicTheme,
            onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
            onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
        )

        AboutSection(
            modifier = Modifier.padding(top = 16.dp),
        )

        DevToolsSection(
            modifier = Modifier.padding(top = 16.dp),
            onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsHeader(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Customize your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Brightness6,
                            contentDescription = "UI Mode",
                        )
                    },
                    headlineContent = { Text(text = "Dark Mode") },
                    supportingContent = { Text(text = "Easy on the eyes") },
                    trailingContent = {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.size(width = 140.dp, height = 32.dp),
                        ) {
                            UIMode.entries.forEachIndexed { index, mode ->
                                val isActive = mode == uiMode
                                SegmentedButton(
                                    selected = isActive,
                                    onClick = { onUiModeUpdated(mode) },
                                    label = {
                                        Icon(
                                            imageVector = mode.icon,
                                            contentDescription = mode.displayName,
                                            tint = if (isActive) MaterialTheme.colorScheme.primary
                                            else LocalContentColor.current
                                        )
                                    },
                                    icon = { /* Don't show tick */ },
                                    shape = SegmentedButtonDefaults.itemShape(index, UIMode.entries.size),
                                    colors = SegmentedButtonDefaults.colors()
                                )
                            }
                        }
                    }
                            )

                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = "Dynamic Theme",
                        )
                    },
                    headlineContent = { Text(text = "Dynamic Theme") },
                    supportingContent = { Text(text = "Use Material You") },
                    trailingContent = {
                        Switch(
                            checked = dynamicTheme,
                            onCheckedChange = onDynamicThemeToggled,
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                val uriHandler = LocalUriHandler.current

                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Android,
                            contentDescription = "Android",
                        )
                    },
                    headlineContent = { Text(text = "Jest") },
                    supportingContent = { Text(text = "Version ${BuildConfig.VERSION_NAME}") }
                )

                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Author",
                        )
                    },
                    headlineContent = { Text(text = "Developer") },
                    supportingContent = { Text(text = "Ronak Harkhani") }
                )

                SettingsCard(
                    onClick = { uriHandler.openUri(LINK_GITHUB) },
                    leadingContent = {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    },
                    headlineContent = { Text(text = "Github") },
                    supportingContent = { Text(text = "AxonDragonScale") },
                )


                SettingsCard(
                    onClick = { uriHandler.openUri(LINK_JEST) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Source Code",
                        )
                    },
                    headlineContent = { Text(text = "Source Code") },
                )
            }
        }
    }
}

@Composable
private fun DevToolsSection(
    modifier: Modifier = Modifier,
    onOpenDevTools: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            text = "Dev Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            SettingsCard(
                onClick = onOpenDevTools,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Android,
                        contentDescription = "Dev Tools",
                    )
                },
                headlineContent = { Text(text = "Open Dev Tools") },
                supportingContent = { Text(text = "Debug utilities and settings") },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        leadingContent = leadingContent,
        headlineContent = {
            val style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            CompositionLocalProvider(LocalTextStyle provides style) {
                headlineContent()
            }
        },
        supportingContent = {
            val style = MaterialTheme.typography.labelSmall
            CompositionLocalProvider(LocalTextStyle provides style) {
                supportingContent?.invoke()
            }
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    JestTheme {
        val uiState by remember { mutableStateOf(SettingsUiState()) }
        Settings(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            uiState = uiState,
            onEvent = { },
        )
    }
}
