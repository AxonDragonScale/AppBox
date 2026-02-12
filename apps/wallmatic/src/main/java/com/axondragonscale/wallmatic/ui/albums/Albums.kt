package com.axondragonscale.wallmatic.ui.albums

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.bottombar.BOTTOM_BAR_HEIGHT
import com.axondragonscale.wallmatic.ui.common.AlbumNameDialog
import com.axondragonscale.wallmatic.ui.common.ContextMenuDialog
import com.axondragonscale.wallmatic.ui.common.ContextMenuItem
import com.axondragonscale.wallmatic.ui.common.TabHeader
import com.axondragonscale.wallmatic.ui.common.Wallpaper
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.compose.navigationBarsHeight
import com.axondragonscale.wallmatic.ui.util.countSummary

/**
 * Created by Ronak Harkhani on 09/06/24
 */

@Composable
fun Albums(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: AlbumsVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var renameAlbumDialog by remember { mutableStateOf<Album?>(null) }
    var albumActionsDialog by remember { mutableStateOf<Album?>(null) }

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

    renameAlbumDialog?.let {
        AlbumNameDialog(
            currentAlbumName = it.name,
            onDismiss = { renameAlbumDialog = null },
            onConfirm = { albumName ->
                vm.onEvent(AlbumsUiEvent.RenameAlbum(albumId = it.id, albumName = albumName))
            },
        )
    }

    albumActionsDialog?.let { album ->
        AlbumActionsContextMenuDialog(
            album = album,
            onRenameClick = { renameAlbumDialog = album },
            onDeleteClick = { vm.onEvent(AlbumsUiEvent.DeleteAlbum(album.id)) },
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
        AlbumList(
            albums = uiState.albums,
            onAlbumClick = { onEvent(AlbumsUiEvent.NavigateToAlbum(it.id)) },
            onAlbumLongClick = { onEvent(AlbumsUiEvent.ShowAlbumActionsDialog(it)) }
        )

        CreateAlbumButton(
            onCreateClick = { onEvent(AlbumsUiEvent.ShowCreateAlbumDialog) }
        )
    }
}

@Composable
private fun BoxScope.CreateAlbumButton(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier
            .align(BottomCenter)
            .padding(bottom = BOTTOM_BAR_HEIGHT + 24.dp),
        shape = CircleShape,
        onClick = onCreateClick,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = ""
        )
    }
}

@Composable
private fun AlbumList(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier.padding(horizontal = 8.dp),
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(top = 16.dp, bottom = BOTTOM_BAR_HEIGHT + navigationBarsHeight() + 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            TabHeader(
                modifier = Modifier.padding(start = 8.dp, top = 32.dp, bottom = 8.dp),
                text = "Albums"
            )
        }

        items(albums) { album ->
            AlbumCard(
                album = album,
                enabled = true,
                onClick = { onAlbumClick(album) },
                onLongClick = { onAlbumLongClick(album) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    album: Album,
    enabled: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = album.countSummary(),
                style = MaterialTheme.typography.labelSmall
            )
        }

        album.coverUri?.let {
            Wallpaper(
                uri = it,
                cornerRadius = 12.dp,
            )
        } ?: Box(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth(),
            contentAlignment = Center
        ) {
            Text(
                text = "404",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun AlbumActionsContextMenuDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    album: Album,
) {
    ContextMenuDialog(
        modifier = modifier,
        context = {
            AlbumCard(album = album)
        },
        contextMenuItems = listOf(
            ContextMenuItem(
                icon = Icons.Outlined.Edit,
                text = "Rename",
                action = {
                    onDismiss()
                    onRenameClick()
                },
            ),
            ContextMenuItem(
                icon = Icons.Outlined.Delete,
                text = "Delete",
                action = {
                    onDismiss()
                    onDeleteClick()
                },
            )
        ),
        onDismiss = onDismiss,
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember {
                val albums = List(21) { Album("Album $it") }
                AlbumsUiState(albums = albums)
            }
            Albums(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}
