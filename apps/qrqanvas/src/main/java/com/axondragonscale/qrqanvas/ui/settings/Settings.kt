package com.axondragonscale.qrqanvas.ui.settings

import com.axondragonscale.commons.icon

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.UIMode
import com.axondragonscale.qrqanvas.BuildConfig
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
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
        },
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            AppearanceSection(
                modifier = Modifier.padding(top = 8.dp),
                uiMode = uiState.uiMode,
                dynamicTheme = uiState.dynamicTheme,
                onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
            )

            AboutSection(
                modifier = Modifier.padding(top = 24.dp),
            )

            DevToolsSection(
                modifier = Modifier.padding(top = 24.dp),
                onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
            )

            Spacer(modifier = Modifier.height(32.dp))
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
        SectionHeader(text = "Appearance")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                            else LocalContentColor.current,
                                        )
                                    },
                                    icon = { /* Don't show tick */ },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = UIMode.entries.size,
                                    ),
                                    colors = SegmentedButtonDefaults.colors(),
                                )
                            }
                        }
                    },
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
                    },
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
        SectionHeader(text = "About")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Android,
                            contentDescription = "App",
                        )
                    },
                    headlineContent = { Text(text = "QRQanvas") },
                    supportingContent = { Text(text = "Version ${BuildConfig.VERSION_NAME}") },
                )

                SettingsCard(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Developer",
                        )
                    },
                    headlineContent = { Text(text = "Developer") },
                    supportingContent = { Text(text = "Ronak Harkhani") },
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
        SectionHeader(text = "Developer")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            SettingsCard(
                onClick = onOpenDevTools,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = "Dev Tools",
                    )
                },
                headlineContent = { Text(text = "Dev Tools") },
                supportingContent = { Text(text = "Debug utilities and settings") },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier.padding(bottom = 12.dp, start = 4.dp),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
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
        supportingContent = supportingContent?.let {
            {
                val style = MaterialTheme.typography.labelSmall
                CompositionLocalProvider(LocalTextStyle provides style) {
                    it()
                }
            }
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsPreview() {
    QRQanvasTheme {
        androidx.compose.material3.Surface {
            val uiState by remember { mutableStateOf(SettingsUiState()) }
            Settings(
                uiState = uiState,
                onEvent = {},
                onNavigateBack = {},
            )
        }
    }
}
