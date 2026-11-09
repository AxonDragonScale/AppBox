package com.axondragonscale.logcatviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.axondragonscale.logcatviewer.model.FilterKind
import com.axondragonscale.logcatviewer.model.SavedView
import com.axondragonscale.logcatviewer.ui.filter.FilterDraft
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle

/** Created by Ronak on 09/11/26 */

/** The name dialog the saved-views flow opens: naming a new view or renaming an existing one. */
internal sealed interface NameDialog {
    val initial: String
    val title: String
    val caption: String

    data class New(override val initial: String) : NameDialog {
        override val title get() = "Save view"
        override val caption get() = "Name this filter set so you can restore it in one tap."
    }

    data class Rename(val view: SavedView) : NameDialog {
        override val initial get() = view.name
        override val title get() = "Rename view"
        override val caption get() = "Pick a new name for this saved filter set."
    }
}

@Composable
internal fun SaveViewDialog(dialog: NameDialog, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    LogcatTextDialog(
        title = dialog.title,
        caption = dialog.caption,
        initial = dialog.initial,
        confirmLabel = "Save",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun AddRuleDialog(draft: FilterDraft, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    LogcatTextDialog(
        title = if (draft.include) "Include ${draft.kind.label}" else "Exclude ${draft.kind.label}",
        caption = if (draft.kind == FilterKind.PACKAGE) {
            "Matches the package's live PIDs."
        } else {
            "Exact tag equality."
        },
        initial = "",
        confirmLabel = "Add",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun LogcatTextDialog(
    title: String,
    caption: String,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(initial) { mutableStateOf(initial) }
    val focusRequester = remember { FocusRequester() }

    LogcatDialog(onDismiss = onDismiss) {
        Text(
            text = title,
            style = LocalLogcatStyle.current.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onConfirm(text) }),
            )
        }
        DialogActions(
            confirmLabel = confirmLabel,
            confirmEnabled = text.isNotBlank(),
            onConfirm = { onConfirm(text) },
            onDismiss = onDismiss,
        )
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
internal fun LogcatDeleteDialog(viewName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    LogcatDialog(onDismiss = onDismiss) {
        Text(
            text = "Delete view?",
            style = LocalLogcatStyle.current.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "\"$viewName\" will be removed. This cannot be undone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DialogActions(
            confirmLabel = "Delete",
            confirmEnabled = true,
            destructive = true,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun LogcatDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun DialogActions(
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Cancel",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onDismiss)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            style = LocalLogcatStyle.current.action,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onConfirm,
            enabled = confirmEnabled,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            colors = if (destructive) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Text(text = confirmLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}
