package com.axondragonscale.logcatviewer.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.FilterRule
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle

/** Created by Ronak on 09/11/26 */

/** A rule's polarity drives its label, icon and tint, so a call site cannot mismatch them. */
internal data class RuleAction(val label: String, val icon: ImageVector, val tint: Color)

@Composable
internal fun ruleAction(include: Boolean): RuleAction = RuleAction(
    label = if (include) "Include" else "Exclude",
    icon = if (include) Icons.Filled.Add else Icons.Filled.Remove,
    tint = if (include) MaterialTheme.colorScheme.primary else LocalLogcatStyle.current.warn,
)

/**
 * One applied rule. The page and the sheet pass a remove action; the saved-view card passes none
 * and sits on a tinted surface, so it supplies its own [container].
 */
@Composable
internal fun LogcatRuleChip(
    rule: FilterRule,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    onRemove: (() -> Unit)? = null,
) {
    val style = LocalLogcatStyle.current
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (rule.include) container else style.warnSurface)
            .then(
                if (onRemove == null) {
                    Modifier
                } else {
                    Modifier.clickable(role = Role.Button, onClickLabel = "Remove filter", onClick = onRemove)
                },
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rule.label,
            style = style.mono,
            color = if (rule.include) MaterialTheme.colorScheme.onSurface else style.warn,
        )
        if (onRemove != null) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (rule.include) MaterialTheme.colorScheme.onSurfaceVariant else style.warn,
            )
        }
    }
}

@Composable
internal fun NoneChip() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "None",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
