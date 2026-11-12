package com.axondragonscale.wallmatic.ui.album

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.component.ContextMenu
import com.axondragonscale.wallmatic.ui.component.ContextMenuItem
import com.axondragonscale.wallmatic.ui.component.SheetDragHandle
import com.axondragonscale.wallmatic.ui.component.WallmaticBottomSheet
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun AddWallpapersSheet(
    onAddFolder: () -> Unit,
    onAddImages: () -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticBottomSheet(
        height = 280.dp,
        onDismiss = onDismiss,
    ) {
        SheetDragHandle()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Add wallpapers",
            style = WallmaticTextStyles.dialogTitle,
            color = MaterialTheme.wallmatic.ink,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        ContextMenu(
            items = listOf(
                ContextMenuItem(
                    icon = Icons.Filled.Folder,
                    text = "Add folder",
                    action = onAddFolder,
                ),
                ContextMenuItem(
                    icon = Icons.Filled.Image,
                    text = "Add images",
                    action = onAddImages,
                ),
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
