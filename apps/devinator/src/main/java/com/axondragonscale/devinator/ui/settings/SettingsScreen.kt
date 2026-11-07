package com.axondragonscale.devinator.ui.settings

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.UIMode
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.ui.Route
import com.axondragonscale.devinator.ui.components.DevinatorSwitch
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

/** The segmented control reads System / Light / Dark, which is what Android itself calls Auto. */
private val SEGMENT_ORDER = listOf(UIMode.AUTO, UIMode.LIGHT, UIMode.DARK)

private val UIMode.label: String
    get() = when (this) {
        UIMode.AUTO -> "System"
        UIMode.LIGHT -> "Light"
        UIMode.DARK -> "Dark"
    }

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: SettingsVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The grant can be revoked outside the app, so it is re-read every time the screen returns.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SettingsUiEvent.OnResume)
    }

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onOpenPermission = { navController.navigate(Route.Permission) },
        onOpenDeveloperOptions = {
            context.startActivity(SecureSettingsPermission.developmentSettingsIntent())
        },
        onEvent = { event -> viewModel.onEvent(event) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onOpenPermission: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
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
            SettingsCard {
                SettingsRow(
                    title = "Appearance",
                    trailing = {
                        UiModeSegmentedControl(
                            selected = uiState.uiMode,
                            onSelect = { mode -> onEvent(SettingsUiEvent.SetUiMode(mode)) },
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    title = "Dynamic colour",
                    trailing = {
                        DevinatorSwitch(
                            checked = uiState.dynamicColor,
                            onCheckedChange = { enabled ->
                                onEvent(SettingsUiEvent.SetDynamicColor(enabled))
                            },
                        )
                    },
                )
            }

            SettingsCard {
                SettingsRow(
                    title = "Write secure settings",
                    trailing = { GrantChip(granted = uiState.permissionGranted) },
                )
                SettingsDivider()
                SettingsRow(
                    title = "Grant or revoke",
                    onClick = onOpenPermission,
                    trailing = {
                        TrailingIcon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight)
                    },
                )
            }

            SettingsCard {
                SettingsRow(
                    title = "Open Developer options",
                    onClick = onOpenDeveloperOptions,
                    trailing = {
                        TrailingIcon(imageVector = Icons.AutoMirrored.Filled.OpenInNew)
                    },
                )
            }

            SettingsCard {
                SettingsRow(
                    title = "Devinator",
                    trailing = {
                        Text(
                            text = "Version ${uiState.versionName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(content = { content() })
    }
}

/** Full-bleed like the tiles page: the ripple spans the card, the padding only insets the content. */
@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        trailing()
    }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    )
}

@Composable
private fun TrailingIcon(modifier: Modifier = Modifier, imageVector: ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun UiModeSegmentedControl(
    modifier: Modifier = Modifier,
    selected: UIMode,
    onSelect: (UIMode) -> Unit,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SEGMENT_ORDER.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable(role = Role.RadioButton) { onSelect(mode) }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/** A status, not a control: the chip is what keeps this row from reading as tappable. */
@Composable
private fun GrantChip(modifier: Modifier = Modifier, granted: Boolean) {
    val contentColor = if (granted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (granted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    LocalDevinatorColors.current.errorSurfaceHigh
                },
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = contentColor, shape = CircleShape),
        )
        Text(
            text = if (granted) "Granted" else "Missing",
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(versionName = "1.0.0"),
            onBack = {},
            onOpenPermission = {},
            onOpenDeveloperOptions = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenLightPreview() {
    DevinatorTheme(darkTheme = false, dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(versionName = "1.0.0"),
            onBack = {},
            onOpenPermission = {},
            onOpenDeveloperOptions = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenNotGrantedPreview() {
    DevinatorTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(permissionGranted = false, versionName = "1.0.0"),
            onBack = {},
            onOpenPermission = {},
            onOpenDeveloperOptions = {},
            onEvent = {},
        )
    }
}
