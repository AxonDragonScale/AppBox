package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.crop
import com.axondragonscale.wallmatic.ui.component.ContextMenu
import com.axondragonscale.wallmatic.ui.component.ContextMenuItem
import com.axondragonscale.wallmatic.ui.component.WallpaperTile
import com.axondragonscale.wallmatic.ui.component.WallmaticOverlay
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun WallpaperActionsOverlay(
    wallpaper: Wallpaper,
    onSetWallpaper: (TargetScreen) -> Unit,
    onAdjust: (Wallpaper) -> Unit,
    onBlacklist: () -> Unit,
    onWhitelist: () -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticOverlay(onDismiss = onDismiss) {
        WallpaperActionsContent(
            modifier = Modifier.align(Alignment.Center),
            wallpaper = wallpaper,
            onSetWallpaper = onSetWallpaper,
            onAdjust = onAdjust,
            onBlacklist = onBlacklist,
            onWhitelist = onWhitelist,
        )
    }
}

@Composable
private fun WallpaperActionsContent(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
    onSetWallpaper: (TargetScreen) -> Unit,
    onAdjust: (Wallpaper) -> Unit,
    onBlacklist: () -> Unit,
    onWhitelist: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WallpaperTile(
            modifier = Modifier.width(178.dp),
            uri = wallpaper.uri,
            crop = wallpaper.crop,
            interactive = false,
            onClick = {},
        )

        ContextMenu(
            modifier = Modifier.width(140.dp),
            compact = true,
            items = listOf(
                ContextMenuItem(
                    icon = Icons.Filled.Home,
                    text = "Set Home",
                    action = { onSetWallpaper(TargetScreen.Home) },
                ),
                ContextMenuItem(
                    icon = Icons.Filled.Lock,
                    text = "Set Lock",
                    action = { onSetWallpaper(TargetScreen.Lock) },
                ),
                ContextMenuItem(
                    icon = Icons.Filled.Wallpaper,
                    text = "Set Both",
                    action = { onSetWallpaper(TargetScreen.Both) },
                ),
                ContextMenuItem(
                    icon = Icons.Filled.Crop,
                    text = "Adjust",
                    action = { onAdjust(wallpaper) },
                ),
                if (wallpaper.isBlacklisted) {
                    ContextMenuItem(
                        icon = Icons.Filled.CheckCircle,
                        text = "Whitelist",
                        action = onWhitelist,
                    )
                } else {
                    ContextMenuItem(
                        icon = Icons.Filled.Block,
                        text = "Blacklist",
                        action = onBlacklist,
                    )
                },
            ),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 787)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 787)
@Composable
private fun WallpaperActionsPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.wallmatic.paper),
            contentAlignment = Alignment.Center,
        ) {
            WallpaperActionsContent(
                wallpaper = Wallpaper(uri = "").apply { id = 1 },
                onSetWallpaper = {},
                onAdjust = {},
                onBlacklist = {},
                onWhitelist = {},
            )
        }
    }
}
