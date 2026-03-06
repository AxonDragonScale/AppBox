package com.axondragonscale.wallmatic.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QueuePlayNext
import androidx.compose.material.icons.outlined.RemoveFromQueue
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.compose.navigationBarsHeight
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.config
import com.axondragonscale.wallmatic.model.wallpaperConfig
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.bottombar.BOTTOM_BAR_HEIGHT
import com.axondragonscale.wallmatic.ui.bottombar.Tab
import com.axondragonscale.wallmatic.ui.common.LoadingState
import com.axondragonscale.wallmatic.ui.common.SelectAlbumBottomSheet
import com.axondragonscale.wallmatic.ui.common.SettingsCard
import com.axondragonscale.wallmatic.ui.common.TabHeader
import com.axondragonscale.wallmatic.ui.common.WallmaticCard
import com.axondragonscale.wallmatic.ui.common.WallpaperThumbnail
import com.axondragonscale.wallmatic.ui.common.Wallpaper
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.util.countSummary
import com.axondragonscale.wallmatic.ui.util.getAspectRatio
import com.axondragonscale.wallmatic.ui.util.performLongPressHapticFeedback
import com.axondragonscale.wallmatic.ui.util.performTickHapticFeedback
import com.axondragonscale.wallmatic.ui.util.toDateTimeString
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 06/06/24
 */

// Non-linear interval stops in milliseconds
private val INTERVAL_STOPS: List<Long> = listOf(
    15.minutes.inWholeMilliseconds,
    30.minutes.inWholeMilliseconds,
    45.minutes.inWholeMilliseconds,
    1.hours.inWholeMilliseconds,
    90.minutes.inWholeMilliseconds,
    2.hours.inWholeMilliseconds,
    3.hours.inWholeMilliseconds,
    4.hours.inWholeMilliseconds,
    6.hours.inWholeMilliseconds,
    8.hours.inWholeMilliseconds,
    12.hours.inWholeMilliseconds,
    24.hours.inWholeMilliseconds,
)

// Labels for all 12 stops — odd-indexed ones are shown, even-indexed are empty (alternating)
private val INTERVAL_STOP_LABELS: List<String> = listOf(
    "15m", "", "45m", "", "1.5h", "", "3h", "", "6h", "", "12h", "",
)

private fun Long.toSliderIndex(): Float {
    val closest = INTERVAL_STOPS.minByOrNull { kotlin.math.abs(it - this) } ?: INTERVAL_STOPS.first()
    return INTERVAL_STOPS.indexOf(closest).toFloat()
}

private fun Long.toIntervalLabel(): String {
    val totalMins = this.milliseconds.inWholeMinutes
    val h = (totalMins / 60).toInt()
    val m = (totalMins % 60).toInt()
    return when {
        h == 0 -> "$m Minutes"
        m == 0 -> if (h == 1) "1 Hour" else "$h Hours"
        else -> if (h == 1) "1 Hour $m Minutes" else "$h Hours $m Minutes"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var selectAlbumBottomSheetTarget by rememberSaveable { mutableStateOf<TargetScreen?>(null) }

    Home(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.CreateAlbumClick ->
                    navController.navigate(Route.Dashboard(tab = Tab.Albums.position))
                is HomeUiEvent.SelectAlbumClick ->
                    selectAlbumBottomSheetTarget = event.target
                is HomeUiEvent.NavigateToWallpaper ->
                    navController.navigate(Route.Wallpaper(event.wallpaperId))
                else -> vm.onEvent(event)
            }
        }
    )

    if (selectAlbumBottomSheetTarget != null) {
        SelectAlbumBottomSheet(
            albums = uiState.albums,
            selectedAlbumId = uiState.config.let {
                if (it.hasHomeConfig()) it.homeConfig.albumId else -1
            },
            onSelectAlbum = {
                vm.onEvent(HomeUiEvent.AlbumSelected(it.id, selectAlbumBottomSheetTarget!!))
            },
            onDismiss = { selectAlbumBottomSheetTarget = null },
        )
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        TabHeader(
            modifier = Modifier.padding(start = 8.dp, top = 48.dp, bottom = 8.dp),
            text = "Wallmatic",
        )

        when {
            uiState.isLoading ->
                LoadingState(message = "Loading wallpapers...")

            uiState.albums.isEmpty() ->
                TextAndButton(
                    text = "Auto cycle wallpapers.\nChoose individual images\nOr entire folders.",
                    buttonText = "CREATE ALBUM",
                    buttonIcon = Icons.Outlined.AddCircle,
                    onClick = { onEvent(HomeUiEvent.CreateAlbumClick) },
                )

            !uiState.config.isInit || uiState.homeAlbum == null ->
                TextAndButton(
                    text = "Auto cycle wallpapers.\nChoose individual images\nOr entire folders.",
                    buttonText = "SELECT ALBUM",
                    buttonIcon = Icons.Outlined.CheckCircle,
                    onClick = { onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Both)) },
                )

            else -> {
                WallpapersCard(
                    modifier = Modifier.padding(vertical = 8.dp),
                    uiState = uiState,
                    onEvent = onEvent,
                )

                MirrorToggleCard(
                    modifier = Modifier.padding(vertical = 8.dp),
                    mirrorHomeConfigForLock = uiState.config.mirrorHomeConfigForLock,
                    onToggle = { onEvent(HomeUiEvent.MirrorHomeConfigForLockToggled) },
                )

                AutoCycleCard(
                    modifier = Modifier.padding(vertical = 8.dp),
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(BOTTOM_BAR_HEIGHT + navigationBarsHeight() + 8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TextAndButton (empty / no-album states)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextAndButton(
    modifier: Modifier = Modifier,
    text: String,
    buttonText: String,
    buttonIcon: ImageVector,
    onClick: () -> Unit,
) = Card(modifier = modifier.padding(vertical = 8.dp)) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .height(48.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = buttonIcon,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WallpapersCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WallpapersCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val mirroring = uiState.config.mirrorHomeConfigForLock
    val lastUpdated = uiState.config.lastUpdated
    val nextUpdate = uiState.config.nextUpdate

    // Use BoxWithConstraints to compute the exact column width:
    //   columnWidth = (totalWidth - gap) / 2
    // This ensures the home column is always the same physical size regardless of
    // whether the lock column is shown. When mirroring is on, the single home column
    // is centered inside the full-width Box.
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val gap = 8.dp
        val columnWidth = (maxWidth - gap) / 2

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        ) {
            WallpaperColumn(
                modifier = Modifier.width(columnWidth),
                label = "Home",
                wallpaper = uiState.homeWallpaper,
                autoCycleEnabled = uiState.config.homeConfig.autoCycleEnabled,
                lastUpdated = lastUpdated,
                nextUpdate = nextUpdate,
                buttonLabel = if (mirroring) "Change Both" else "Change",
                onPreviewClick = { uiState.homeWallpaper?.let { onEvent(HomeUiEvent.NavigateToWallpaper(it.id)) } },
                onChangeClick = {
                    onEvent(HomeUiEvent.ChangeWallpaper(if (mirroring) TargetScreen.Both else TargetScreen.Home))
                },
            )

            AnimatedVisibility(
                visible = !mirroring,
                enter = expandVertically(tween()) + fadeIn(tween(delayMillis = 150)),
                exit = fadeOut(tween()) + shrinkVertically(tween(delayMillis = 150)),
            ) {
                WallpaperColumn(
                    modifier = Modifier.width(columnWidth),
                    label = "Lock",
                    wallpaper = uiState.lockWallpaper,
                    autoCycleEnabled = uiState.config.lockConfig.autoCycleEnabled,
                    lastUpdated = lastUpdated,
                    nextUpdate = nextUpdate,
                    buttonLabel = "Change",
                    onPreviewClick = { uiState.lockWallpaper?.let { onEvent(HomeUiEvent.NavigateToWallpaper(it.id)) } },
                    onChangeClick = { onEvent(HomeUiEvent.ChangeWallpaper(TargetScreen.Lock)) },
                )
            }
        }
    }
}

@Composable
private fun WallpaperColumn(
    modifier: Modifier = Modifier,
    label: String,
    wallpaper: Wallpaper?,
    autoCycleEnabled: Boolean,
    lastUpdated: Long,
    nextUpdate: Long,
    buttonLabel: String,
    onPreviewClick: () -> Unit,
    onChangeClick: () -> Unit,
) {
    val aspectRatio = getAspectRatio()
    val view = LocalView.current

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Screen label chip
        Text(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
        )

        // Wallpaper preview — fixed aspect ratio, always same size regardless of column count
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = wallpaper?.uri != null) { onPreviewClick() },
            contentAlignment = Alignment.Center,
        ) {
            if (wallpaper?.uri != null) {
                Wallpaper(
                    modifier = Modifier.fillMaxSize(),
                    uri = wallpaper.uri,
                    cornerRadius = 12.dp,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = "No wallpaper",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current & next — label and date on the same line
        WallpaperInfoRow(
            icon = Icons.Outlined.RemoveFromQueue,
            label = "Now",
            value = if (lastUpdated == -1L) "—" else lastUpdated.toDateTimeString(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        WallpaperInfoRow(
            icon = Icons.Outlined.QueuePlayNext,
            label = "Next",
            value = if (autoCycleEnabled) nextUpdate.toDateTimeString() else "Off",
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                view.performLongPressHapticFeedback()
                onChangeClick()
            },
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(text = buttonLabel)
        }
    }
}

// Icon + "Label  value" all on one line
@Composable
private fun WallpaperInfoRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(14.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MirrorToggleCard
// ─────────────────────────────────────────────────────────────────────────────

// Flattened: no WallmaticCard wrapper, SettingsCard is the card itself.
@Composable
private fun MirrorToggleCard(
    modifier: Modifier = Modifier,
    mirrorHomeConfigForLock: Boolean,
    onToggle: () -> Unit,
) = Card(modifier = modifier.fillMaxWidth()) {
    SettingsCard(
        shape = RoundedCornerShape(0.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.SyncLock,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = "Mirror lock to home",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = "Lock screen uses the same album and wallpaper as home",
                style = MaterialTheme.typography.labelMedium,
            )
        },
        trailingContent = {
            Switch(
                checked = mirrorHomeConfigForLock,
                onCheckedChange = { onToggle() },
            )
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AutoCycleCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AutoCycleCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) = WallmaticCard(modifier = modifier, title = "Auto Cycle") {
    val mirroring = uiState.config.mirrorHomeConfigForLock
    val homeAutoCycle = uiState.config.homeConfig.autoCycleEnabled
    val lockAutoCycle = uiState.config.lockConfig.autoCycleEnabled

    AlbumSection(
        label = "Home",
        album = uiState.homeAlbum!!,
        autoCycleEnabled = homeAutoCycle,
        onSelectAlbumClick = { onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Home)) },
        onAutoCycleToggle = { onEvent(HomeUiEvent.AutoCycleToggled(it, TargetScreen.Home)) },
    )

    AnimatedVisibility(
        visible = !mirroring,
        enter = expandVertically(tween()) + fadeIn(tween(delayMillis = 150)),
        exit = fadeOut(tween()) + shrinkVertically(tween(delayMillis = 150)),
    ) {
        Column {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            AlbumSection(
                label = "Lock",
                album = uiState.lockAlbum ?: uiState.homeAlbum!!,
                autoCycleEnabled = lockAutoCycle,
                onSelectAlbumClick = { onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Lock)) },
                onAutoCycleToggle = { onEvent(HomeUiEvent.AutoCycleToggled(it, TargetScreen.Lock)) },
            )
        }
    }

    AnimatedVisibility(
        visible = homeAutoCycle || lockAutoCycle,
        enter = expandVertically(tween()) + fadeIn(tween(delayMillis = 300)),
        exit = fadeOut(tween()) + shrinkVertically(tween(delayMillis = 300)),
    ) {
        Column {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            AutoCycleIntervalCard(
                interval = uiState.config.updateInterval,
                onIntervalUpdate = { onEvent(HomeUiEvent.IntervalUpdated(it)) },
            )
        }
    }
}

// AlbumSection: label rendered as a small title above the album selector row.
// Layout: "Home" / "Lock" title line, then thumbnail | name + count | switch on the row below.
@Composable
private fun AlbumSection(
    modifier: Modifier = Modifier,
    album: Album,
    label: String,
    autoCycleEnabled: Boolean,
    onSelectAlbumClick: () -> Unit,
    onAutoCycleToggle: (Boolean) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Clickable album selector
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectAlbumClick() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WallpaperThumbnail(uri = album.coverUri, size = 40.dp, cornerRadius = 6.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = album.countSummary(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Switch(
                checked = autoCycleEnabled,
                onCheckedChange = onAutoCycleToggle,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AutoCycleIntervalCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AutoCycleIntervalCard(
    modifier: Modifier = Modifier,
    interval: Long,
    onIntervalUpdate: (Long) -> Unit,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    var sliderIndex by remember(interval) {
        mutableFloatStateOf(interval.toSliderIndex())
    }

    val currentInterval = INTERVAL_STOPS[sliderIndex.toInt().coerceIn(0, INTERVAL_STOPS.lastIndex)]

    Column(modifier = modifier.fillMaxWidth()) {

        // Title row: "Interval" label on left, formatted value pill on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Interval",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                text = currentInterval.toIntervalLabel(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Slider
        Slider(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = sliderIndex,
            valueRange = 0f..(INTERVAL_STOPS.lastIndex.toFloat()),
            steps = INTERVAL_STOPS.lastIndex - 1,
            onValueChange = { newIndex ->
                sliderIndex = newIndex
                view.performTickHapticFeedback()
                job?.cancel()
                job = scope.launch {
                    delay(400)
                    if (!isActive) return@launch
                    onIntervalUpdate(INTERVAL_STOPS[newIndex.toInt().coerceIn(0, INTERVAL_STOPS.lastIndex)])
                }
            },
        )

        // Alternating track labels — one per stop, odd-indexed labels are empty strings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            INTERVAL_STOP_LABELS.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCreateAlbum() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(uiState = HomeUiState(isLoading = false), onEvent = {})
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSelectAlbum() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(
                uiState = HomeUiState(isLoading = false, albums = listOf(Album(name = "Nature"))),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMirroringOff() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val album = Album(name = "Nature").apply { id = 1 }
            Home(
                uiState = HomeUiState(
                    isLoading = false,
                    albums = listOf(album),
                    config = config {
                        isInit = true
                        mirrorHomeConfigForLock = false
                        updateInterval = 2.hours.inWholeMilliseconds
                        lastUpdated = System.currentTimeMillis() - 30.minutes.inWholeMilliseconds
                        homeConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = true
                            currentWallpaperId = -1
                        }
                        lockConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = true
                            currentWallpaperId = -1
                        }
                    },
                ),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMirroringOn() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val album = Album(name = "Nature").apply { id = 1 }
            Home(
                uiState = HomeUiState(
                    isLoading = false,
                    albums = listOf(album),
                    config = config {
                        isInit = true
                        mirrorHomeConfigForLock = true
                        updateInterval = 1.hours.inWholeMilliseconds
                        lastUpdated = System.currentTimeMillis() - 10.minutes.inWholeMilliseconds
                        homeConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = true
                            currentWallpaperId = -1
                        }
                        lockConfig = homeConfig
                    },
                ),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAutoCycleOff() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val album = Album(name = "Nature").apply { id = 1 }
            Home(
                uiState = HomeUiState(
                    isLoading = false,
                    albums = listOf(album),
                    config = config {
                        isInit = true
                        mirrorHomeConfigForLock = false
                        updateInterval = 2.hours.inWholeMilliseconds
                        lastUpdated = System.currentTimeMillis()
                        homeConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = false
                            currentWallpaperId = -1
                        }
                        lockConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = false
                            currentWallpaperId = -1
                        }
                    },
                ),
                onEvent = {},
            )
        }
    }
}
