package com.axondragonscale.topty.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.topty.ui.theme.ToptyTheme

/**
 * Created by Ronak Harkhani on 28/12/24
 */

@Composable
fun TokenContextMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    isFavorite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Edit") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            onClick = {
                onDismiss()
                onEdit()
            }
        )

        DropdownMenuItem(
            text = { Text(if (isFavorite) "Unfavorite" else "Favorite") },
            leadingIcon = {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            },
            onClick = {
                onDismiss()
                onToggleFavorite()
            }
        )

        DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null
                )
            },
            onClick = {
                onDismiss()
                onShare()
            }
        )

        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TokenContextMenuPreview() {
    ToptyTheme {
        Surface {
            Box {
                var expanded by remember { mutableStateOf(true) }
                TokenContextMenu(
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    isFavorite = false,
                    onEdit = {},
                    onDelete = {},
                    onShare = {},
                    onToggleFavorite = {}
                )
            }
        }
    }
}
