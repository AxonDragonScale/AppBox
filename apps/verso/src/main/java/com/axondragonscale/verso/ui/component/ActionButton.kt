package com.axondragonscale.verso.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.axondragonscale.verso.ui.theme.Dimens
import com.axondragonscale.verso.ui.theme.Spacing
import com.axondragonscale.verso.ui.theme.VersoTextStyles
import com.axondragonscale.verso.ui.theme.verso

enum class ActionStyle { Filled, Outline, Text }

/** Square, 48 dp, tracked caps. The only button in the app. */
@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ActionStyle = ActionStyle.Filled,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.verso

    val background = when (style) {
        ActionStyle.Filled -> colors.ink
        ActionStyle.Outline, ActionStyle.Text -> Color.Transparent
    }
    val content = when (style) {
        ActionStyle.Filled -> colors.paper
        ActionStyle.Outline, ActionStyle.Text -> colors.ink
    }

    Box(
        modifier = modifier
            .height(Dimens.touchTarget)
            .background(background)
            .then(
                if (style == ActionStyle.Outline) {
                    Modifier.border(Dimens.rule, colors.rule)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = VersoTextStyles.label,
            color = if (enabled) content else content.copy(alpha = 0.4f),
        )
    }
}
