package com.axondragonscale.topty.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.icon
import com.axondragonscale.commons.compose.Dropdown
import com.axondragonscale.commons.compose.DropdownItem
import com.axondragonscale.topty.BuildConfig
import com.axondragonscale.topty.Link
import com.axondragonscale.topty.R
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.common.Switch
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import com.axondragonscale.topty.ui.util.thenIf
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 13/10/24
 */

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
                SettingsUiEvent.NavigateBack -> navController.popBackStack()

                SettingsUiEvent.NavigateToDevTools -> navController.navigate(Route.DevTools)

                SettingsUiEvent.NavigateToBackupRestore -> navController.navigate(Route.BackupRestore)

                SettingsUiEvent.NavigateToChangePassword -> navController.navigate(Route.ChangePassword)

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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AppearanceSection(
                uiMode = uiState.uiMode,
                dynamicTheme = uiState.dynamicTheme,
                uiFont = uiState.uiFont,
                codeDesign = uiState.codeDesign,
                onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) },
                onUiFontUpdated = { onEvent(SettingsUiEvent.UIFontUpdated(it)) },
                onCodeDesignUpdated = { onEvent(SettingsUiEvent.CodeDesignUpdated(it)) },
            )

            BehaviorSection(
                modifier = Modifier.padding(top = 24.dp),
                speakOutOnTap = uiState.speakOutOnTap,
                onSpeakOutOnTapToggled = { onEvent(SettingsUiEvent.SpeakOutOnTapToggled(it)) },
            )

            SecuritySection(
                modifier = Modifier.padding(top = 24.dp),
                biometricLock = uiState.biometricLock,
                biometricUnavailableReason = uiState.biometricUnavailableReason,
                preventScreenshots = uiState.preventScreenshots,
                onBiometricLockToggled = { onEvent(SettingsUiEvent.BiometricLockToggled(it)) },
                onPreventScreenshotsToggled = { onEvent(SettingsUiEvent.PreventScreenshotsToggled(it)) },
                onChangePasswordClicked = { onEvent(SettingsUiEvent.NavigateToChangePassword) },
            )

            DataSection(
                modifier = Modifier.padding(top = 24.dp),
                onOpenBackupRestore = { onEvent(SettingsUiEvent.NavigateToBackupRestore) }
            )

            AboutSection(
                modifier = Modifier.padding(top = 24.dp),
            )

            DevToolsSection(
                modifier = Modifier.padding(top = 24.dp),
                onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AppearanceSection(
    modifier: Modifier = Modifier,
    uiMode: UIMode,
    dynamicTheme: Boolean,
    uiFont: UIFont,
    codeDesign: CodeDesign,
    onUiModeUpdated: (UIMode) -> Unit,
    onDynamicThemeToggled: (Boolean) -> Unit,
    onUiFontUpdated: (UIFont) -> Unit,
    onCodeDesignUpdated: (CodeDesign) -> Unit,
) = Section(
    modifier = modifier,
    title = "Appearance"
) {
    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Brightness6,
                contentDescription = "UI Mode",
            )
        },
        headlineContent = { Text(text = "UI Mode") },
        supportingContent = { Text(text = "Ease your eyes") },
        trailingContent = {
            Dropdown(
                modifier = Modifier.size(width = 128.dp, height = 32.dp),
                selectedItem = DropdownItem(uiMode.displayName, uiMode),
                items = UIMode.entries.map {
                    DropdownItem(name = it.displayName, value = it, leadingIcon = it.icon)
                },
                onItemSelected = { onUiModeUpdated(it.value) },
                shape = DefaultShape,
            )
        }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
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

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.TextFields,
                contentDescription = "Font",
            )
        },
        headlineContent = { Text(text = "Font") },
        supportingContent = { Text(text = "Set Font to your taste") },
        trailingContent = {
            Dropdown(
                modifier = Modifier.size(width = 128.dp, height = 32.dp),
                selectedItem = DropdownItem(uiFont.displayName, uiFont),
                items = UIFont.entries.map { DropdownItem(name = it.displayName, value = it) },
                onItemSelected = { onUiFontUpdated(it.value) },
                shape = DefaultShape,
            )
        }
    )

    SettingsCard(
        modifier = Modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.DesignServices,
                contentDescription = "Code UI",
            )
        },
        headlineContent = { Text(text = "Code UI") },
        supportingContent = { Text(text = "Make it look cool!") },
        trailingContent = {
            Dropdown(
                modifier = Modifier.size(width = 128.dp, height = 32.dp),
                selectedItem = DropdownItem(codeDesign.name, codeDesign),
                items = CodeDesign.entries.map { DropdownItem(name = it.name, value = it) },
                onItemSelected = { onCodeDesignUpdated(it.value) },
                shape = DefaultShape,
            )
        }
    )
}

@Composable
private fun BehaviorSection(
    modifier: Modifier = Modifier,
    speakOutOnTap: Boolean,
    onSpeakOutOnTapToggled: (Boolean) -> Unit,
) = Section(
    modifier = modifier,
    title = "Behavior",
) {
    SettingsCard(
        modifier = Modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Campaign,
                contentDescription = "Speak Out",
            )
        },
        headlineContent = { Text(text = "Speak Out") },
        supportingContent = { Text(text = "Tap to speak out the code") },
        trailingContent = {
            Switch(
                checked = speakOutOnTap,
                onCheckedChange = onSpeakOutOnTapToggled,
            )
        }
    )
}

@Composable
private fun SecuritySection(
    modifier: Modifier = Modifier,
    biometricLock: Boolean,
    biometricUnavailableReason: String?,
    preventScreenshots: Boolean,
    onBiometricLockToggled: (Boolean) -> Unit,
    onPreventScreenshotsToggled: (Boolean) -> Unit,
    onChangePasswordClicked: () -> Unit,
) = Section(
    modifier = modifier,
    title = "Security",
) {
    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = "Fingerprint",
            )
        },
        headlineContent = { Text(text = "Biometric Lock") },
        supportingContent = {
            Text(
                text = biometricUnavailableReason ?: "Require Fingerprint to open app",
                color = if (biometricUnavailableReason == null) LocalContentColor.current
                        else MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            Switch(
                checked = biometricLock,
                onCheckedChange = onBiometricLockToggled,
            )
        }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp).clickable(onClick = onChangePasswordClicked),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Change Password",
            )
        },
        headlineContent = { Text(text = "Change Password") },
        supportingContent = { Text(text = "Update your master password") },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
            )
        }
    )

    SettingsCard(
        modifier = Modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Prevent Screenshots",
            )
        },
        headlineContent = { Text(text = "Prevent Screenshots") },
        supportingContent = { Text(text = "Block screenshots and screen recording") },
        trailingContent = {
            Switch(
                checked = preventScreenshots,
                onCheckedChange = onPreventScreenshotsToggled,
            )
        }
    )
}

@Composable
private fun DataSection(
    modifier: Modifier = Modifier,
    onOpenBackupRestore: () -> Unit,
) = Section(
    modifier = modifier,
    title = "Data",
) {
    SettingsCard(
        onClick = onOpenBackupRestore,
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Backup,
                contentDescription = "Backup & Restore",
            )
        },
        headlineContent = { Text(text = "Backup & Restore") },
        supportingContent = { Text(text = "Export or import your tokens") },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
            )
        }
    )
}

@Composable
private fun AboutSection(
    modifier: Modifier = Modifier,
) = Section(
    modifier = modifier,
    title = "About",
) {
    val uriHandler = LocalUriHandler.current

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Android,
                contentDescription = "Android",
            )
        },
        headlineContent = { Text(text = "Topty") },
        supportingContent = { Text(text = "v${BuildConfig.VERSION_NAME}") }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Author",
            )
        },
        headlineContent = { Text(text = "Developer") },
        supportingContent = { Text(text = "RONAK HARKHANI") }
    )

    SettingsCard(
        modifier = Modifier.padding(bottom = 8.dp),
        onClick = { uriHandler.openUri(Link.AXON_DRAGON_SCALE) },
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
        modifier = Modifier,
        onClick = { uriHandler.openUri(Link.TOPTY) },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Source Code",
            )
        },
        headlineContent = { Text(text = "Source Code") },
    )
}

@Composable
private fun DevToolsSection(
    modifier: Modifier = Modifier,
    onOpenDevTools: () -> Unit,
) = Section(
    modifier = modifier,
    title = "Dev Tools",
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

@Composable
private fun Section(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        content()
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
            .clip(DefaultShape)
            .thenIf(onClick != null) { clickable(onClick = onClick!!) },
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        val uiState by remember { mutableStateOf(SettingsUiState()) }
        Settings(
            uiState = uiState,
            onEvent = { },
        )
    }
}
