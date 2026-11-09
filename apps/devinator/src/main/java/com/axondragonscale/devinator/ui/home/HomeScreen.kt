package com.axondragonscale.devinator.ui.home

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.DurationOption
import com.axondragonscale.devinator.model.EndpointKind
import com.axondragonscale.devinator.model.TimedToggleSession
import com.axondragonscale.devinator.model.remainingText
import com.axondragonscale.devinator.permission.LocalNetworkAccess
import com.axondragonscale.devinator.ui.Route
import com.axondragonscale.devinator.ui.components.CodeBlock
import com.axondragonscale.devinator.ui.components.DevinatorCard
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.DevinatorSnackbarHost
import com.axondragonscale.devinator.ui.components.DevinatorSwitch
import com.axondragonscale.devinator.ui.components.DevinatorTopBar
import com.axondragonscale.devinator.ui.components.DurationChip
import com.axondragonscale.devinator.ui.components.IconBadge
import com.axondragonscale.devinator.ui.components.SmallDropdown
import com.axondragonscale.devinator.ui.components.StatusChip
import com.axondragonscale.devinator.ui.components.rememberCopyToClipboard
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors
import com.axondragonscale.devinator.ui.theme.MonoTextStyle
import com.axondragonscale.devinator.ui.theme.SectionTitleTextStyle
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

private const val PREVIEW_HOST = "192.168.1.42"
private const val PREVIEW_PORT = 37129

/** The AutoOffRow's 18 dp icon plus its 12 dp gap: the caption lines up with the row's label. */
private val AUTO_OFF_CAPTION_INSET = 30.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: HomeVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Local-network access is only how the adbd port is discovered, so it is asked for at the moment
    // wireless debugging is switched on. A refusal costs the port, never the toggle.
    val requestLocalNetwork = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onEvent(HomeUiEvent.SetWirelessDebugging(true))
    }

    fun enableWirelessDebugging() {
        if (!LocalNetworkAccess.isRequired || LocalNetworkAccess.isGranted(context)) {
            viewModel.onEvent(HomeUiEvent.SetWirelessDebugging(true))
        } else {
            requestLocalNetwork.launch(LocalNetworkAccess.permission)
        }
    }

    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is HomeUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.OpenSettings -> navController.navigate(Route.Settings)
                HomeUiEvent.OpenTweaks -> navController.navigate(Route.Tweaks)
                HomeUiEvent.OpenTiles -> navController.navigate(Route.QuickSettingsTiles)
                HomeUiEvent.OpenLogcat -> navController.navigate(Route.Logcat)
                is HomeUiEvent.SetWirelessDebugging ->
                    if (event.enabled) enableWirelessDebugging() else viewModel.onEvent(event)

                else -> viewModel.onEvent(event)
            }
        },
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (HomeUiEvent) -> Unit,
) {
    val copy = rememberCopyToClipboard()
    val command = uiState.endpoint?.command
    val address = uiState.endpoint?.let { endpoint ->
        endpoint.port?.let { "${endpoint.host}:$it" } ?: endpoint.host
    }

    fun copyCommand() {
        command?.let { copy("ADB command", it) }
    }

    // The row shows an address, so that is what its copy glyph copies; the command block below
    // copies the command it prints.
    fun copyAddress() {
        address?.let { copy("ADB address", it) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { DevinatorSnackbarHost(snackbarHostState) },
        topBar = {
            DevinatorTopBar(
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = { onEvent(HomeUiEvent.OpenLogcat) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = "Logcat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = { onEvent(HomeUiEvent.OpenSettings) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
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
            DeveloperOptionsCard(
                enabled = uiState.developerOptions,
                onToggle = { onEvent(HomeUiEvent.SetDeveloperOptions(it)) },
            )
            TimedToggleCard(
                session = uiState.timedToggle,
                duration = uiState.timedToggleDuration,
                developerOptions = uiState.developerOptions,
                onStart = { onEvent(HomeUiEvent.StartTimedToggle(it)) },
                onEnd = { onEvent(HomeUiEvent.EndTimedToggle) },
                onSelectDuration = { onEvent(HomeUiEvent.SelectTimedDuration(it)) },
                onExpired = { onEvent(HomeUiEvent.ReconcileDeadlines) },
            )
            UsbDebuggingCard(
                developerOptions = uiState.developerOptions,
                checked = uiState.usbDebugging,
                onToggle = { onEvent(HomeUiEvent.SetUsbDebugging(it)) },
            )
            WirelessDebuggingCard(
                uiState = uiState,
                onToggle = { onEvent(HomeUiEvent.SetWirelessDebugging(it)) },
                onCopyAddress = ::copyAddress,
                onCopyCommand = ::copyCommand,
                onSelectAutoOff = { onEvent(HomeUiEvent.SelectAutoOff(it)) },
                onExpired = { onEvent(HomeUiEvent.ReconcileDeadlines) },
            )
            PagesCard(
                onOpenTweaks = { onEvent(HomeUiEvent.OpenTweaks) },
                onOpenTiles = { onEvent(HomeUiEvent.OpenTiles) },
            )
        }
    }
}

// region Cards

@Composable
private fun DeveloperOptionsCard(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = LocalDevinatorColors.current
    DevinatorCard(
        modifier = modifier,
        color = if (enabled) colors.primarySurface else MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, if (enabled) colors.primaryOutline else colors.errorOutline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = Icons.Filled.DeveloperMode,
                size = 56.dp,
                iconSize = 28.dp,
                cornerRadius = 16.dp,
                containerColor = if (enabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    colors.errorSurfaceHigh
                },
                contentColor = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Developer options",
                    style = SectionTitleTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusChip(label = if (enabled) "On" else "Off", on = enabled)
                    Text(
                        text = "Master switch for ADB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            DevinatorSwitch(
                checked = enabled,
                onCheckedChange = onToggle,
                contentDescription = "Developer options",
                dangerOff = true,
            )
        }
    }
}

@Composable
private fun TimedToggleCard(
    modifier: Modifier = Modifier,
    session: TimedToggleSession?,
    duration: DurationOption,
    developerOptions: Boolean,
    onStart: (Boolean) -> Unit,
    onEnd: () -> Unit,
    onSelectDuration: (DurationOption) -> Unit,
    onExpired: () -> Unit,
) {
    val remaining = rememberRemaining(session?.deadline, onExpired)

    DevinatorCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(icon = Icons.Filled.Timer)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Timed toggle",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (session == null) {
                        Text(
                            text = if (developerOptions) {
                                "Off for ${duration.label}, then back on."
                            } else {
                                "On for ${duration.label}, then back off."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (session.target) {
                                    "Turns back off in"
                                } else {
                                    "Turns back on in"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = remaining ?: "0:00",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                TimedDurationControl(
                    duration = duration,
                    onSelect = onSelectDuration,
                )
            }
            Button(
                onClick = {
                    if (session != null) onEnd() else onStart(!developerOptions)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = when {
                        session != null && session.target -> "Turn off now"
                        session != null -> "Turn on now"
                        developerOptions -> "Pause for ${duration.label}"
                        else -> "Resume for ${duration.label}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
private fun TimedDurationControl(
    modifier: Modifier = Modifier,
    duration: DurationOption,
    onSelect: (DurationOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.DropdownList) { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DurationChip(label = duration.label)
        }
        SmallDropdown(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = DurationOption.timed,
            selected = duration,
            label = { it.label },
            onSelect = onSelect,
        )
    }
}

@Composable
private fun UsbDebuggingCard(
    modifier: Modifier = Modifier,
    developerOptions: Boolean,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    DevinatorCard(modifier = modifier) {
        SwitchRow(
            modifier = Modifier.padding(16.dp),
            icon = Icons.Filled.Usb,
            title = "USB debugging",
            caption = if (developerOptions) "ADB over USB" else "Turn on Developer options first",
            checked = checked,
            enabled = developerOptions,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun WirelessDebuggingCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onToggle: (Boolean) -> Unit,
    onCopyAddress: () -> Unit,
    onCopyCommand: () -> Unit,
    onSelectAutoOff: (DurationOption) -> Unit,
    onExpired: () -> Unit,
) {
    val enabled = uiState.developerOptions
    val pending = uiState.wirelessPending
    val showDetails = enabled && uiState.wirelessDebugging && !pending

    DevinatorCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SwitchRow(
                icon = Icons.Filled.WifiTethering,
                title = "Wireless debugging",
                caption = when {
                    !enabled -> "Turn on Developer options first"
                    pending -> "Confirm the system dialog"
                    else -> "ADB over Wi-Fi or hotspot"
                },
                checked = uiState.wirelessDebugging,
                enabled = enabled,
                onCheckedChange = onToggle,
                trailing = if (pending) {
                    { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                } else {
                    null
                },
            )
            if (showDetails) {
                EndpointBlock(
                    endpoint = uiState.endpoint,
                    onCopyAddress = onCopyAddress,
                    onCopyCommand = onCopyCommand,
                )
                DevinatorDivider(inset = false)
                AutoOffBlock(
                    selected = uiState.autoOffDuration,
                    deadline = uiState.autoOffDeadline,
                    onSelect = onSelectAutoOff,
                    onExpired = onExpired,
                )
            }
        }
    }
}

/**
 * The two pages reachable from Home, in one card: they are both navigations, so they stack rather
 * than take a card each. Rows are the Tiles page's 64 dp row, not Home's 80 dp card.
 */
@Composable
private fun PagesCard(
    modifier: Modifier = Modifier,
    onOpenTweaks: () -> Unit,
    onOpenTiles: () -> Unit,
) {
    DevinatorCard(modifier = modifier) {
        PageRow(
            icon = Icons.Filled.Tune,
            title = "Tweaks",
            caption = "Animation and app behaviour",
            onClick = onOpenTweaks,
        )
        DevinatorDivider()
        PageRow(
            icon = Icons.Filled.GridView,
            title = "Quick Settings Tiles",
            caption = "Choose which tiles appear in the shade.",
            onClick = onOpenTiles,
        )
    }
}

@Composable
private fun PageRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    caption: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// endregion

// region Rows

@Composable
private fun SwitchRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    caption: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .then(
                if (trailing == null) {
                    Modifier.toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Switch,
                        onValueChange = onCheckedChange,
                    )
                } else {
                    Modifier
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            DevinatorSwitch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.clearAndSetSemantics {},
                enabled = true,
            )
        }
    }
}

// endregion

// region Endpoint and auto-off

@Composable
private fun EndpointBlock(
    modifier: Modifier = Modifier,
    endpoint: AdbEndpoint?,
    onCopyAddress: () -> Unit,
    onCopyCommand: () -> Unit,
) {
    if (endpoint == null || endpoint.host.isBlank()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        ) {
            Text(
                text = "No network, so there's no address to show.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val command = endpoint.command
    val address = endpoint.port?.let { "${endpoint.host}:$it" } ?: endpoint.host

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (command != null) {
                    Modifier.clickable(onClickLabel = "Copy address", onClick = onCopyAddress)
                } else {
                    Modifier
                },
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NetworkPill(label = endpoint.kind.label())
                Text(
                    text = "Must be on the same network as this device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = address,
                style = MonoTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (command != null) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (command != null) {
        CodeBlock(command = command, onCopy = onCopyCommand)
    } else {
        Text(
            text = "Port not found. It's on Developer options › Wireless debugging.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NetworkPill(
    modifier: Modifier = Modifier,
    label: String,
) {
    Row(
        modifier = modifier
            .heightIn(min = 20.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.outline)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AutoOffBlock(
    modifier: Modifier = Modifier,
    selected: DurationOption,
    deadline: Instant?,
    onSelect: (DurationOption) -> Unit,
    onExpired: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val remaining = rememberRemaining(deadline, onExpired)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    role = Role.DropdownList,
                    onClickLabel = "Auto-off duration",
                    onClick = { expanded = true },
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Auto-off",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box {
                DurationChip(
                    label = selected.label,
                    off = selected == DurationOption.OFF,
                )
                SmallDropdown(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    options = DurationOption.entries,
                    selected = selected,
                    label = { it.label },
                    onSelect = onSelect,
                )
            }
        }
        // The caption belongs to the label, not to the row's icon: it lines up with "Auto-off".
        if (remaining == null) {
            Text(
                text = "Wireless debugging stays on until you turn it off.",
                modifier = Modifier.padding(start = AUTO_OFF_CAPTION_INSET),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.padding(start = AUTO_OFF_CAPTION_INSET),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Wireless debugging turns off in",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = remaining,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}


@Composable
private fun rememberRemaining(deadline: Instant?, onExpired: () -> Unit): String? {
    val target = deadline ?: return null
    var now by remember(target) { mutableStateOf(Clock.System.now()) }
    val expired = rememberUpdatedState(onExpired)

    LaunchedEffect(target) {
        while (true) {
            val current = Clock.System.now()
            now = current
            if (current >= target) {
                expired.value()
                break
            }
            delay(1_000)
        }
    }

    return target.remainingText(now)
}

// endregion

private fun EndpointKind.label(): String = when (this) {
    EndpointKind.WIFI -> "Wi-Fi"
    EndpointKind.HOTSPOT -> "Hotspot"
    EndpointKind.VPN -> "VPN"
    EndpointKind.OTHER -> "Other"
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                developerOptions = true,
                usbDebugging = true,
                wirelessDebugging = true,
                endpoint = AdbEndpoint(EndpointKind.WIFI, PREVIEW_HOST, PREVIEW_PORT),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDeveloperOptionsOffPreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(developerOptions = false),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenOfflinePreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                developerOptions = true,
                wirelessDebugging = true,
                endpoint = null,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPendingPreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(developerOptions = true, wirelessPending = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenAutoOffArmedPreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                developerOptions = true,
                wirelessDebugging = true,
                endpoint = AdbEndpoint(EndpointKind.WIFI, PREVIEW_HOST, PREVIEW_PORT),
                autoOffDuration = DurationOption.MIN_5,
                autoOffDeadline = Clock.System.now() + 4.minutes + 32.seconds,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenTimedPausePreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                developerOptions = false,
                timedToggle = TimedToggleSession(
                    target = false,
                    restoreTo = true,
                    deadline = Clock.System.now() + 4.minutes + 32.seconds,
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenTimedResumePreview() {
    DevinatorTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                developerOptions = true,
                timedToggle = TimedToggleSession(
                    target = true,
                    restoreTo = false,
                    deadline = Clock.System.now() + 4.minutes + 32.seconds,
                ),
            ),
            onEvent = {},
        )
    }
}
