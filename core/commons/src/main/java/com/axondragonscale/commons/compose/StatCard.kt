package com.axondragonscale.commons.compose

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
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

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatCardPreview() {
    MaterialTheme {
        StatCard(
            modifier = Modifier.padding(16.dp),
            title = "Weekly Pattern",
        ) {
            Text(
                text = "Chart goes here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "With Trailing — Light", showBackground = true)
@Preview(name = "With Trailing — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatCardWithTrailingPreview() {
    MaterialTheme {
        StatCard(
            modifier = Modifier.padding(16.dp),
            title = "Time of Day",
            trailingContent = {
                Text(
                    text = "Peak: 7 AM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            Text(
                text = "Chart goes here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Empty State — Light", showBackground = true)
@Preview(name = "Empty State — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChartEmptyStatePreview() {
    MaterialTheme {
        Surface {
            ChartEmptyState(modifier = Modifier.padding(16.dp))
        }
    }
}

// endregion
