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
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.ScreenConfig
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallmaticConfig
import com.axondragonscale.wallmatic.model.WallpaperCrop
import com.axondragonscale.wallmatic.model.WallpaperFailure
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.CustomIntervalSheet
import com.axondragonscale.wallmatic.ui.component.EmptyAlbumScreen
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
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

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

    val mirrored = uiState.config.mirrorHomeForLock
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

                HomeUiEvent.SelectAlbumClick -> showSelectAlbum = true
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
            selectedAlbumId = activeAlbum?.id,
            onSelectAlbum = { vm.onEvent(HomeUiEvent.AlbumSelected(it.id, activeTarget)) },
            onDismiss = { showSelectAlbum = false },
        )
    }

    if (showCustomInterval) {
        CustomIntervalSheet(
            initial = uiState.config.interval,
            onConfirm = {
                vm.onEvent(HomeUiEvent.IntervalUpdated(it))
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
    Column(modifier = modifier.fillMaxSize()) {
        if (showTargetTabs) {
            Spacer(modifier = Modifier.height(20.dp))

            HomeLockTabs(
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                selected = target,
                onSelect = onTargetChange,
            )
        }

        when {
            uiState.isLoading -> {
                Spacer(modifier = Modifier.height(20.dp))
                Loading()
            }

            uiState.albums.isEmpty() -> NoAlbums(
                modifier = Modifier.weight(1f),
                onCreateAlbum = { onEvent(HomeUiEvent.CreateAlbumClick) },
            )

            uiState.albumFor(target) == null -> NoActiveAlbum(
                modifier = Modifier.weight(1f),
                onChangeAlbum = { onEvent(HomeUiEvent.SelectAlbumClick) },
            )

            else -> {
                Spacer(modifier = Modifier.height(20.dp))

                ActiveAlbum(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.screenPadding),
                    uiState = uiState,
                    target = target,
                    onEvent = onEvent,
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
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
    val failure = config.failure

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WallpaperPlate(
                modifier = Modifier
                    .weight(180f)
                    .aspectRatio(Dimens.DeviceAspectRatio),
                uri = wallpaper?.uri,
                crop = wallpaper?.crop ?: WallpaperCrop.Default,
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
                            onEvent(HomeUiEvent.SelectAlbumClick)
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
                ScheduleValue(config.lastUpdated?.toShortDateTimeString() ?: "—")

                Spacer(modifier = Modifier.height(8.dp))

                ScheduleLabel("NEXT")
                Spacer(modifier = Modifier.height(2.dp))
                ScheduleValue(config.nextUpdate?.toShortDateTimeString() ?: "—")
            }
        }

        if (failure != null) {
            Spacer(modifier = Modifier.height(16.dp))
            StatusRow(
                title = "Last update failed",
                detail = failure.at.toShortDateTimeString(),
                action = "Retry",
                onAction = { onEvent(HomeUiEvent.RetryUpdate(failure.target)) },
            )
            Spacer(modifier = Modifier.height(22.dp))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            HorizontalRule(color = MaterialTheme.wallmatic.ruleSoft)
            Spacer(modifier = Modifier.height(35.dp))
        }

        IntervalPicker(
            value = config.interval,
            onValueChange = { onEvent(HomeUiEvent.IntervalUpdated(it)) },
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

@Composable
private fun NoAlbums(
    modifier: Modifier = Modifier,
    onCreateAlbum: () -> Unit,
) {
    EmptyAlbumScreen(
        modifier = modifier,
        eyebrow = "GET STARTED",
        title = "No album\nyet.",
        description = "An album is a folder of wallpapers. Create one, add a few, and Wallmatic rotates them on your schedule.",
        steps = listOf("Create an album", "Add wallpapers", "Set it active"),
        actionText = "Create album",
        onAction = onCreateAlbum,
    )
}

@Composable
private fun NoActiveAlbum(
    modifier: Modifier = Modifier,
    onChangeAlbum: () -> Unit,
) {
    EmptyAlbumScreen(
        modifier = modifier,
        eyebrow = "GET STARTED",
        title = "No album\nselected.",
        description = "Choose an album to start rotating its wallpapers on your schedule.",
        steps = emptyList(),
        actionText = "Change album",
        onAction = onChangeAlbum,
    )
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
                    config = activeState.config.copy(
                        failure = WallpaperFailure(
                            target = TargetScreen.Home,
                            at = Instant.fromEpochMilliseconds(1_700_000_900_000L),
                        ),
                    ),
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
                    albums = listOf(previewAlbum()),
                    config = WallmaticConfig(
                        interval = 15.minutes,
                        mirrorHomeForLock = false,
                        home = ScreenConfig(albumId = 99),
                        lock = ScreenConfig(albumId = 99),
                    ),
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
    val now = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    return HomeUiState(
        isLoading = false,
        albums = listOf(previewAlbum()),
        config = WallmaticConfig(
            interval = 15.minutes,
            lastUpdated = now,
            mirrorHomeForLock = mirrored,
            home = ScreenConfig(albumId = 1, currentWallpaperId = 1),
            lock = ScreenConfig(albumId = 1, currentWallpaperId = 2),
        ),
        homeWallpaper = Wallpaper(uri = "", albumId = 1).apply { id = 1 },
        lockWallpaper = Wallpaper(uri = "", albumId = 1).apply { id = 2 },
    )
}

private fun previewAlbum() = FullAlbum(
    id = 1,
    name = "Test",
    folders = listOf(
        FullFolder(id = 1, name = "Folder", folderUri = "", wallpapers = emptyList()),
    ),
    wallpapers = (1..24).map { Wallpaper(uri = "", albumId = 1) },
)
