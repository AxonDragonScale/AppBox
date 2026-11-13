package com.axondragonscale.wallmatic.ui.albums

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.AlbumCard
import com.axondragonscale.wallmatic.ui.component.AlbumNameDialog
import com.axondragonscale.wallmatic.ui.component.EmptyAlbumLayout
import com.axondragonscale.wallmatic.ui.component.Masthead
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.WallmaticButton
import com.axondragonscale.wallmatic.ui.component.WallmaticFab
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Albums(
    modifier: Modifier = Modifier,
    navController: NavController,
    openCreateAlbumDialog: Boolean = false,
    onOpenCreateAlbumDialogHandled: () -> Unit = {},
) {
    val vm: AlbumsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var showCreateAlbumDialog by rememberSaveable { mutableStateOf(false) }
    var renameAlbumDialog by remember { mutableStateOf<Album?>(null) }
    var albumActionsDialog by remember { mutableStateOf<Album?>(null) }

    LaunchedEffect(openCreateAlbumDialog) {
        if (openCreateAlbumDialog) {
            showCreateAlbumDialog = true
            onOpenCreateAlbumDialogHandled()
        }
    }

    vm.uiEffect.collectWithLifecycle { uiEffect ->
        when (uiEffect) {
            is AlbumsUiEffect.NavigateToAlbum ->
                navController.navigate(route = Route.Album(uiEffect.albumId))
        }
    }

    Albums(
        modifier = modifier,
        uiState = uiState,
        onEvent = {
            when (it) {
                is AlbumsUiEvent.ShowCreateAlbumDialog ->
                    showCreateAlbumDialog = true

                is AlbumsUiEvent.ShowAlbumActionsDialog ->
                    albumActionsDialog = it.album

                is AlbumsUiEvent.NavigateToAlbum ->
                    navController.navigate(route = Route.Album(it.albumId))

                else -> vm.onEvent(it)
            }
        },
    )

    if (showCreateAlbumDialog) {
        AlbumNameDialog(
            onDismiss = { showCreateAlbumDialog = false },
            onConfirm = { albumName ->
                vm.onEvent(AlbumsUiEvent.CreateAlbum(albumName = albumName))
            },
        )
    }

    renameAlbumDialog?.let { album ->
        AlbumNameDialog(
            currentAlbumName = album.name,
            title = "Rename album",
            helper = null,
            onDismiss = { renameAlbumDialog = null },
            onConfirm = { albumName ->
                vm.onEvent(AlbumsUiEvent.RenameAlbum(albumId = album.id, albumName = albumName))
            },
        )
    }

    albumActionsDialog?.let { album ->
        AlbumActionsOverlay(
            album = album,
            targetLabel = uiState.targetLabelFor(album.id),
            coverCrop = uiState.coverCropFor(album),
            activeTargets = uiState.activeTargets[album.id].orEmpty(),
            mirrorEnabled = uiState.mirrorEnabled,
            onSetActive = { target ->
                albumActionsDialog = null
                vm.onEvent(AlbumsUiEvent.SetActive(album.id, target))
            },
            onRename = {
                albumActionsDialog = null
                renameAlbumDialog = album
            },
            onDelete = {
                albumActionsDialog = null
                vm.onEvent(AlbumsUiEvent.DeleteAlbum(album.id))
            },
            onDismiss = { albumActionsDialog = null },
        )
    }
}

@Composable
private fun Albums(
    modifier: Modifier = Modifier,
    uiState: AlbumsUiState,
    onEvent: (AlbumsUiEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Loading()

            uiState.albums.isEmpty() -> LibraryEmpty(
                onCreateAlbum = { onEvent(AlbumsUiEvent.ShowCreateAlbumDialog) },
            )

            else -> AlbumGrid(
                uiState = uiState,
                onAlbumClick = { onEvent(AlbumsUiEvent.NavigateToAlbum(it.id)) },
                onAlbumLongClick = { onEvent(AlbumsUiEvent.ShowAlbumActionsDialog(it)) },
            )
        }

        if (!uiState.isLoading && uiState.albums.isNotEmpty()) {
            WallmaticFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimens.fabEndInset, bottom = Dimens.fabBottomInset),
                onClick = { onEvent(AlbumsUiEvent.ShowCreateAlbumDialog) },
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
    uiState: AlbumsUiState,
    onAlbumClick: (Album) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
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
                coverUri = album.coverUri,
                coverCrop = uiState.coverCropFor(album),
                target = uiState.targetLabelFor(album.id),
                onClick = { onAlbumClick(album) },
                onLongClick = { onAlbumLongClick(album) },
            )
        }
    }
}

@Composable
private fun LibraryEmpty(
    modifier: Modifier = Modifier,
    onCreateAlbum: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        EmptyAlbumLayout(
            eyebrow = "GET STARTED",
            title = "No albums yet.",
            description = "An album is a folder of wallpapers. Create one to start rotating.",
            steps = listOf("Create an album", "Add wallpapers", "Set it active"),
        )

        Spacer(modifier = Modifier.weight(1f))

        WallmaticButton(text = "Create album", onClick = onCreateAlbum)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun AlbumsPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Library")
            Albums(
                uiState = previewState(),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun AlbumsEmptyPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Library")
            Albums(
                uiState = AlbumsUiState(isLoading = false),
                onEvent = {},
            )
        }
    }
}

private fun previewState() = AlbumsUiState(
    isLoading = false,
    albums = listOf(
        Album(name = "Test").apply {
            id = 1
            folderIds = listOf(1)
            wallpaperIds = (1..24).toList()
        },
        Album(name = "Nature").apply {
            id = 2
            folderIds = listOf(2)
            wallpaperIds = (1..24).toList()
        },
        Album(name = "City").apply {
            id = 3
            folderIds = listOf(3)
            wallpaperIds = (1..12).toList()
        },
        Album(name = "Space").apply {
            id = 4
            folderIds = listOf(4)
            wallpaperIds = (1..31).toList()
        },
    ),
    activeTargets = mapOf(
        1 to setOf(TargetScreen.Home),
        3 to setOf(TargetScreen.Lock),
    ),
)
