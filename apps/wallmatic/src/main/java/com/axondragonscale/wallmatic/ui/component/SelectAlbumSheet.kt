package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import com.axondragonscale.wallmatic.ui.util.countSummary

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun SelectAlbumSheet(
    modifier: Modifier = Modifier,
    albums: List<FullAlbum>,
    selectedAlbumId: Int?,
    onSelectAlbum: (FullAlbum) -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticBottomSheet(
        height = 420.dp,
        onDismiss = onDismiss,
    ) {
        SelectAlbumContent(
            modifier = modifier,
            albums = albums,
            selectedAlbumId = selectedAlbumId,
            onSelectAlbum = onSelectAlbum,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun SelectAlbumContent(
    modifier: Modifier = Modifier,
    albums: List<FullAlbum>,
    selectedAlbumId: Int?,
    onSelectAlbum: (FullAlbum) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(modifier = modifier) {
        SheetDragHandle()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Album",
            style = WallmaticTextStyles.dialogTitle,
            color = MaterialTheme.wallmatic.ink,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
            items(albums) { album ->
                AlbumRow(
                    title = album.name,
                    subtitle = album.countSummary(),
                    cover = album.cover,
                    selected = album.id == selectedAlbumId,
                    onClick = {
                        onSelectAlbum(album)
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 420)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 420)
@Composable
private fun SelectAlbumSheetPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(MaterialTheme.wallmatic.paper)
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        ) {
            SelectAlbumContent(
                albums = listOf(
                    FullAlbum(id = 1, name = "Test", folders = emptyList(), wallpapers = emptyList()),
                    FullAlbum(id = 2, name = "Nature", folders = emptyList(), wallpapers = emptyList()),
                ),
                selectedAlbumId = 1,
                onSelectAlbum = {},
                onDismiss = {},
            )
        }
    }
}
