package com.axondragonscale.wallmatic.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QueuePlayNext
import androidx.compose.material.icons.outlined.RemoveFromQueue
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
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
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.util.countSummary
import com.axondragonscale.wallmatic.ui.util.getAspectRatio
import com.axondragonscale.wallmatic.ui.util.performLongPressHapticFeedback
import com.axondragonscale.wallmatic.ui.util.toDateTimeString
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 06/06/24
 */

private val AutoCycleIntervals: List<Duration> = listOf(
    15.minutes, 30.minutes, 45.minutes,
    1.hours, 90.minutes, 2.hours,
    3.hours, 4.hours, 6.hours,
    8.hours, 12.hours, 24.hours,
)

private fun Duration.displayLabel(): String {
    val h = inWholeHours
    val m = inWholeMinutes % 60
    return when {
        h == 0L -> "$m Minutes"
        m == 0L -> if (h == 1L) "1 Hour" else "$h Hours"
        else -> "${h}h ${m}m"
    }
}

private fun Duration.chipLabel(): String {
    val h = inWholeHours
    val m = inWholeMinutes % 60
    return when {
        h == 0L -> "${m}m"
        m == 0L -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var selectAlbumBottomSheetTarget by rememberSaveable {
        mutableStateOf<TargetScreen?>(null)
    }

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
            text = "Wallmatic"
        )

        when {
            uiState.isLoading ->
                LoadingState(message = "Loading wallpapers...")

            uiState.albums.isEmpty() ->
                TextAndButton(
                    text = """
                        Auto cycle wallpapers.
                        Choose individual images
                        Or entire folders.
                    """.trimIndent(),
                    buttonText = "CREATE ALBUM",
                    buttonIcon = Icons.Outlined.AddCircle,
                    onClick = { onEvent(HomeUiEvent.CreateAlbumClick) }
                )

            !uiState.config.isInit || uiState.homeAlbum == null || uiState.lockAlbum == null ->
                TextAndButton(
                    text = """
                        Auto cycle wallpapers.
                        Choose individual images
                        Or entire folders.
                    """.trimIndent(),
                    buttonText = "SELECT ALBUM",
                    buttonIcon = Icons.Outlined.CheckCircle,
                    onClick = { onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Both)) }
                )

            else -> {
                HomeScreenCard(
                    modifier = Modifier.padding(vertical = 8.dp),
                    uiState = uiState,
                    onEvent = onEvent,
                )

                LockScreenCard(
                    modifier = Modifier.padding(vertical = 8.dp),
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(BOTTOM_BAR_HEIGHT + navigationBarsHeight() + 8.dp)
        )
    }
}

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
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
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

@Composable
private fun HomeScreenCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) = WallmaticCard(modifier = modifier, title = "Home Screen") {

    AlbumConfigCard(
        album = uiState.homeAlbum!!,
        autoCycleEnabled = uiState.config.homeConfig.autoCycleEnabled,
        onSelectAlbumClick = {
            onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Home))
        },
        onAutoCycleToggle = {
            onEvent(HomeUiEvent.AutoCycleToggled(it, TargetScreen.Home))
        },
    )

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

    WallpaperPreviewCard(
        wallpaper = uiState.homeWallpaper,
        autoCycleEnabled = uiState.config.homeConfig.autoCycleEnabled,
        lastUpdated = uiState.config.lastUpdated,
        nextUpdate = uiState.config.nextUpdate,
        onWallpaperClick = {
            onEvent(HomeUiEvent.NavigateToWallpaper(it))
        },
        onChangeWallpaperClick = {
            onEvent(HomeUiEvent.ChangeWallpaper(TargetScreen.Home))
        }
    )

    AnimatedVisibility(
        visible = uiState.config.homeConfig.autoCycleEnabled,
        enter = expandVertically(tween()) + fadeIn(tween(delayMillis = 300)),
        exit = fadeOut() + shrinkVertically(tween(delayMillis = 300))
    ) {
        Column {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AutoCycleIntervalCard(
                interval = uiState.config.updateInterval,
                onIntervalUpdate = {
                    onEvent(HomeUiEvent.IntervalUpdated(it))
                },
            )
        }
    }
}

@Composable
private fun LockScreenCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) = WallmaticCard(modifier = modifier, title = "Lock Screen") {
    MirrorHomeScreenCard(
        mirrorHomeConfigForLock = uiState.config.mirrorHomeConfigForLock,
        onToggle = { onEvent(HomeUiEvent.MirrorHomeConfigForLockToggled) },
    )

    // TODO: Extract this out
    // Maybe the preview and the other (future) controls should also be hidden
    // when autoCycle is disabled
    AnimatedVisibility(
        visible = !uiState.config.mirrorHomeConfigForLock && uiState.lockAlbum != null,
        enter = expandVertically(tween()) + fadeIn(tween(delayMillis = 300)),
        exit = fadeOut() + shrinkVertically(tween(delayMillis = 300))
    ) {
        Column {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            AlbumConfigCard(
                album = uiState.lockAlbum!!,
                autoCycleEnabled = uiState.config.lockConfig.autoCycleEnabled,
                onSelectAlbumClick = {
                    onEvent(HomeUiEvent.SelectAlbumClick(TargetScreen.Lock))
                },
                onAutoCycleToggle = {
                    onEvent(HomeUiEvent.AutoCycleToggled(it, TargetScreen.Lock))
                },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            WallpaperPreviewCard(
                wallpaper = uiState.lockWallpaper,
                autoCycleEnabled = uiState.config.lockConfig.autoCycleEnabled,
                lastUpdated = uiState.config.lastUpdated,
                nextUpdate = uiState.config.nextUpdate,
                onWallpaperClick = {
                    onEvent(HomeUiEvent.NavigateToWallpaper(it))
                },
                onChangeWallpaperClick = {
                    onEvent(HomeUiEvent.ChangeWallpaper(TargetScreen.Lock))
                }
            )
        }
    }
}

@Composable
private fun MirrorHomeScreenCard(
    modifier: Modifier = Modifier,
    mirrorHomeConfigForLock: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    SettingsCard(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.SyncLock,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                text = "Mirror Home Screen",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = "Use same settings as Home Screen",
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingContent = {
            Switch(
                checked = mirrorHomeConfigForLock,
                onCheckedChange = onToggle
            )
        }
    )
}

@Composable
private fun AlbumConfigCard(
    modifier: Modifier = Modifier,
    album: Album,
    autoCycleEnabled: Boolean,
    onSelectAlbumClick: () -> Unit,
    onAutoCycleToggle: (Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier.clickable { onSelectAlbumClick() },
        leadingContent = {
            WallpaperThumbnail(
                uri = album.coverUri,
                size = 48.dp,
                cornerRadius = 8.dp,
            )
        },
        headlineContent = {
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = album.countSummary(),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingContent = {
            Switch(
                checked = autoCycleEnabled,
                onCheckedChange = onAutoCycleToggle,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun WallpaperPreviewCard(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper?,
    autoCycleEnabled: Boolean,
    lastUpdated: Long,
    nextUpdate: Long,
    onWallpaperClick: (Int) -> Unit,
    onChangeWallpaperClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .aspectRatio(getAspectRatio())
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(enabled = wallpaper?.uri != null) {
                    wallpaper?.let { onWallpaperClick(it.id) }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (wallpaper?.uri != null) {
                AsyncImage(
                    model = wallpaper.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = "No Wallpaper",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val view = LocalView.current
        Column(
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WallpaperInfoItem(
                    icon = Icons.Outlined.RemoveFromQueue,
                    label = "Current",
                    value = lastUpdated.toDateTimeString(),
                )
                WallpaperInfoItem(
                    icon = Icons.Outlined.QueuePlayNext,
                    label = "Next",
                    value = if (autoCycleEnabled) nextUpdate.toDateTimeString() else "Auto-cycle off",
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                onClick = {
                    view.performLongPressHapticFeedback()
                    onChangeWallpaperClick()
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Change Wallpaper")
            }
        }
    }
}

@Composable
private fun WallpaperInfoItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AutoCycleIntervalCard(
    modifier: Modifier = Modifier,
    interval: Long,
    onIntervalUpdate: (Long) -> Unit,
) {
    val selected = AutoCycleIntervals.minByOrNull { abs(it.inWholeMilliseconds - interval) }
        ?: AutoCycleIntervals.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "Auto Cycle Interval",
            style = MaterialTheme.typography.labelLarge,
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(horizontal = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 10.dp),
            text = selected.displayLabel(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        ) {
            items(AutoCycleIntervals) { duration ->
                val isSelected = duration == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onIntervalUpdate(duration.inWholeMilliseconds) },
                    label = { Text(text = duration.chipLabel()) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else null,
                )
            }
        }
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCreateAlbum() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember { HomeUiState(isLoading = false) }
            Home(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSelectAlbum() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember {
                HomeUiState(isLoading = false, albums = listOf(Album(name = "a")))
            }
            Home(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember {
                HomeUiState(
                    isLoading = false,
                    albums = listOf(Album(name = "Album 1").apply { id = 1 }),
                    config = config {
                        isInit = true
                        mirrorHomeConfigForLock = false
                        updateInterval = 15.minutes.inWholeMilliseconds
                        homeConfig = wallpaperConfig {
                            albumId = 1
                            autoCycleEnabled = false
                        }
                        lockConfig = wallpaperConfig {
                            albumId = 1
                        }
                    }
                )
            }
            Home(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}
