package com.axondragonscale.wallmatic.ui.library

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.component.AlbumCard
import com.axondragonscale.wallmatic.ui.component.ContextMenu
import com.axondragonscale.wallmatic.ui.component.ContextMenuItem
import com.axondragonscale.wallmatic.ui.component.WallmaticOverlay
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** Scrimmed album plate with the actions menu, as drawn in `10 · Context menu`. */
@Composable
fun AlbumActionsOverlay(
    album: FullAlbum,
    targetLabel: String?,
    targets: List<TargetScreen>,
    onSetActive: (TargetScreen) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticOverlay(onDismiss = onDismiss) {
        AlbumActionsContent(
            modifier = Modifier.align(Alignment.Center),
            album = album,
            targetLabel = targetLabel,
            targets = targets,
            onSetActive = onSetActive,
            onRename = onRename,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun AlbumActionsContent(
    modifier: Modifier = Modifier,
    album: FullAlbum,
    targetLabel: String?,
    targets: List<TargetScreen>,
    onSetActive: (TargetScreen) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AlbumCard(
            modifier = Modifier.width(178.dp),
            title = album.name,
            subtitle = album.countSummary(),
            cover = album.cover,
            target = targetLabel,
            captionPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            interactive = false,
            onClick = {},
        )

        ContextMenu(
            modifier = Modifier.width(140.dp),
            items = buildList {
                add(
                    ContextMenuItem(
                        icon = Icons.Outlined.Edit,
                        text = "Rename",
                        action = onRename,
                    ),
                )
                targets.forEach { target ->
                    add(
                        ContextMenuItem(
                            icon = when (target) {
                                TargetScreen.Home -> Icons.Filled.Home
                                TargetScreen.Lock -> Icons.Filled.Lock
                                TargetScreen.Both -> Icons.Filled.CheckCircle
                            },
                            text = when (target) {
                                TargetScreen.Home -> "Set Home"
                                TargetScreen.Lock -> "Set Lock"
                                TargetScreen.Both -> "Set active"
                            },
                            action = { onSetActive(target) },
                        ),
                    )
                }
                add(
                    ContextMenuItem(
                        icon = Icons.Outlined.Delete,
                        text = "Delete",
                        action = onDelete,
                    ),
                )
            },
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun AlbumActionsPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.wallmatic.paper),
            contentAlignment = Alignment.Center,
        ) {
            AlbumActionsContent(
                album = previewAlbum(),
                targetLabel = null,
                targets = listOf(TargetScreen.Both),
                onSetActive = {},
                onRename = {},
                onDelete = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun AlbumActionsMirrorOffPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.wallmatic.paper),
            contentAlignment = Alignment.Center,
        ) {
            AlbumActionsContent(
                album = previewAlbum(),
                targetLabel = "HOME",
                targets = listOf(TargetScreen.Lock),
                onSetActive = {},
                onRename = {},
                onDelete = {},
            )
        }
    }
}

private fun previewAlbum() = FullAlbum(
    id = 1,
    name = "Test",
    folders = listOf(
        FullFolder(id = 1, name = "Folder", folderUri = "", wallpapers = emptyList()),
    ),
    wallpapers = (1..24).map { Wallpaper(uri = "", albumId = 1) },
)
