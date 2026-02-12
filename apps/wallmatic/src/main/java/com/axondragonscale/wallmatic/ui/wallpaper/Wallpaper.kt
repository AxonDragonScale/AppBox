package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

        uiState.isEditMode -> {
            EditModeContent(
                modifier = modifier,
                uiState = uiState,
                onEvent = onEvent
            )
        }

        else -> {
            NormalModeContent(
                modifier = modifier,
                uiState = uiState,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun NormalModeContent(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = uiState.wallpaper?.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        FloatingActionButton(
            onClick = { onEvent(WallpaperUiEvent.ToggleEditMode) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeContent(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background wallpaper - fullscreen with rotation applied
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = uiState.tempRotation.toFloat() },
            model = uiState.wallpaper?.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        // Crop overlay - fullscreen
        CropOverlay(
            imageSize = uiState.imageSize,
            cropRect = uiState.tempCropRect,
            onCropChange = { onEvent(WallpaperUiEvent.UpdateCrop(it)) }
        )

        // Top bar overlay
        TopAppBar(
            title = { Text("Edit Wallpaper") },
            navigationIcon = {
                IconButton(onClick = { onEvent(WallpaperUiEvent.CancelEdits) }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            },
            actions = {
                IconButton(onClick = { onEvent(WallpaperUiEvent.SaveEdits) }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.7f),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        // Bottom controls overlay
        EditControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            rotation = uiState.tempRotation,
            onEvent = onEvent
        )
    }
}

@Composable
private fun EditControls(
    modifier: Modifier = Modifier,
    rotation: Int,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rotation: ${rotation}°",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { onEvent(WallpaperUiEvent.RotateCounterClockwise) }
            ) {
                Text("⟲ 90°")
            }

            Button(
                onClick = { onEvent(WallpaperUiEvent.ResetEdits) }
            ) {
                Text("Reset")
            }

            OutlinedButton(
                onClick = { onEvent(WallpaperUiEvent.RotateClockwise) }
            ) {
                Text("⟳ 90°")
            }
        }
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
