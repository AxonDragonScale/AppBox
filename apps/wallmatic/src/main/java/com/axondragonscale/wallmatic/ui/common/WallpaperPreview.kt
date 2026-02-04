package com.axondragonscale.wallmatic.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.util.getAspectRatio

/**
 * Created by Ronak Harkhani on 26/09/24
 */

@Composable
fun WallpaperPreview(
    modifier: Modifier = Modifier,
    uri: String?,
    bezelSize: Dp = 2.dp,
    cornerRadius: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(getAspectRatio())
            .border(bezelSize, Color.Black, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        if (uri != null)
            Wallpaper(
                modifier = Modifier.fillMaxSize(),
                uri = uri,
                cornerRadius = cornerRadius,
                contentScale = ContentScale.Crop,
            )
        else
            Text(
                text = "404",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
    }
}
