package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.axondragonscale.devinator.ui.theme.MonoCommandTextStyle

/** Created by Ronak on 08/11/26 */

/** A command block. The whole block copies, so the target is the block rather than the glyph. */
@Composable
fun CodeBlock(
    modifier: Modifier = Modifier,
    command: String,
    onCopy: () -> Unit,
) {
    // A one-line endpoint is centred in its 48 dp block; the grant command wraps to three lines,
    // where a centred glyph would float beside the middle line, so it stays at the top instead.
    var lines by remember { mutableIntStateOf(1) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                role = Role.Button,
                onClickLabel = "Copy command",
                onClick = onCopy,
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = if (lines > 1) Alignment.Top else Alignment.CenterVertically,
    ) {
        Text(
            text = command,
            modifier = Modifier.weight(1f),
            style = MonoCommandTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
            onTextLayout = { lines = it.lineCount },
        )
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
