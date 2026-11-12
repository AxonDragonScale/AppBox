package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** Scrim plus a square context menu, for actions that do not need a preview plate. */
@Composable
fun ContextMenuOverlay(
    modifier: Modifier = Modifier,
    items: List<ContextMenuItem>,
    onDismiss: () -> Unit,
) {
    WallmaticOverlay(onDismiss = onDismiss) {
        ContextMenu(
            modifier = modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .width(140.dp),
            items = items,
        )
    }
}
