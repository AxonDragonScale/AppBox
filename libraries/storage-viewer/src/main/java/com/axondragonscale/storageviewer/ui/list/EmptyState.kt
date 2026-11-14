package com.axondragonscale.storageviewer.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Created by Ronak on 14/11/26 */

private val IconSize = 64.dp // design: 64 icon tile
private val BodyMaxWidth = 320.dp
private val BodyMaxLines = 4
private val SidePadding = 24.dp
private val IconGap = 12.dp
private val TextGap = 6.dp

/**
 * The state a list shows when it has nothing to show: one icon, a plain-language title, the reason
 * and one way out. [helper] carries a muted second line, never a technical identifier.
 */
@Composable
internal fun StorageEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    body: String,
    helper: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SidePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(IconGap))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(TextGap))
        Text(
            text = body,
            modifier = Modifier.widthIn(max = BodyMaxWidth),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = BodyMaxLines,
        )
        if (helper != null) {
            Spacer(Modifier.height(TextGap))
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(IconGap))
            action()
        }
    }
}
