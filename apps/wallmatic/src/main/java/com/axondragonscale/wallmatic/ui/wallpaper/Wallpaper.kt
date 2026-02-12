package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.axondragonscale.wallmatic.ui.common.EmptyState
import com.axondragonscale.wallmatic.ui.common.LoadingState
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 11/07/24
 */

@Composable
fun Wallpaper(modifier: Modifier = Modifier) {
    val vm: WallpaperVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Wallpaper(
        modifier = modifier,
        uiState = uiState,
        onEvent = { vm.onEvent(it) }
    )
}

@Composable
private fun Wallpaper(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    when {
        uiState.isLoading ->
            LoadingState(message = "Loading wallpaper...")

        uiState.wallpaper == null ->
            EmptyState(
                title = "Wallpaper Not Found",
                description = "This wallpaper may have been deleted or is no longer available"
            )

        else ->
            AsyncImage(
                modifier = modifier.fillMaxSize(),
                model = uiState.wallpaper.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Wallpaper(
                uiState = WallpaperUiState(),
                onEvent = { }
            )
        }
    }
}
