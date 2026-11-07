package com.axondragonscale.devinator.ui.home

import android.content.ClipData
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.AutoOffDuration
import com.axondragonscale.devinator.model.EndpointKind
import com.axondragonscale.devinator.ui.Route
import com.axondragonscale.devinator.ui.components.DevinatorTopBar
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors
import com.axondragonscale.devinator.ui.theme.MonoCommandTextStyle
import com.axondragonscale.devinator.ui.theme.MonoTextStyle
import com.axondragonscale.devinator.ui.theme.SectionTitleTextStyle
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

private const val PREVIEW_HOST = "192.168.1.42"
private const val PREVIEW_PORT = 37129

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: HomeVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                HomeUiEvent.OpenTiles -> navController.navigate(Route.QuickSettingsTiles)
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
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val command = uiState.endpoint
        ?.takeIf { it.port != null }
        ?.let { "adb connect ${it.host}:${it.port}" }

    fun copyCommand() {
        val text = command ?: return
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("ADB command", text)))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DevinatorTopBar(
                actions = {
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
            UsbDebuggingCard(
                developerOptions = uiState.developerOptions,
                checked = uiState.usbDebugging,
                onToggle = { onEvent(HomeUiEvent.SetUsbDebugging(it)) },
            )
            WirelessDebuggingCard(
                uiState = uiState,
                onToggle = { onEvent(HomeUiEvent.SetWirelessDebugging(it)) },
                onCopy = ::copyCommand,
                onSelectAutoOff = { onEvent(HomeUiEvent.SelectAutoOff(it)) },
            )
            TilesCard(onOpen = { onEvent(HomeUiEvent.OpenTiles) })
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) colors.primarySurface else MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, if (enabled) colors.primaryOutline else colors.errorOutline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (enabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            colors.errorSurfaceHigh
                        },
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.DeveloperMode,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
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
                    StatusChip(on = enabled)
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
                large = true,
                dangerOff = true,
            )
        }
    }
}

@Composable
private fun UsbDebuggingCard(
    modifier: Modifier = Modifier,
    developerOptions: Boolean,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
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
    onCopy: () -> Unit,
    onSelectAutoOff: (AutoOffDuration) -> Unit,
) {
    val enabled = uiState.developerOptions
    val pending = uiState.wirelessPending
    val showDetails = enabled && uiState.wirelessDebugging && !pending

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
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
                    onCopy = onCopy,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                AutoOffBlock(
                    selected = uiState.autoOffDuration,
                    deadline = uiState.autoOffDeadline,
                    onSelect = onSelectAutoOff,
                )
            }
        }
    }
}

@Composable
private fun TilesCard(
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(icon = Icons.Filled.GridView)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Quick Settings Tiles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Add tiles to Quick Settings",
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
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.38f),
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
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun IconBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusChip(
    modifier: Modifier = Modifier,
    on: Boolean,
) {
    val colors = LocalDevinatorColors.current
    val contentColor = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (on) MaterialTheme.colorScheme.primaryContainer else colors.errorSurfaceHigh)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = contentColor, shape = CircleShape),
        )
        Text(
            text = if (on) "On" else "Off",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
        )
    }
}

@Composable
private fun DevinatorSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    large: Boolean = false,
    dangerOff: Boolean = false,
) {
    val trackWidth = if (large) 60.dp else 52.dp
    val trackHeight = if (large) 36.dp else 32.dp
    val thumbSize = if (large) 28.dp else 24.dp
    val iconSize = if (large) 16.dp else 14.dp
    val padding = 4.dp

    val trackColor = when {
        checked -> MaterialTheme.colorScheme.primary
        dangerOff -> LocalDevinatorColors.current.errorSurfaceHigh
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val thumbColor = when {
        checked -> MaterialTheme.colorScheme.onPrimary
        dangerOff -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconTint = when {
        checked -> MaterialTheme.colorScheme.primary
        dangerOff -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - padding * 2 else 0.dp,
        label = "switchThumbOffset",
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(trackWidth, trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(padding)
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center,
        ) {
            if (checked || dangerOff) {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = iconTint,
                )
            }
        }
    }
}

// endregion

// region Endpoint and auto-off

@Composable
private fun EndpointBlock(
    modifier: Modifier = Modifier,
    endpoint: AdbEndpoint?,
    onCopy: () -> Unit,
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
                text = "No network",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val address = endpoint.port?.let { "${endpoint.host}:$it" } ?: endpoint.host
    val copyable = endpoint.port != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (copyable) {
                    Modifier.clickable(onClickLabel = "Copy adb command", onClick = onCopy)
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
                    text = "Must be on this network",
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
        if (copyable) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (copyable) {
        val command = "adb connect ${endpoint.host}:${endpoint.port}"
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClickLabel = "Copy adb command", onClick = onCopy)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = command,
                modifier = Modifier.weight(1f),
                style = MonoCommandTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Text(
            text = "Port is on the Developer options screen",
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
            .height(20.dp)
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
    selected: AutoOffDuration,
    deadline: Instant?,
    onSelect: (AutoOffDuration) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val countdown = rememberCountdown(deadline)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
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
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected == AutoOffDuration.OFF) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            ) {
                Text(
                    text = "Wireless debugging turns itself off when the timer ends.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AutoOffDuration.entries.forEach { duration ->
                    DropdownMenuItem(
                        text = { Text(duration.label) },
                        trailingIcon = {
                            if (duration == selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelect(duration)
                        },
                    )
                }
            }
        }
        Text(
            text = countdown ?: "Stays on until you turn it off.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun rememberCountdown(deadline: Instant?): String? {
    val target = deadline ?: return null
    var now by remember(target) { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(target) {
        while (now < target) {
            delay(1_000)
            now = Clock.System.now()
        }
    }

    val remaining: Duration = target - now
    if (remaining.inWholeSeconds <= 0) return null
    val seconds = remaining.inWholeSeconds
    return "Turns off in ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}."
}

// endregion

private val AutoOffDuration.label: String
    get() = when (this) {
        AutoOffDuration.OFF -> "Off"
        AutoOffDuration.MIN_5 -> "5 min"
        AutoOffDuration.MIN_15 -> "15 min"
        AutoOffDuration.MIN_30 -> "30 min"
    }

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
