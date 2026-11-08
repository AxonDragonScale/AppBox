package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors

/** Created by Ronak on 08/11/26 */

/** The app's one status chip: a dot and a label, never a control. */
@Composable
fun StatusChip(
    modifier: Modifier = Modifier,
    label: String,
    on: Boolean,
) {
    val contentColor = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (on) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    LocalDevinatorColors.current.errorSurfaceHigh
                },
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = contentColor, shape = CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
