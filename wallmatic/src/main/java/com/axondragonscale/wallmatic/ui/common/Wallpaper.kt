package com.axondragonscale.wallmatic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.axondragonscale.wallmatic.database.entity.Wallpaper

@Composable
fun Wallpaper(
    modifier: Modifier = Modifier,
    uri: String,
    cornerRadius: Dp = 16.dp,
    contentScale: ContentScale = ContentScale.Fit,
) {
    AsyncImage(
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(modifier),
        model = uri,
        contentScale = contentScale,
        contentDescription = "",
    )
}

@Composable
fun Wallpaper(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
    cornerRadius: Dp = 16.dp,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = wallpaper.uri,
            contentScale = contentScale,
            contentDescription = "",
        )

        if (wallpaper.isBlacklisted) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}
