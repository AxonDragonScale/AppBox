package com.axondragonscale.wallmatic.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.config
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.wallpaperConfig
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.CustomIntervalSheet
import com.axondragonscale.wallmatic.ui.component.EmptyAlbumLayout
import com.axondragonscale.wallmatic.ui.component.EmptyPreviewMark
import com.axondragonscale.wallmatic.ui.component.HomeLockTabs
import com.axondragonscale.wallmatic.ui.component.HorizontalRule
import com.axondragonscale.wallmatic.ui.component.IntervalPicker
import com.axondragonscale.wallmatic.ui.component.Masthead
import com.axondragonscale.wallmatic.ui.component.SelectAlbumSheet
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.StatusRow
import com.axondragonscale.wallmatic.ui.component.WallmaticButton
import com.axondragonscale.wallmatic.ui.component.WallmaticButtonStyle
import com.axondragonscale.wallmatic.ui.component.WallpaperPlate
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary
import com.axondragonscale.wallmatic.ui.util.toShortDateTimeString
import com.axondragonscale.wallmatic.util.failedTargetOrNull
import com.axondragonscale.wallmatic.util.nextUpdate
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 06/06/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToLibrary: () -> Unit,
) {
    val vm: HomeVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var target by rememberSaveable { mutableStateOf(TargetScreen.Home) }
    var showSelectAlbum by rememberSaveable { mutableStateOf(false) }
    var showCustomInterval by rememberSaveable { mutableStateOf(false) }

    val mirrored = uiState.config.mirrorHomeConfigForLock
    val activeTarget = if (mirrored) TargetScreen.Home else target
    val activeAlbum = uiState.albumFor(activeTarget)

    Home(
        modifier = modifier,
        uiState = uiState,
        target = activeTarget,
        showTargetTabs = !mirrored && uiState.albums.isNotEmpty(),
        onTargetChange = { target = it },
        onEvent = { event ->
            when (event) {
                HomeUiEvent.CreateAlbumClick -> onNavigateToLibrary()

                is HomeUiEvent.SelectAlbumClick -> showSelectAlbum = true
                HomeUiEvent.CustomIntervalClick -> showCustomInterval = true

                is HomeUiEvent.NavigateToWallpaper ->
                    navController.navigate(Route.Wallpaper(event.wallpaperId))

                else -> vm.onEvent(event)
            }
        },
    )

    if (showSelectAlbum) {
        SelectAlbumSheet(
            albums = uiState.albums,
            selectedAlbumId = activeAlbum?.id ?: -1,
            onSelectAlbum = { vm.onEvent(HomeUiEvent.AlbumSelected(it.id, activeTarget)) },
            onDismiss = { showSelectAlbum = false },
        )
    }

    if (showCustomInterval) {
        CustomIntervalSheet(
            initial = Interval.fromMillis(uiState.config.updateInterval),
            onConfirm = {
                vm.onEvent(HomeUiEvent.IntervalUpdated(it.inWholeMilliseconds))
                showCustomInterval = false
            },
            onDismiss = { showCustomInterval = false },
        )
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    target: TargetScreen,
    showTargetTabs: Boolean,
    onTargetChange: (TargetScreen) -> Unit,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        if (showTargetTabs) {
            HomeLockTabs(selected = target, onSelect = onTargetChange)
            Spacer(modifier = Modifier.height(20.dp))
        }

        when {
            uiState.isLoading -> Loading()

            uiState.albums.isEmpty() -> NoAlbums(
                modifier = Modifier.weight(1f),
                onCreateAlbum = { onEvent(HomeUiEvent.CreateAlbumClick) },
            )

            uiState.albumFor(target) == null -> NoActiveAlbum(
                modifier = Modifier.weight(1f),
                onChangeAlbum = { onEvent(HomeUiEvent.SelectAlbumClick(target)) },
            )

            else -> ActiveAlbum(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                target = target,
                onEvent = onEvent,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun Loading() {
    StateMessage(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        title = "LOADING",
    )
}

@Composable
private fun ActiveAlbum(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    target: TargetScreen,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val album = uiState.albumFor(target) ?: return
    val wallpaper = uiState.wallpaperFor(target)
    val config = uiState.config
    val hasSchedule = config.lastUpdated > 0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WallpaperPlate(
                modifier = Modifier
                    .weight(180f)
                    .aspectRatio(Dimens.DeviceAspectRatio),
                uri = wallpaper?.uri,
                interactive = !wallpaper?.uri.isNullOrBlank(),
                onClick = { wallpaper?.let { onEvent(HomeUiEvent.NavigateToWallpaper(it.id)) } },
            ) {
                if (wallpaper?.uri.isNullOrBlank()) {
                    EmptyPreviewMark(modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(168f)) {
                Text(
                    text = "ALBUM",
                    style = WallmaticTextStyles.microStrong.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.wallmatic.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.wallmatic.ink)
                        .clickable(onClickLabel = "Change album") {
                            onEvent(HomeUiEvent.SelectAlbumClick(target))
                        }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = album.name,
                        style = WallmaticTextStyles.title.copy(
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.wallmatic.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.wallmatic.ink,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = album.countSummary(),
                    style = WallmaticTextStyles.caption,
                    color = MaterialTheme.wallmatic.inkMuted,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.wallmatic.ruleSoft)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ScheduleLabel("CURRENT")
                Spacer(modifier = Modifier.height(2.dp))
                ScheduleValue(config.lastUpdated.scheduleTime())

                Spacer(modifier = Modifier.height(8.dp))

                ScheduleLabel("NEXT")
                Spacer(modifier = Modifier.height(2.dp))
                ScheduleValue(if (hasSchedule) config.nextUpdate.scheduleTime() else "—")
            }
        }

        if (config.lastFailedAt > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            StatusRow(
                title = "Last update failed",
                detail = config.lastFailedAt.toShortDateTimeString(),
                action = "Retry",
                onAction = {
                    onEvent(HomeUiEvent.RetryUpdate(config.failedTargetOrNull ?: target))
                },
            )
            Spacer(modifier = Modifier.height(22.dp))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            HorizontalRule(color = MaterialTheme.wallmatic.ruleSoft)
            Spacer(modifier = Modifier.height(35.dp))
        }

        IntervalPicker(
            value = Interval.fromMillis(config.updateInterval),
            onValueChange = { onEvent(HomeUiEvent.IntervalUpdated(it.inWholeMilliseconds)) },
            onCustomClick = { onEvent(HomeUiEvent.CustomIntervalClick) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        WallmaticButton(
            text = "Change now",
            style = WallmaticButtonStyle.Outline,
            onClick = { onEvent(HomeUiEvent.ChangeNow(target)) },
        )
    }
}

@Composable
private fun ScheduleLabel(text: String) {
    Text(
        text = text,
        style = WallmaticTextStyles.microStrong,
        color = MaterialTheme.wallmatic.inkMuted,
    )
}

@Composable
private fun ScheduleValue(text: String) {
    Text(
        text = text,
        style = WallmaticTextStyles.time,
        color = MaterialTheme.wallmatic.ink,
    )
}

private fun Long.scheduleTime(): String =
    if (this <= 0L || this == Long.MAX_VALUE) "—" else toShortDateTimeString()

@Composable
private fun NoAlbums(
    modifier: Modifier = Modifier,
    onCreateAlbum: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EmptyAlbumLayout(
            eyebrow = "GET STARTED",
            title = "No album\nyet.",
            description = "An album is a folder of wallpapers. Create one, add a few, and Wallmatic rotates them on your schedule.",
            steps = listOf("Create an album", "Add wallpapers", "Set it active"),
        )
        Spacer(modifier = Modifier.weight(1f))
        WallmaticButton(text = "Create album", onClick = onCreateAlbum)
    }
}

@Composable
private fun NoActiveAlbum(
    modifier: Modifier = Modifier,
    onChangeAlbum: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EmptyAlbumLayout(
            eyebrow = "GET STARTED",
            title = "No album\nselected.",
            description = "Choose an album to start rotating its wallpapers on your schedule.",
            steps = emptyList(),
        )
        Spacer(modifier = Modifier.weight(1f))
        WallmaticButton(text = "Change album", onClick = onChangeAlbum)
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun HomeMirroredPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
            Home(
                uiState = activeState(mirrored = true),
                target = TargetScreen.Home,
                showTargetTabs = false,
                onTargetChange = {},
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun HomeLockPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
            Home(
                uiState = activeState(mirrored = false),
                target = TargetScreen.Lock,
                showTargetTabs = true,
                onTargetChange = {},
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun HomeNoAlbumPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
            Home(
                uiState = HomeUiState(isLoading = false),
                target = TargetScreen.Home,
                showTargetTabs = false,
                onTargetChange = {},
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun HomeUpdateFailedPreview() {
    WallmaticTheme {
        val activeState = activeState(mirrored = true)
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
            Home(
                uiState = activeState.copy(
                    config = activeState.config.copy {
                        lastFailedAt = 1_700_000_900_000L
                    },
                ),
                target = TargetScreen.Home,
                showTargetTabs = false,
                onTargetChange = {},
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun HomeNoActiveAlbumPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
            Home(
                uiState = HomeUiState(
                    isLoading = false,
                    albums = listOf(Album(name = "Test").apply { id = 1 }),
                    config = config {
                        isInit = true
                        updateInterval = 15.minutes.inWholeMilliseconds
                        homeConfig = wallpaperConfig { albumId = 99 }
                    },
                ),
                target = TargetScreen.Home,
                showTargetTabs = true,
                onTargetChange = {},
                onEvent = {},
            )
        }
    }
}

private fun activeState(mirrored: Boolean): HomeUiState {
    val now = 1_700_000_000_000L
    val album = Album(name = "Test").apply {
        id = 1
        folderIds = listOf(1)
        wallpaperIds = (1..24).toList()
    }
    return HomeUiState(
        isLoading = false,
        albums = listOf(album),
        config = config {
            isInit = true
            updateInterval = 15.minutes.inWholeMilliseconds
            lastUpdated = now
            mirrorHomeConfigForLock = mirrored
            homeConfig = wallpaperConfig {
                albumId = 1
                autoCycleEnabled = true
                currentWallpaperId = 1
            }
            lockConfig = wallpaperConfig {
                albumId = 1
                autoCycleEnabled = true
                currentWallpaperId = 2
            }
        },
        homeWallpaper = Wallpaper(uri = "").apply { id = 1 },
        lockWallpaper = Wallpaper(uri = "").apply { id = 2 },
    )
}
