package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.axondragonscale.wallmatic.model.WallpaperCrop
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun AlbumRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    coverUri: String?,
    coverCrop: WallpaperCrop? = null,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.wallmatic

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(colors.paperHigh)
                .border(1.dp, colors.ink),
        ) {
            if (coverUri != null) {
                AsyncImage(
                    model = wallpaperModel(uri = coverUri, crop = coverCrop),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = WallmaticTextStyles.titleSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    lineHeight = 21.sp,
                ),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = WallmaticTextStyles.caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .border(2.dp, if (selected) colors.primary else colors.inkMuted),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colors.primary)
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlbumRowPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            AlbumRow(
                title = "Test",
                subtitle = "1 Folder · 24 Wallpapers",
                coverUri = null,
                selected = true,
                onClick = {},
            )
            AlbumRow(
                title = "Nature",
                subtitle = "1 Folder · 24 Wallpapers",
                coverUri = null,
                selected = false,
                onClick = {},
            )
        }
    }
}
