package com.axondragonscale.logviewer.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.logviewer.ui.style.LocalLogStyle
import com.axondragonscale.logviewer.ui.style.hairlineColor

/** Created by Ronak Harkhani on 11/22/26 */

/** One applied tag filter; an exclusion reads "-tag" on the warning surface. */
@Composable
internal fun LogTagChip(label: String, exclude: Boolean, onRemove: () -> Unit) {
    val style = LocalLogStyle.current
    val content = if (exclude) style.warn else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (exclude) style.warnSurface else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(role = Role.Button, onClickLabel = "Remove filter", onClick = onRemove)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (exclude) "-$label" else label,
            style = style.mono,
            color = content,
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = content,
        )
    }
}

/** A section heading with the hairline the board draws out to the right. */
@Composable
internal fun FilterSectionHeader(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f),
            color = hairlineColor(),
        )
        trailing?.invoke()
    }
}

/** The filled pair that opens the rule picker; one per filter section. */
@Composable
internal fun IncludeExcludeButtons(
    onInclude: () -> Unit,
    onExclude: () -> Unit,
) {
    val style = LocalLogStyle.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        RuleButton(
            label = "Include",
            icon = Icons.Filled.Add,
            tint = MaterialTheme.colorScheme.primary,
            onClick = onInclude,
        )
        RuleButton(
            label = "Exclude",
            icon = Icons.Filled.Remove,
            tint = style.warn,
            onClick = onExclude,
        )
    }
}

@Composable
private fun RuleButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = tint,
        )
        Text(
            text = label,
            style = LocalLogStyle.current.action.copy(fontSize = 12.sp),
            color = tint,
        )
    }
}
