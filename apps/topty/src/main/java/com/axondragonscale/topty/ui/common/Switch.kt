package com.axondragonscale.topty.ui.common

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 14/10/24
 */

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: SwitchColors = SwitchDefaults.colors(),
    shape: Shape = DefaultShape,
    borderWidth: Dp = SwitchDefaults.TrackOutlineWidth,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val trackColor = colors.trackColor(checked)
    val thumbColor = colors.thumbColor(checked)

    Box(
        modifier
            .minimumInteractiveComponentSize()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
            )
            .requiredSize(width = SwitchDefaults.SwitchWidth, height = SwitchDefaults.SwitchHeight)
            .border(borderWidth, colors.borderColor(checked), shape)
            .background(trackColor, shape)
    ) {
        Box(
            modifier =
                Modifier.align(Alignment.CenterStart)
                    .then(ThumbElement(interactionSource, checked))
                    .indication(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, radius = 20.dp)
                    )
                    .background(thumbColor, shape),
        )
    }
}

private data class ThumbElement(
    val interactionSource: InteractionSource,
    val checked: Boolean,
) : ModifierNodeElement<ThumbNode>() {
    override fun create() = ThumbNode(interactionSource, checked)

    override fun update(node: ThumbNode) {
        node.interactionSource = interactionSource
        if (node.checked != checked) {
            node.invalidateMeasurement()
        }
        node.checked = checked
        node.update()
    }
}

private class ThumbNode(
    var interactionSource: InteractionSource,
    var checked: Boolean,
) : Modifier.Node(), LayoutModifierNode {

    override val shouldAutoInvalidate: Boolean
        get() = false

    private var isPressed = false
    private var offsetAnim: Animatable<Float, AnimationVector1D>? = null
    private var sizeAnim: Animatable<Float, AnimationVector1D>? = null
    private var initialOffset: Float = Float.NaN
    private var initialSize: Float = Float.NaN

    override fun onAttach() {
        coroutineScope.launch {
            var pressCount = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> pressCount++
                    is PressInteraction.Release -> pressCount--
                    is PressInteraction.Cancel -> pressCount--
                }
                val pressed = pressCount > 0
                if (isPressed != pressed) {
                    isPressed = pressed
                    invalidateMeasurement()
                }
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val hasContent =
            measurable.maxIntrinsicHeight(constraints.maxWidth) != 0 &&
                    measurable.maxIntrinsicWidth(constraints.maxHeight) != 0
        val size =
            when {
                isPressed -> SwitchDefaults.ThumbDiameter + 4.dp
                hasContent || checked -> SwitchDefaults.ThumbDiameter
                else -> SwitchDefaults.UncheckedThumbDiameter
            }.toPx()

        val actualSize = (sizeAnim?.value ?: size).toInt()
        val placeable = measurable.measure(Constraints.fixed(actualSize, actualSize))
        val thumbPaddingStart = (SwitchDefaults.SwitchHeight - size.toDp()) / 2f
        val minBound = thumbPaddingStart.toPx()
        val thumbPathLength = (SwitchDefaults.SwitchWidth - SwitchDefaults.ThumbDiameter) - SwitchDefaults.ThumbPadding
        val maxBound = thumbPathLength.toPx()
        val offset =
            when {
                isPressed && checked -> maxBound - SwitchDefaults.TrackOutlineWidth.toPx()
                isPressed && !checked -> SwitchDefaults.TrackOutlineWidth.toPx()
                checked -> maxBound
                else -> minBound
            }

        if (sizeAnim?.targetValue != size) {
            coroutineScope.launch {
                sizeAnim?.animateTo(size, if (isPressed) SwitchDefaults.SnapSpec else SwitchDefaults.AnimationSpec)
            }
        }

        if (offsetAnim?.targetValue != offset) {
            coroutineScope.launch {
                offsetAnim?.animateTo(offset, if (isPressed) SwitchDefaults.SnapSpec else SwitchDefaults.AnimationSpec)
            }
        }

        if (initialSize.isNaN() && initialOffset.isNaN()) {
            initialSize = size
            initialOffset = offset
        }

        return layout(actualSize, actualSize) {
            placeable.placeRelative(offsetAnim?.value?.toInt() ?: offset.toInt(), 0)
        }
    }

    fun update() {
        if (sizeAnim == null && !initialSize.isNaN()) {
            sizeAnim = Animatable(initialSize)
        }

        if (offsetAnim == null && !initialOffset.isNaN()) offsetAnim = Animatable(initialOffset)
    }
}

object SwitchDefaults {

    val SwitchHeight = 32.dp
    val SwitchWidth = 52.dp

    val TrackOutlineWidth = 1.dp

    val ThumbDiameter = 24.dp
    val UncheckedThumbDiameter = 18.dp
    val ThumbPadding = (SwitchHeight - ThumbDiameter) / 2

    val SnapSpec = SnapSpec<Float>()
    val AnimationSpec = TweenSpec<Float>(durationMillis = 100)

    @Composable
    fun colors() = SwitchColors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
    )
}

data class SwitchColors(
    val checkedThumbColor: Color,
    val checkedTrackColor: Color,
    val checkedBorderColor: Color,
    val uncheckedThumbColor: Color,
    val uncheckedTrackColor: Color,
    val uncheckedBorderColor: Color,
) {
    fun trackColor(checked: Boolean) =
        if (checked) checkedTrackColor else uncheckedTrackColor

    fun thumbColor(checked: Boolean) =
        if (checked) checkedThumbColor else uncheckedThumbColor

    fun borderColor(checked: Boolean) =
        if (checked) checkedBorderColor else uncheckedBorderColor
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Switch(
                checked = false,
                onCheckedChange = {}
            )

            Switch(
                checked = true,
                onCheckedChange = {}
            )

            androidx.compose.material3.Switch(
                checked = false,
                onCheckedChange = {},
            )

            androidx.compose.material3.Switch(
                checked = true,
                onCheckedChange = {},
            )
        }
    }
}
