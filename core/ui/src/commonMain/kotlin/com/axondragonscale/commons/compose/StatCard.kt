package com.axondragonscale.commons.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * A generic titled card shell for stat and chart widgets.
 *
 * Provides a [Surface] with [MaterialTheme.colorScheme.surfaceContainer] background and a
 * standardised title row at the top. An optional [trailingContent] slot sits at the right
 * side of the title row — useful for secondary labels (e.g. a peak-value badge).
 * The [content] lambda is rendered below the title inside the same [Column].
 *
 * @param title Primary label shown at the top-left of the card.
 * @param trailingContent Optional composable anchored at the top-right of the title row.
 * @param content Card body rendered below the title.
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                trailingContent?.invoke()
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

/**
 * A standard empty-state placeholder for chart widgets that have no data to display.
 *
 * Renders a centered [message] inside a fixed-height [Box], matching the visual footprint
 * of the chart it replaces so the [StatCard] shell doesn't collapse to nothing.
 *
 * @param message Text shown to the user. Defaults to "No records yet".
 */
@Composable
fun ChartEmptyState(
    modifier: Modifier = Modifier,
    message: String = "No records yet",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
