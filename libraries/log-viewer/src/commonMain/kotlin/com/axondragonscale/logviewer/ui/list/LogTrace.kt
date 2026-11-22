package com.axondragonscale.logviewer.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Created by Ronak Harkhani on 11/22/26 */

/** The lines below a record's first: one exception, one block. */
@Composable
internal fun LogTrace(
    lines: List<String>,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    firstLineColor: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.take(maxLines).forEachIndexed { index, line ->
            Text(
                text = line,
                style = style,
                color = if (index == 0 && firstLineColor != null) firstLineColor else color,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (lines.size > maxLines) {
            Text(
                text = "… ${lines.size - maxLines} more",
                style = style,
                color = color.copy(alpha = 0.7f),
            )
        }
    }
}
