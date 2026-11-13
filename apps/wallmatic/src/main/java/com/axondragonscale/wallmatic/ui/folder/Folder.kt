package com.axondragonscale.wallmatic.ui.folder

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.EmptyStateCard
import com.axondragonscale.wallmatic.ui.component.HorizontalRule
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.WallpaperTile
import com.axondragonscale.wallmatic.ui.component.WallmaticTopBar
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.wallpaper.WallpaperActionsOverlay
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 17/09/24
 */

@Composable
fun Folder(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val vm: FolderVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var wallpaperActionsTarget by remember { mutableStateOf<Wallpaper?>(null) }

    Folder(
        modifier = modifier,
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onEvent = { event ->
            when (event) {
                is FolderUiEvent.NavigateToWallpaper ->
                    navController.navigate(Route.Wallpaper(event.wallpaperId))
                is FolderUiEvent.ShowWallpaperActions ->
                    wallpaperActionsTarget = event.wallpaper

                else -> vm.onEvent(event)
            }
        },
    )

    wallpaperActionsTarget?.let { wallpaper ->
        WallpaperActionsOverlay(
            wallpaper = wallpaper,
            onSetWallpaper = { target ->
                wallpaperActionsTarget = null
                vm.onEvent(FolderUiEvent.SetWallpaper(wallpaper, target))
            },
            onAdjust = { onAdjustTarget ->
                wallpaperActionsTarget = null
                navController.navigate(Route.Wallpaper(onAdjustTarget.id, edit = true))
            },
            onBlacklist = {
                wallpaperActionsTarget = null
                vm.onEvent(FolderUiEvent.BlacklistWallpaper(wallpaper))
            },
            onWhitelist = {
                wallpaperActionsTarget = null
                vm.onEvent(FolderUiEvent.WhitelistWallpaper(wallpaper))
            },
            onDismiss = { wallpaperActionsTarget = null },
        )
    }
}

@Composable
private fun Folder(
    modifier: Modifier = Modifier,
    uiState: FolderUiState,
    onBack: () -> Unit,
    onEvent: (FolderUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.wallmatic.paper)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        WallmaticTopBar(
            title = uiState.folder?.name ?: "Folder",
            onBack = onBack,
        )

        HorizontalRule(
            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            color = MaterialTheme.wallmatic.ink,
        )

        when {
            uiState.isLoading -> Loading()

            uiState.folder == null -> NotFound()

            uiState.folder.wallpapers.isEmpty() -> EmptyFolder()

            else -> FolderGrid(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun FolderGrid(
    modifier: Modifier = Modifier,
    uiState: FolderUiState,
    onEvent: (FolderUiEvent) -> Unit,
) {
    val folder = uiState.folder ?: return

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(uiState.gridSize),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            end = Dimens.screenPadding,
            top = 32.dp,
            bottom = 32.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        itemsIndexed(folder.wallpapers) { index, wallpaper ->
            WallpaperTile(
                uri = wallpaper.uri,
                crop = wallpaper.crop,
                index = index + 1,
                onClick = { onEvent(FolderUiEvent.NavigateToWallpaper(wallpaper.id)) },
                onLongClick = { onEvent(FolderUiEvent.ShowWallpaperActions(wallpaper)) },
            )
        }
    }
}

@Composable
private fun Loading() {
    CenteredMessage(title = "LOADING")
}

@Composable
private fun NotFound() {
    CenteredMessage(
        title = "FOLDER NOT FOUND",
        description = "This folder may have been deleted.",
    )
}

@Composable
private fun EmptyFolder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyStateCard(
            modifier = Modifier.padding(Dimens.screenPadding),
            title = "No wallpapers",
            description = "This folder doesn't contain any images.",
        )
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    description: String? = null,
) {
    StateMessage(
        modifier = Modifier.fillMaxSize(),
        title = title,
        description = description,
    )
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun FolderPreview() {
    WallmaticTheme {
        Folder(
            uiState = FolderUiState(
                isLoading = false,
                folder = previewFolder(),
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun FolderEmptyPreview() {
    WallmaticTheme {
        Folder(
            uiState = FolderUiState(
                isLoading = false,
                folder = FullFolder(
                    id = 1,
                    name = "Wallpapers",
                    folderUri = "",
                    wallpapers = emptyList(),
                ),
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

private fun previewFolder() = FullFolder(
    id = 1,
    name = "Wallpapers",
    folderUri = "",
    wallpapers = (1..8).map { Wallpaper(uri = "", albumId = 1).apply { id = it } },
)
