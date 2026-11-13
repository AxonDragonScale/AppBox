package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.model.WallpaperCrop
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun WallpaperTile(
    modifier: Modifier = Modifier,
    uri: String?,
    crop: WallpaperCrop? = null,
    index: Int? = null,
    interactive: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    WallpaperPlate(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(Dimens.TileAspectRatio),
        uri = uri,
        crop = crop,
        interactive = interactive,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        if (index != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .width(32.dp)
                    .height(18.dp)
                    .background(MaterialTheme.wallmatic.paper)
                    .border(1.dp, MaterialTheme.wallmatic.ink),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = String.format(Locale.ROOT, "%02d", index),
                    style = WallmaticTextStyles.micro.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = MaterialTheme.wallmatic.ink,
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WallpaperTilePreview() {
    WallmaticTheme {
        WallpaperTile(
            modifier = Modifier.width(178.dp),
            uri = null,
            index = 1,
            onClick = {},
        )
    }
}
