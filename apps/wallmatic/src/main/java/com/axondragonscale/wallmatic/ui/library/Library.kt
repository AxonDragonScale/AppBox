package com.axondragonscale.wallmatic.ui.library

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.ScreenConfig
import com.axondragonscale.wallmatic.model.WallmaticConfig
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.AlbumCard
import com.axondragonscale.wallmatic.ui.component.AlbumNameDialog
import com.axondragonscale.wallmatic.ui.component.EmptyAlbumScreen
import com.axondragonscale.wallmatic.ui.component.Masthead
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.WallmaticFab
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Library(
    modifier: Modifier = Modifier,
    navController: NavController,
    openCreateAlbumDialog: Boolean = false,
    onOpenCreateAlbumDialogHandled: () -> Unit = {},
) {
    val vm: LibraryVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var showCreateAlbumDialog by rememberSaveable { mutableStateOf(false) }
    var renameAlbumDialog by remember { mutableStateOf<Int?>(null) }
    var albumActionsDialog by remember { mutableStateOf<Int?>(null) }

    // Ids, not snapshots: a sync must not leave a stale album next to a live menu.
    val renameAlbum = renameAlbumDialog?.let { id -> uiState.albums.firstOrNull { it.id == id } }
    val actionsAlbum = albumActionsDialog?.let { id -> uiState.albums.firstOrNull { it.id == id } }

    LaunchedEffect(uiState.albums) {
        if (renameAlbumDialog != null && renameAlbum == null) renameAlbumDialog = null
        if (albumActionsDialog != null && actionsAlbum == null) albumActionsDialog = null
    }

    LaunchedEffect(openCreateAlbumDialog) {
        if (openCreateAlbumDialog) {
            showCreateAlbumDialog = true
            onOpenCreateAlbumDialogHandled()
        }
    }

    vm.uiEffect.collectWithLifecycle { uiEffect ->
        when (uiEffect) {
            is LibraryUiEffect.NavigateToAlbum ->
                navController.navigate(route = Route.Album(uiEffect.albumId))
        }
    }

    Library(
        modifier = modifier,
        uiState = uiState,
        onEvent = {
            when (it) {
                is LibraryUiEvent.ShowCreateAlbumDialog ->
                    showCreateAlbumDialog = true

                is LibraryUiEvent.ShowAlbumActionsDialog ->
                    albumActionsDialog = it.album.id

                is LibraryUiEvent.NavigateToAlbum ->
                    navController.navigate(route = Route.Album(it.albumId))

                else -> vm.onEvent(it)
            }
        },
    )

    if (showCreateAlbumDialog) {
        AlbumNameDialog(
            onDismiss = { showCreateAlbumDialog = false },
            onConfirm = { albumName ->
                vm.onEvent(LibraryUiEvent.CreateAlbum(albumName = albumName))
            },
        )
    }

    renameAlbum?.let { album ->
        AlbumNameDialog(
            currentAlbumName = album.name,
            title = "Rename album",
            helper = null,
            onDismiss = { renameAlbumDialog = null },
            onConfirm = { albumName ->
                vm.onEvent(LibraryUiEvent.RenameAlbum(albumId = album.id, albumName = albumName))
            },
        )
    }

    actionsAlbum?.let { album ->
        AlbumActionsOverlay(
            album = album,
            targetLabel = uiState.targetLabelFor(album.id),
            targets = uiState.targetsFor(album.id),
            onSetActive = { target ->
                albumActionsDialog = null
                vm.onEvent(LibraryUiEvent.SetActive(album.id, target))
            },
            onRename = {
                albumActionsDialog = null
                renameAlbumDialog = album.id
            },
            onDelete = {
                albumActionsDialog = null
                vm.onEvent(LibraryUiEvent.DeleteAlbum(album.id))
            },
            onDismiss = { albumActionsDialog = null },
        )
    }
}

@Composable
private fun Library(
    modifier: Modifier = Modifier,
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Loading()

            uiState.albums.isEmpty() -> EmptyAlbumScreen(
                eyebrow = "GET STARTED",
                title = "No albums yet.",
                description = "An album is a folder of wallpapers. Create one to start rotating.",
                steps = listOf("Create an album", "Add wallpapers", "Set it active"),
                actionText = "Create album",
                onAction = { onEvent(LibraryUiEvent.ShowCreateAlbumDialog) },
            )

            else -> AlbumGrid(
                uiState = uiState,
                onAlbumClick = { onEvent(LibraryUiEvent.NavigateToAlbum(it.id)) },
                onAlbumLongClick = { onEvent(LibraryUiEvent.ShowAlbumActionsDialog(it)) },
            )
        }

        if (!uiState.isLoading && uiState.albums.isNotEmpty()) {
            WallmaticFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimens.fabEndInset, bottom = Dimens.fabBottomInset),
                onClick = { onEvent(LibraryUiEvent.ShowCreateAlbumDialog) },
            )
        }
    }
}

@Composable
private fun Loading() {
    StateMessage(
        modifier = Modifier.fillMaxSize(),
        title = "LOADING",
    )
}

@Composable
private fun AlbumGrid(
    modifier: Modifier = Modifier,
    uiState: LibraryUiState,
    onAlbumClick: (FullAlbum) -> Unit,
    onAlbumLongClick: (FullAlbum) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            end = Dimens.screenPadding,
            top = 24.dp,
            bottom = Dimens.gridBottomInset,
        ),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp),
    ) {
        items(uiState.albums, key = { it.id }) { album ->
            AlbumCard(
                title = album.name,
                subtitle = album.countSummary(),
                cover = album.cover,
                target = uiState.targetLabelFor(album.id),
                onClick = { onAlbumClick(album) },
                onLongClick = { onAlbumLongClick(album) },
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun LibraryPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Library")
            Library(
                uiState = previewState(),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun LibraryEmptyPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Library")
            Library(
                uiState = LibraryUiState(isLoading = false),
                onEvent = {},
            )
        }
    }
}

private fun previewState() = LibraryUiState(
    isLoading = false,
    albums = listOf(
        previewAlbum(id = 1, name = "Test", folders = 1, wallpapers = 24),
        previewAlbum(id = 2, name = "Nature", folders = 1, wallpapers = 24),
        previewAlbum(id = 3, name = "City", folders = 1, wallpapers = 12),
        previewAlbum(id = 4, name = "Space", folders = 1, wallpapers = 31),
    ),
    config = WallmaticConfig(
        mirrorHomeForLock = false,
        home = ScreenConfig(albumId = 1),
        lock = ScreenConfig(albumId = 3),
    ),
)

private fun previewAlbum(id: Int, name: String, folders: Int, wallpapers: Int) = FullAlbum(
    id = id,
    name = name,
    folders = List(folders) { index ->
        FullFolder(
            id = index + 1,
            name = "Folder",
            folderUri = "",
            wallpapers = emptyList(),
        )
    },
    wallpapers = List(wallpapers) { Wallpaper(uri = "", albumId = id) },
)
