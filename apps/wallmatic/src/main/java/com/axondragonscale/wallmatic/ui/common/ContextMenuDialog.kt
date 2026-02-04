package com.axondragonscale.wallmatic.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

/**
 * Created by Ronak Harkhani on 05/10/24
 */

data class ContextMenuItem(
    val icon: ImageVector,
    val text: String,
    val action: () -> Unit,
)

@Composable
fun ContextMenuDialog(
    modifier: Modifier = Modifier,
    context: @Composable () -> Unit,
    contextMenuItems: List<ContextMenuItem>,
    onDismiss: () -> Unit,
    maxWidthPercent: Float = 0.6f,
    maxHeightPercent: Float = 0.75f,
) = Dialog(onDismiss) {
    val width = LocalConfiguration.current.screenWidthDp.dp
    val height = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = modifier.sizeIn(
            maxWidth = width * maxWidthPercent,
            maxHeight = height * maxHeightPercent,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val density = LocalDensity.current
        var contextMenuHeight by remember { mutableStateOf(0.dp) }

        Box(
            modifier = Modifier.sizeIn(
                maxWidth = width * maxWidthPercent,
                maxHeight = (height * maxHeightPercent) - 150.dp
            )
        ) {
            context()
        }

        ContextMenu(
            modifier = Modifier
                .onSizeChanged { with(density) { contextMenuHeight = it.height.toDp() } }
                .padding(top = 16.dp),
            contextMenuItems = contextMenuItems,
        )
    }
}

@Composable
private fun ContextMenu(
    modifier: Modifier = Modifier,
    contextMenuItems: List<ContextMenuItem>,
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        contextMenuItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .clickable { item.action() }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .padding(end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = item.text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (index != contextMenuItems.lastIndex)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
        }
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ContextMenuDialog(
                modifier = Modifier,
                context = {
                    Card(modifier = Modifier.size(400.dp, 700.dp)) { }
                },
                contextMenuItems = listOf(
                    ContextMenuItem(
                        icon = Icons.Outlined.Delete,
                        text = "Delete",
                        action = { },
                    ),
                    ContextMenuItem(
                        icon = Icons.Outlined.Edit,
                        text = "Rename",
                        action = { },
                    )
                ),
                onDismiss = { }
            )
        }
    }
}
