package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    coverUri: String?,
    target: String? = null,
    captionPadding: PaddingValues = PaddingValues(top = 6.dp),
    interactive: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.wallmatic

    Column(modifier = modifier.fillMaxWidth()) {
        WallpaperPlate(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(Dimens.TileAspectRatio),
            uri = coverUri,
            interactive = interactive,
            onClick = onClick,
            onLongClick = onLongClick,
        ) {
            if (coverUri.isNullOrBlank()) {
                EmptyPreviewMark(modifier = Modifier.align(Alignment.Center))
            }

            if (target != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .width(40.dp)
                        .height(18.dp)
                        .background(colors.paper)
                        .border(1.dp, colors.ink),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = target.uppercase(Locale.ROOT),
                        style = WallmaticTextStyles.micro.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = colors.ink,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.paper)
                .padding(captionPadding),
        ) {
            Text(
                text = title,
                style = WallmaticTextStyles.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = WallmaticTextStyles.caption.copy(fontSize = 10.sp, lineHeight = 12.sp),
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlbumCardPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            AlbumCard(
                modifier = Modifier.width(178.dp),
                title = "Test",
                subtitle = "1 Folder · 24 Wallpapers",
                coverUri = null,
                target = "Home",
                onClick = {},
            )
        }
    }
}
