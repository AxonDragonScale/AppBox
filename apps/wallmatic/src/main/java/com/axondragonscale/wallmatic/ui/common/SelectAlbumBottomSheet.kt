package com.axondragonscale.wallmatic.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.util.countSummary
import com.axondragonscale.wallmatic.ui.util.hasWallpapers

/**
 * Created by Ronak Harkhani on 21/09/24
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAlbumBottomSheet(
    albums: List<Album>,
    selectedAlbumId: Int,
    onSelectAlbum: (Album) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        SelectAlbum(
            albums = albums,
            selectedAlbumId = selectedAlbumId,
            onSelectAlbum = {
                onSelectAlbum(it)
                onDismiss()
            }
        )
    }
}

@Composable
private fun SelectAlbum(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    selectedAlbumId: Int,
    onSelectAlbum: (Album) -> Unit,
) {
    if (albums.isEmpty() || !albums.any { it.hasWallpapers() })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Whoops!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                text = "Looks like you don't have any Albums with wallpapers yet!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    else
        LazyColumn(modifier = modifier) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Select Album",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

            }

            items(albums) {
                ListItem(
                    modifier = Modifier.clickable { onSelectAlbum(it) },
                    leadingContent = {
                        WallpaperThumbnail(uri = it.coverUri)
                    },
                    headlineContent = {
                        Text(
                            text = it.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = it.countSummary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = it.id == selectedAlbumId,
                            onClick = { onSelectAlbum(it) }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ModalBottomSheet(
                sheetState = rememberStandardBottomSheetState(), // Visible in preview
                onDismissRequest = { },
            ) {
                SelectAlbum(
                    albums = listOf(
                        Album(name = "Album 1", coverUri = ""),
                        Album(name = "Album 2", coverUri = ""),
                        Album(name = "Album 3", coverUri = ""),
                    ),
                    selectedAlbumId = 1,
                    onSelectAlbum = { }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmpty() {
    WallmaticTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ModalBottomSheet(
                sheetState = rememberStandardBottomSheetState(), // Visible in preview
                onDismissRequest = { },
            ) {
                SelectAlbum(
                    albums = listOf(),
                    selectedAlbumId = 1,
                    onSelectAlbum = { }
                )
            }
        }
    }
}
