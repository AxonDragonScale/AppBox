package com.axondragonscale.logcatviewer.ui.sheets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.FilterKind
import com.axondragonscale.logcatviewer.model.FilterRule
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.ui.filter.FilterDraft
import com.axondragonscale.logcatviewer.ui.filter.LogcatRuleChip
import com.axondragonscale.logcatviewer.ui.filter.LogcatSearchField
import com.axondragonscale.logcatviewer.ui.filter.NoneChip
import com.axondragonscale.logcatviewer.ui.filter.ruleAction
import com.axondragonscale.logcatviewer.ui.preview.LogcatPreviewTheme
import com.axondragonscale.logcatviewer.ui.preview.PreviewFilter

/** Created by Ronak on 09/11/26 */

@Composable
internal fun FilterSheet(
    filter: LogFilter,
    resultCount: Int,
    onQueryChange: (String) -> Unit,
    onRegexChange: (Boolean) -> Unit,
    onRemoveRule: (FilterRule) -> Unit,
    onAddFilter: (FilterDraft) -> Unit,
    onClose: () -> Unit,
) {
    LogcatBottomSheet(onDismiss = onClose) {
        FilterSheetContent(
            filter = filter,
            resultCount = resultCount,
            onQueryChange = onQueryChange,
            onRegexChange = onRegexChange,
            onRemoveRule = onRemoveRule,
            onAddFilter = onAddFilter,
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterSheetContent(
    filter: LogFilter,
    resultCount: Int,
    onQueryChange: (String) -> Unit,
    onRegexChange: (Boolean) -> Unit,
    onRemoveRule: (FilterRule) -> Unit,
    onAddFilter: (FilterDraft) -> Unit,
    onClose: () -> Unit,
) {
    SheetHeader(title = "Filters", onClose = onClose)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetLabel("CONTENT")
        LogcatSearchField(
            value = filter.query,
            onValueChange = onQueryChange,
            regex = filter.regex,
            onRegexChange = onRegexChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        )
        Text(
            text = "Matches the message text only. Case-insensitive unless regex is on.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    RuleSection(
        kind = FilterKind.TAG,
        filter = filter,
        onRemoveRule = onRemoveRule,
        onAddFilter = onAddFilter,
    )

    RuleSection(
        kind = FilterKind.PACKAGE,
        filter = filter,
        onRemoveRule = onRemoveRule,
        onAddFilter = onAddFilter,
    )

    Text(
        text = "Include keeps a line; exclude drops it. Package follows the app's live PIDs.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    SheetPrimaryButton(text = "Show $resultCount lines", onClick = onClose)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleSection(
    kind: FilterKind,
    filter: LogFilter,
    onRemoveRule: (FilterRule) -> Unit,
    onAddFilter: (FilterDraft) -> Unit,
) {
    val rules = filter.rules.filter { it.kind == kind }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetLabel(kind.name) {
            RuleActionButton(include = true) { onAddFilter(FilterDraft(kind, include = true)) }
            RuleActionButton(include = false) { onAddFilter(FilterDraft(kind, include = false)) }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (rules.isEmpty()) {
                NoneChip()
            } else {
                rules.forEach { rule ->
                    LogcatRuleChip(rule, onRemove = { onRemoveRule(rule) })
                }
            }
        }
    }
}

@Composable
private fun RuleActionButton(include: Boolean, onClick: () -> Unit) {
    val action = ruleAction(include)
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = action.tint,
        )
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = action.tint,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterSheetPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SheetColumn {
                FilterSheetContent(
                    filter = PreviewFilter,
                    resultCount = 142,
                    onQueryChange = {},
                    onRegexChange = {},
                    onRemoveRule = {},
                    onAddFilter = {},
                    onClose = {},
                )
            }
        }
    }
}
