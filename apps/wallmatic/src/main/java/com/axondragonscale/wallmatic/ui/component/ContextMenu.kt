package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

data class ContextMenuItem(
    val icon: ImageVector,
    val text: String,
    val destructive: Boolean = false,
    val action: () -> Unit,
)

@Composable
fun ContextMenu(
    modifier: Modifier = Modifier,
    items: List<ContextMenuItem>,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.wallmatic

    Column(
        modifier = modifier
            .background(colors.paper)
            .border(1.dp, colors.ink),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    HorizontalRule(color = colors.ruleSoft)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 44.dp else 48.dp)
                    .clickable { item.action() }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.destructive) colors.error else colors.ink,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.text,
                    style = WallmaticTextStyles.microStrong.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = if (item.destructive) colors.error else colors.ink,
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContextMenuPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            ContextMenu(
                items = listOf(
                    ContextMenuItem(Icons.Outlined.Edit, "Rename", action = {}),
                    ContextMenuItem(Icons.Outlined.Delete, "Delete", destructive = true, action = {}),
                ),
            )
        }
    }
}
