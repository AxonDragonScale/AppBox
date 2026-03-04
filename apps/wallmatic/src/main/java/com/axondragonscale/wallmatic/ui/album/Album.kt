package com.axondragonscale.wallmatic.ui.album

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.common.ContextMenuDialog
import com.axondragonscale.wallmatic.ui.common.ContextMenuItem
import com.axondragonscale.wallmatic.ui.common.EmptyState
import com.axondragonscale.wallmatic.ui.common.FluidFabButton
import com.axondragonscale.wallmatic.ui.common.FluidFabButtonProperties
import com.axondragonscale.wallmatic.ui.common.LoadingState
import com.axondragonscale.wallmatic.ui.common.TopBar
import com.axondragonscale.wallmatic.ui.common.Wallpaper
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.util.getAspectRatio
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 23/06/24
 */

@Composable
fun Album(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    albumId: Int,
) {
    val vm: AlbumVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var folderActionsTarget by remember { mutableStateOf<FullFolder?>(null) }
    var wallpaperActionsTarget by remember { mutableStateOf<Wallpaper?>(null) }

    Album(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is AlbumUiEvent.NavigateToFolder ->
                    navController.navigate(Route.Folder(event.folderId))
                is AlbumUiEvent.NavigateToWallpaper ->
                    navController.navigate(Route.Wallpaper(event.wallpaperId))
                is AlbumUiEvent.ShowFolderActions ->
                    folderActionsTarget = event.folder
                is AlbumUiEvent.ShowWallpaperActions ->
                    wallpaperActionsTarget = event.wallpaper

                else -> vm.onEvent(event)
            }
        },
    )

    folderActionsTarget?.let {
        FolderActionsContextMenuDialog(
            folder = it,
            onDelete = { vm.onEvent(AlbumUiEvent.DeleteFolder(it.id)) },
            onDismiss = { folderActionsTarget = null },
        )
    }

    wallpaperActionsTarget?.let {
        WallpaperActionsContextMenuDialog(
            wallpaper = it,
            onSetWallpaper = { target -> vm.onEvent(AlbumUiEvent.SetWallpaper(it, target)) },
            onDelete = { vm.onEvent(AlbumUiEvent.DeleteWallpaper(it.id)) },
            onDismiss = { wallpaperActionsTarget = null },
        )
    }

}

@Composable
private fun Album(
    modifier: Modifier = Modifier,
    uiState: AlbumUiState,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.loading ->
                LoadingState(message = "Loading album...")

            uiState.album == null ->
                EmptyState(
                    title = "Album Not Found",
                    description = "This album may have been deleted"
                )

            uiState.album.folders.isEmpty() && uiState.album.wallpapers.isEmpty() ->
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        title = "Album",
                        subtitle = uiState.album.name,
                    )
                    EmptyState(
                        title = "No Wallpapers Yet",
                        description = "Add folders or images to start building your collection"
                    )
                }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        title = "Album",
                        subtitle = uiState.album.name,
                    )

                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = StaggeredGridCells.Fixed(uiState.gridSize),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (folder in uiState.album.folders) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Folder(
                                    folder = folder,
                                    enabled = true,
                                    onClick = { onEvent(AlbumUiEvent.NavigateToFolder(folder.id)) },
                                    onLongClick = { onEvent(AlbumUiEvent.ShowFolderActions(folder)) },
                                    onWallpaperClick = { onEvent(AlbumUiEvent.NavigateToWallpaper(it)) }
                                )
                            }
                        }

                        items(uiState.album.wallpapers) {
                            Wallpaper(
                                modifier = Modifier.combinedClickable(
                                    onClick = { onEvent(AlbumUiEvent.NavigateToWallpaper(it.id)) },
                                    onLongClick = { onEvent(AlbumUiEvent.ShowWallpaperActions(it)) }
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

        if (!uiState.loading && uiState.album != null)
            PickerButton(
                modifier = Modifier.navigationBarsPadding(),
                onFolderSelected = { onEvent(AlbumUiEvent.FolderSelected(it)) },
                onImagesSelected = { onEvent(AlbumUiEvent.ImagesSelected(it)) },
            )
    }
}

@Composable
private fun Folder(
    modifier: Modifier = Modifier,
    folder: FullFolder,
    enabled: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onWallpaperClick: (wallpaperId: Int) -> Unit = {},
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
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = folder.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${folder.wallpapers.size} wallpapers",
                style = MaterialTheme.typography.labelSmall
            )
        }
        LazyRow(
            modifier = Modifier.height(300.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(folder.wallpapers) {
                // Height + Aspect Ratio = Fixed Size = Better Perf
                Wallpaper(
                    modifier = Modifier
                        .aspectRatio(getAspectRatio())
                        .clickable(enabled) { onWallpaperClick(it.id) },
                    wallpaper = it,
                    cornerRadius = 8.dp,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PickerButton(
    modifier: Modifier = Modifier,
    onFolderSelected: (Uri) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> if (uri != null) onFolderSelected(uri) }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> if (uris.isNotEmpty()) onImagesSelected(uris) }
    )

//    // Photo Picker. Provided by Photos app. Albums available.
//    val photoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickMultipleVisualMedia(),
//        onResult = {
//            println("zeref $it")
//        }
//    )

    FluidFabButton(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp),
        isExpanded = isExpanded,
        duration = 500,
        onClick = { isExpanded = !isExpanded },
        leftButton = FluidFabButtonProperties(
            icon = Icons.Default.Folder,
            onClick = {
                isExpanded = !isExpanded
                folderPickerLauncher.launch(null)
            }
        ),
        rightButton = FluidFabButtonProperties(
            icon = Icons.Default.Image,
            onClick = {
                isExpanded = !isExpanded
                imagePickerLauncher.launch(arrayOf("image/*"))
            }
        ),
    )
}

@Composable
private fun FolderActionsContextMenuDialog(
    modifier: Modifier = Modifier,
    folder: FullFolder,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ContextMenuDialog(
        modifier = modifier,
        context = {
          Folder(folder = folder)
        },
        contextMenuItems = listOf(
            ContextMenuItem(
                icon = Icons.Default.Delete,
                text = "Delete",
                action = {
                    onDelete()
                    onDismiss()
                },
            )
        ),
        onDismiss = onDismiss,
        maxWidthPercent = 0.8f,
    )
}

@Composable
fun WallpaperActionsContextMenuDialog(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
    onSetWallpaper: (TargetScreen) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ContextMenuDialog(
        modifier = modifier,
        context = {
            Wallpaper(
                wallpaper = wallpaper,
                cornerRadius = 12.dp,
            )
        },
        contextMenuItems = listOf(
            ContextMenuItem(
                icon = Icons.Default.Home,
                text = "Set Home Screen",
                action = {
                    onSetWallpaper(TargetScreen.Home)
                    onDismiss()
                },
            ),
            ContextMenuItem(
                icon = Icons.Default.Lock,
                text = "Set Lock Screen",
                action = {
                    onSetWallpaper(TargetScreen.Lock)
                    onDismiss()
                },
            ),
            ContextMenuItem(
                icon = Icons.Default.Wallpaper,
                text = "Set Both",
                action = {
                    onSetWallpaper(TargetScreen.Both)
                    onDismiss()
                },
            ),
            ContextMenuItem(
                icon = Icons.Default.Delete,
                text = "Delete",
                action = {
                    onDelete()
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
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember {
                AlbumUiState(
                    loading = false,
                    album = FullAlbum(
                        id = 1,
                        name = "Album Name",
                        coverUri = null,
                        folders = listOf(
                            FullFolder(
                                id = 1,
                                name = "Folder 1",
                                coverUri = null,
                                folderUri = "null",
                                wallpapers = listOf(
                                    Wallpaper(""),
                                    Wallpaper(""),
                                    Wallpaper(""),
                                    Wallpaper(""),
                                ),
                            )
                        ),
                        wallpapers = listOf(
                            Wallpaper(""),
                            Wallpaper(""),
                            Wallpaper(""),
                        ),
                    )
                )
            }
            Album(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoading() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val uiState = remember { AlbumUiState() }
            Album(
                uiState = uiState,
                onEvent = { }
            )
        }
    }
}
