package com.axondragonscale.logcatviewer.ui.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

/** Created by Ronak on 09/11/26 */

/** Stack frames under a record: the first is the exception, the rest is context. */
@Composable
internal fun LogcatTrace(
    lines: List<String>,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
) {
    lines.take(maxLines).forEachIndexed { index, line ->
        Text(
            text = line,
            style = style,
            color = if (index == 0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
    if (lines.size > maxLines) {
        Text(
            text = "… ${lines.size - maxLines} more lines",
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
