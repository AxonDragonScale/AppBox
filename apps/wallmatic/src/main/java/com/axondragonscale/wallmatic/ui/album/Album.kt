package com.axondragonscale.wallmatic.ui.album

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.ui.component.ContextMenuOverlay
import com.axondragonscale.wallmatic.ui.component.ContextMenuItem
import com.axondragonscale.wallmatic.ui.component.EmptyAlbumLayout
import com.axondragonscale.wallmatic.ui.component.HorizontalRule
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.WallmaticButton
import com.axondragonscale.wallmatic.ui.component.WallmaticFab
import com.axondragonscale.wallmatic.ui.component.WallmaticTopBar
import com.axondragonscale.wallmatic.ui.component.WallpaperTile
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.wallpaper.WallpaperActionsOverlay
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 23/06/24
 */

@Composable
fun Album(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val vm: AlbumVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var folderActionsTarget by remember { mutableStateOf<FullFolder?>(null) }
    var wallpaperActionsTarget by remember { mutableStateOf<Wallpaper?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> uri?.let { vm.onEvent(AlbumUiEvent.FolderSelected(it)) } },
    )
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> if (uris.isNotEmpty()) vm.onEvent(AlbumUiEvent.ImagesSelected(uris)) },
    )

    Album(
        modifier = modifier,
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onPickFolder = { folderPickerLauncher.launch(null) },
        onPickImages = { imagePickerLauncher.launch(arrayOf("image/*")) },
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

    folderActionsTarget?.let { folder ->
        ContextMenuOverlay(
            items = listOf(
                ContextMenuItem(
                    icon = Icons.Outlined.Delete,
                    text = "Delete…",
                    action = {
                        folderActionsTarget = null
                        vm.onEvent(AlbumUiEvent.DeleteFolder(folder.id))
                    },
                ),
            ),
            onDismiss = { folderActionsTarget = null },
        )
    }

    wallpaperActionsTarget?.let { wallpaper ->
        WallpaperActionsOverlay(
            wallpaper = wallpaper,
            onSetWallpaper = { target ->
                wallpaperActionsTarget = null
                vm.onEvent(AlbumUiEvent.SetWallpaper(wallpaper, target))
            },
            onAdjust = { onAdjustTarget ->
                wallpaperActionsTarget = null
                navController.navigate(Route.Wallpaper(onAdjustTarget.id, edit = true))
            },
            onBlacklist = {
                wallpaperActionsTarget = null
                vm.onEvent(AlbumUiEvent.BlacklistWallpaper(wallpaper))
            },
            onWhitelist = {
                wallpaperActionsTarget = null
                vm.onEvent(AlbumUiEvent.WhitelistWallpaper(wallpaper))
            },
            onDismiss = { wallpaperActionsTarget = null },
        )
    }
}

@Composable
private fun Album(
    modifier: Modifier = Modifier,
    uiState: AlbumUiState,
    onBack: () -> Unit,
    onPickFolder: () -> Unit,
    onPickImages: () -> Unit,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.wallmatic.paper)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        WallmaticTopBar(
            title = uiState.album?.name ?: "Album",
            onBack = onBack,
            trailing = uiState.targetLabel?.let { label ->
                {
                    Text(
                        text = "ACTIVE · $label",
                        style = WallmaticTextStyles.microStrong.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp,
                        ),
                        color = MaterialTheme.wallmatic.primary,
                    )
                }
            },
        )

        HorizontalRule(
            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            color = MaterialTheme.wallmatic.ink,
        )

        when {
            uiState.loading -> Loading()

            uiState.album == null -> NotFound()

            !uiState.hasContent -> EmptyAlbum(
                onAddWallpapers = { showAddSheet = true },
            )

            else -> AlbumContent(
                uiState = uiState,
                onAddWallpapers = { showAddSheet = true },
                onEvent = onEvent,
            )
        }
    }

    if (showAddSheet) {
        AddWallpapersSheet(
            onAddFolder = {
                showAddSheet = false
                onPickFolder()
            },
            onAddImages = {
                showAddSheet = false
                onPickImages()
            },
            onDismiss = { showAddSheet = false },
        )
    }
}

@Composable
private fun AlbumContent(
    modifier: Modifier = Modifier,
    uiState: AlbumUiState,
    onAddWallpapers: () -> Unit,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    val album = uiState.album ?: return

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(uiState.gridSize),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = 16.dp,
                bottom = Dimens.gridBottomInset,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                if (album.folders.isNotEmpty() || !uiState.isActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (!uiState.isActive) {
                            ReadyToRotateBanner(
                                targets = uiState.activatableTargets,
                                onSetActive = { target ->
                                    onEvent(AlbumUiEvent.SetActive(target))
                                },
                            )
                        }

                        album.folders.forEach { folder ->
                            FolderCard(
                                folder = folder,
                                onClick = { onEvent(AlbumUiEvent.NavigateToFolder(folder.id)) },
                                onLongClick = { onEvent(AlbumUiEvent.ShowFolderActions(folder)) },
                            )
                        }
                    }
                }
            }

            itemsIndexed(album.wallpapers) { index, wallpaper ->
                WallpaperTile(
                    uri = wallpaper.uri,
                    crop = wallpaper.crop,
                    index = index + 1,
                    onClick = { onEvent(AlbumUiEvent.NavigateToWallpaper(wallpaper.id)) },
                    onLongClick = { onEvent(AlbumUiEvent.ShowWallpaperActions(wallpaper)) },
                )
            }
        }

        WallmaticFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimens.fabEndInset, bottom = Dimens.fabBottomInset),
            onClick = onAddWallpapers,
        )
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
private fun NotFound() {
    StateMessage(
        modifier = Modifier.fillMaxSize(),
        title = "ALBUM NOT FOUND",
        description = "This album may have been deleted.",
    )
}

@Composable
private fun EmptyAlbum(
    modifier: Modifier = Modifier,
    onAddWallpapers: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        EmptyAlbumLayout(
            eyebrow = "EMPTY ALBUM",
            title = "This album is empty.",
            description = "Add wallpapers from your device. You can add more later.",
            steps = emptyList(),
        )

        Spacer(modifier = Modifier.weight(1f))

        WallmaticButton(text = "Add wallpapers", onClick = onAddWallpapers)

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun AlbumPreview() {
    WallmaticTheme {
        Album(
            uiState = AlbumUiState(
                loading = false,
                album = previewAlbum(),
                targetLabel = "HOME",
                isActive = true,
            ),
            onBack = {},
            onPickFolder = {},
            onPickImages = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun AlbumReadyPreview() {
    WallmaticTheme {
        Album(
            uiState = AlbumUiState(
                loading = false,
                album = previewAlbum(),
                activatableTargets = listOf(TargetScreen.Both),
            ),
            onBack = {},
            onPickFolder = {},
            onPickImages = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 851)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 851)
@Composable
private fun AlbumEmptyPreview() {
    WallmaticTheme {
        Album(
            uiState = AlbumUiState(
                loading = false,
                album = FullAlbum(
                    id = 2,
                    name = "Nature",
                    folders = emptyList(),
                    wallpapers = emptyList(),
                ),
            ),
            onBack = {},
            onPickFolder = {},
            onPickImages = {},
            onEvent = {},
        )
    }
}

private fun previewAlbum(): FullAlbum {
    val folderWallpapers = (1..24).map { Wallpaper(uri = "", albumId = 1).apply { id = it } }
    return FullAlbum(
        id = 1,
        name = "Nature",
        folders = listOf(
            FullFolder(
                id = 1,
                name = "Wallpapers",
                folderUri = "",
                wallpapers = folderWallpapers,
            ),
        ),
        wallpapers = (25..28).map { Wallpaper(uri = "", albumId = 1).apply { id = it } },
    )
}
