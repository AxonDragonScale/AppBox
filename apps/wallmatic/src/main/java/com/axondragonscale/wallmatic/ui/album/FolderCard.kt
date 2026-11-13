package com.axondragonscale.wallmatic.ui.album

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.WallpaperCrop
import com.axondragonscale.wallmatic.ui.component.WallpaperPlate
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun FolderCard(
    modifier: Modifier = Modifier,
    folder: FullFolder,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = MaterialTheme.wallmatic

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.paperLow)
            .border(1.dp, colors.ink)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = folder.name,
                style = WallmaticTextStyles.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = folder.countSummary(),
                style = WallmaticTextStyles.caption,
                color = colors.inkMuted,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            val slots = maxOf(folder.wallpapers.size, 3)
            items(slots) { index ->
                val wallpaper = folder.wallpapers.getOrNull(index)
                FolderStrip(
                    modifier = Modifier
                        .width(108.dp)
                        .height(240.dp),
                    uri = wallpaper?.uri,
                    crop = wallpaper?.crop ?: WallpaperCrop.Default,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
    }
}

@Composable
private fun FolderStrip(
    modifier: Modifier = Modifier,
    uri: String?,
    crop: WallpaperCrop,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    WallpaperPlate(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        uri = uri,
        crop = crop,
        interactive = false,
    )
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 360)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 360)
@Composable
private fun FolderCardPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
        ) {
            FolderCard(
                folder = FullFolder(
                    id = 1,
                    name = "Wallpapers",
                    folderUri = "",
                    wallpapers = (1..3).map { Wallpaper(uri = "", albumId = 1).apply { id = it } },
                ),
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
