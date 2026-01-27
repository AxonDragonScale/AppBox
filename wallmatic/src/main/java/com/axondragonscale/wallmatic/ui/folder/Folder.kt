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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
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
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.common.ContextMenuDialog
import com.axondragonscale.wallmatic.ui.common.ContextMenuItem
import com.axondragonscale.wallmatic.ui.common.TopBar
import com.axondragonscale.wallmatic.ui.common.Wallpaper
import com.axondragonscale.wallmatic.ui.theme.SystemBars
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

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

    SystemBars(statusBarColor = MaterialTheme.colorScheme.primaryContainer)
    Folder(
        modifier = modifier.statusBarsPadding(),
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
    if (uiState.folder == null) return
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

@Composable
fun WallpaperActionsContextMenuDialog(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
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
        contextMenuItems = listOf(
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
        ),
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
