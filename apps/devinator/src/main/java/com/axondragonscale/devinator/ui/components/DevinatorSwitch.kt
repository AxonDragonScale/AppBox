package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors

/** Created by Ronak on 11/06/26 */

/**
 * The app's one switch. Material 3 owns the geometry and touch target; `dangerOff` is what the
 * developer-options switch turns into when it is off.
 */
@Composable
fun DevinatorSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dangerOff: Boolean = false,
    contentDescription: String? = null,
) {
    val colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedBorderColor = Color.Transparent,
        checkedIconColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = if (dangerOff) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        uncheckedTrackColor = if (dangerOff) {
            LocalDevinatorColors.current.errorSurfaceHigh
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = MaterialTheme.colorScheme.errorContainer,
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.then(
            if (contentDescription == null) {
                Modifier
            } else {
                Modifier.semantics { this.contentDescription = contentDescription }
            },
        ),
        enabled = enabled,
        colors = colors,
        thumbContent = when {
            checked -> {
                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            }

            dangerOff -> {
                { Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
            }

            else -> null
        },
    )
}
