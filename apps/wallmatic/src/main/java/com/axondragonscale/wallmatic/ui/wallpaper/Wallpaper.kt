package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
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
    if (uiState.wallpaper == null)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "404",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    else
        AsyncImage(
            modifier = modifier.fillMaxSize(),
            model = uiState.wallpaper.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
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
