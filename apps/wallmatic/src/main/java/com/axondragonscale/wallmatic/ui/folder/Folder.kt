package com.axondragonscale.wallmatic.ui.folder

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.common.ContextMenuDialog
import com.axondragonscale.wallmatic.ui.common.ContextMenuItem
import com.axondragonscale.wallmatic.ui.common.EmptyState
import com.axondragonscale.wallmatic.ui.common.LoadingState
import com.axondragonscale.wallmatic.ui.common.TopBar
import com.axondragonscale.wallmatic.ui.common.Wallpaper
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 17/09/24
 */

@Composable
fun Folder(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    folderId: Int,
) {
    val vm: FolderVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var wallpaperActionsTarget by remember { mutableStateOf<Wallpaper?>(null) }

    Folder(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is FolderUiEvent.NavigateToWallpaper ->
                    navController.navigate(Route.Wallpaper(event.wallpaperId))
                is FolderUiEvent.ShowWallpaperActions ->
                    wallpaperActionsTarget = event.wallpaper

                else -> vm.onEvent(event)
            }
        }
    )

    wallpaperActionsTarget?.let {
        WallpaperActionsContextMenuDialog(
            wallpaper = it,
            onSetWallpaper = { target -> vm.onEvent(FolderUiEvent.SetWallpaper(it, target)) },
            onBlacklist = { vm.onEvent(FolderUiEvent.BlacklistWallpaper(it)) },
            onWhitelist = { vm.onEvent(FolderUiEvent.WhitelistWallpaper(it)) },
            onDismiss = { wallpaperActionsTarget = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Folder(
    modifier: Modifier = Modifier,
    uiState: FolderUiState,
    onEvent: (FolderUiEvent) -> Unit,
) {
    when {
        uiState.isLoading ->
            LoadingState(message = "Loading folder...")

        uiState.folder == null ->
            EmptyState(
                title = "Folder Not Found",
                description = "This folder may have been deleted"
            )

        uiState.folder.wallpapers.isEmpty() ->
            Column(modifier = modifier.fillMaxSize()) {
                TopBar(
                    title = "Folder",
                    subtitle = uiState.folder.name
                )
                EmptyState(
                    title = "No Wallpapers",
                    description = "This folder doesn't contain any images"
                )
            }

        else ->
            Column(modifier = modifier.fillMaxSize()) {
                TopBar(
                    title = "Folder",
                    subtitle = uiState.folder.name
                )

                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Fixed(uiState.gridSize),
                    contentPadding = PaddingValues(8.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.folder.wallpapers) {
                        Wallpaper(
                            modifier = Modifier.combinedClickable(
                                onClick = { onEvent(FolderUiEvent.NavigateToWallpaper(it.id)) },
                                onLongClick = { onEvent(FolderUiEvent.ShowWallpaperActions(it)) }
                            ),
                            wallpaper = it,
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
    }
}

@Composable
fun WallpaperActionsContextMenuDialog(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
    onSetWallpaper: (TargetScreen) -> Unit,
    onBlacklist: () -> Unit,
    onWhitelist: () -> Unit,
    onDismiss: () -> Unit,
) {
    ContextMenuDialog(
        modifier = modifier,
        context = {
            Wallpaper(
                modifier = Modifier,
                uri = wallpaper.uri,
                cornerRadius = 12.dp,
            )
        },
        contextMenuItems = buildList {
            add(
                ContextMenuItem(
                    icon = Icons.Default.Home,
                    text = "Set Home Screen",
                    action = {
                        onSetWallpaper(TargetScreen.Home)
                        onDismiss()
                    },
                )
            )
            add(
                ContextMenuItem(
                    icon = Icons.Default.Lock,
                    text = "Set Lock Screen",
                    action = {
                        onSetWallpaper(TargetScreen.Lock)
                        onDismiss()
                    },
                )
            )
            add(
                ContextMenuItem(
                    icon = Icons.Default.Wallpaper,
                    text = "Set Both",
                    action = {
                        onSetWallpaper(TargetScreen.Both)
                        onDismiss()
                    },
                )
            )
            add(
                if (wallpaper.isBlacklisted)
                    ContextMenuItem(
                        icon = Icons.Outlined.CheckCircle,
                        text = "Whitelist",
                        action = {
                            onWhitelist()
                            onDismiss()
                        },
                    )
                else
                    ContextMenuItem(
                        icon = Icons.Default.Block,
                        text = "Blacklist",
                        action = {
                            onBlacklist()
                            onDismiss()
                        },
                    )
            )
        },
        onDismiss = onDismiss,
    )
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoading() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember { FolderUiState() }
            Folder(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}
