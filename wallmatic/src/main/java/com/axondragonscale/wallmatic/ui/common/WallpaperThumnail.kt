package com.axondragonscale.wallmatic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 21/09/24
 */

@Composable
fun WallpaperThumbnail(
    modifier: Modifier = Modifier,
    uri: String?,
    size: Dp = 64.dp,
    cornerRadius: Dp = 8.dp,
) {
    if (uri != null)
        Wallpaper(
            modifier = modifier.size(size),
            uri = uri,
            cornerRadius = cornerRadius,
            contentScale = ContentScale.Crop,
        )
    else
        Box(
            modifier = modifier
                .size(size)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(cornerRadius)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "404",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
}
