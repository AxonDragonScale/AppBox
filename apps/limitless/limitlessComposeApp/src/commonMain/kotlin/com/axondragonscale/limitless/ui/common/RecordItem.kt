package com.axondragonscale.limitless.ui.common

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.formatDateTime
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 29/06/25
 */

@Composable
internal fun RecordItem(
    modifier: Modifier = Modifier,
    record: RecordEntity,
    onClick: () -> Unit = {},
) {
    val formattedDateTime = remember(record.timestamp) {
        Instant.fromEpochMilliseconds(record.timestamp).formatDateTime()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Text(
            text = formattedDateTime,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (record.message.isNotBlank()) {
            Text(
                text = record.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        } else {
            Text(
                text = "No description",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun RecordItemPreview() {
    LimitlessTheme {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                RecordItem(
                    record = RecordEntity(
                        id = 1,
                        habitId = 1,
                        timestamp = 1750900200000,
                        message = "Morning meditation session",
                    ),
                )
                HorizontalDivider()
                RecordItem(
                    record = RecordEntity(
                        id = 2,
                        habitId = 1,
                        timestamp = 1750857000000,
                        message = "",
                    ),
                )
                HorizontalDivider()
                RecordItem(
                    record = RecordEntity(
                        id = 3,
                        habitId = 1,
                        timestamp = 1750813800000,
                        message = "Felt really focused today, managed a full 30 minute session without distractions.",
                    ),
                )
            }
        }
    }
}

// endregion
