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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.UIMode
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.ui.Route
import com.axondragonscale.devinator.ui.components.BackTopBar
import com.axondragonscale.devinator.ui.components.DevinatorCard
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.DevinatorSwitch
import com.axondragonscale.devinator.ui.components.StatusChip
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

/** The segmented control reads System / Light / Dark, which is what Android itself calls Auto. */
private val SEGMENT_ORDER = listOf(UIMode.AUTO, UIMode.LIGHT, UIMode.DARK)

private val UIMode.settingsLabel: String
    get() = if (this == UIMode.AUTO) "System" else displayName

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
        topBar = { BackTopBar(title = "Settings", onBack = onBack) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DevinatorCard {
                SettingsRow(
                    title = "Appearance",
                    trailing = {
                        UiModeSegmentedControl(
                            selected = uiState.uiMode,
                            onSelect = { mode -> onEvent(SettingsUiEvent.SetUiMode(mode)) },
                        )
                    },
                )
                DevinatorDivider()
                SettingsRow(
                    title = "Dynamic colour",
                    trailing = {
                        DevinatorSwitch(
                            checked = uiState.dynamicColor,
                            onCheckedChange = { enabled ->
                                onEvent(SettingsUiEvent.SetDynamicColor(enabled))
                            },
                            contentDescription = "Dynamic colour",
                        )
                    },
                )
            }

            DevinatorCard {
                SettingsRow(
                    title = "Write secure settings",
                    trailing = {
                        StatusChip(
                            label = if (uiState.permissionGranted) "Granted" else "Missing",
                            on = uiState.permissionGranted,
                        )
                    },
                )
                DevinatorDivider()
                SettingsRow(
                    title = "Grant or revoke",
                    onClick = onOpenPermission,
                    trailing = {
                        TrailingIcon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight)
                    },
                )
            }

            DevinatorCard {
                SettingsRow(
                    title = "Open Developer options",
                    onClick = onOpenDeveloperOptions,
                    trailing = {
                        TrailingIcon(imageVector = Icons.AutoMirrored.Filled.OpenInNew)
                    },
                )
            }

            DevinatorCard {
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
            .padding(2.dp)
            .selectableGroup(),
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
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(mode) },
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.settingsLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(versionName = "1.0.0", permissionGranted = true),
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
            uiState = SettingsUiState(versionName = "1.0.0", permissionGranted = true),
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
