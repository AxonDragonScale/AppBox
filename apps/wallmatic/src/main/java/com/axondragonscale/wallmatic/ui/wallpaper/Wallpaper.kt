package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 11/07/24
 */

@Composable
fun Wallpaper(modifier: Modifier = Modifier) {
    val vm: WallpaperVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Wallpaper(modifier = modifier, uiState = uiState)
}

@Composable
private fun Wallpaper(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
) {
    when {
        uiState.isLoading -> Message("LOADING")

        uiState.wallpaper == null -> Message(
            title = "WALLPAPER NOT FOUND",
            description = "This wallpaper may have been deleted.",
        )

        else -> AsyncImage(
            modifier = modifier.fillMaxSize(),
            model = uiState.wallpaper.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun Message(
    title: String,
    description: String? = null,
) {
    StateMessage(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.wallmatic.paper),
        title = title,
        description = description,
    )
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 915)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 915)
@Composable
private fun WallpaperPreview() {
    WallmaticTheme {
        Wallpaper(uiState = WallpaperUiState(isLoading = false))
    }
}
