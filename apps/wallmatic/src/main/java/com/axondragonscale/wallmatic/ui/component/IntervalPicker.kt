package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun IntervalPicker(
    modifier: Modifier = Modifier,
    value: Duration,
    onValueChange: (Duration) -> Unit,
    onCustomClick: () -> Unit,
) {
    val colors = MaterialTheme.wallmatic

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "EVERY",
                style = WallmaticTextStyles.microStrong,
                color = colors.inkMuted,
                modifier = Modifier.alignByBaseline(),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Interval.label(value),
                style = WallmaticTextStyles.titleSmall.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.ink,
                modifier = Modifier.alignByBaseline(),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "CUSTOM",
                style = WallmaticTextStyles.micro.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier
                    .clickable(onClickLabel = "Set a custom interval", onClick = onCustomClick),
            )
        }

        val selectedIndex = Interval.nearestTickIndex(value)
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)
        val thumbSize = 10.dp
        val rippleSize = 26.dp
        val interactionSource = remember { MutableInteractionSource() }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .pointerInput(Interval.ticks, thumbSize, rippleSize, interactionSource) {
                    val thumbPx = thumbSize.toPx()
                    val lastTickIndex = Interval.ticks.size - 1

                    // Maps a finger x to a tick index; the thumb travels over `width - thumbSize`.
                    fun indexAt(x: Float): Int {
                        val usableWidthPx = size.width - thumbPx
                        if (usableWidthPx <= 0f) return 0
                        val fraction = ((x - thumbPx / 2) / usableWidthPx).coerceIn(0f, 1f)
                        return (fraction * lastTickIndex).roundToInt()
                    }

                    var lastIndex = -1

                    fun snapTo(x: Float) {
                        val index = indexAt(x)
                        if (index == lastIndex) return
                        lastIndex = index
                        currentOnValueChange(Interval.ticks[index].duration)
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        lastIndex = currentSelectedIndex
                        snapTo(down.position.x)

                        val press = PressInteraction.Press(
                            Offset(rippleSize.toPx() / 2f, rippleSize.toPx() / 2f),
                        )
                        interactionSource.tryEmit(press)

                        val released = drag(down.id) { change ->
                            change.consume()
                            snapTo(change.position.x)
                        }
                        interactionSource.tryEmit(
                            if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press),
                        )
                    }
                }
        ) {
            val usableWidth = maxWidth - thumbSize

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2
                drawLine(
                    color = colors.ruleSoft,
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 1.dp.toPx(),
                )
                Interval.ticks.indices.forEach { index ->
                    val x = index * (size.width - 1.dp.toPx()) / (Interval.ticks.size - 1)
                    drawLine(
                        color = colors.ink,
                        start = Offset(x, centerY - 3.5.dp.toPx()),
                        end = Offset(x, centerY + 3.5.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }

            // The ripple layer follows the thumb so the press feedback is always centred on it.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(
                        x = usableWidth * (selectedIndex.toFloat() / (Interval.ticks.size - 1)) -
                                (rippleSize - thumbSize) / 2,
                    )
                    .size(rippleSize)
                    .indication(interactionSource, ripple(color = colors.ink, bounded = false))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = usableWidth * (selectedIndex.toFloat() / (Interval.ticks.size - 1)))
                    .size(thumbSize)
                    .background(colors.primary)
            )

            Row(modifier = Modifier.fillMaxSize()) {
                Interval.ticks.forEachIndexed { index, tick ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                role = Role.RadioButton
                                selected = index == selectedIndex
                                contentDescription = "Every ${tick.label}"
                                onClick(label = "Set every ${tick.label}") {
                                    onValueChange(tick.duration)
                                    true
                                }
                            }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Interval.ticks.forEach { tick ->
                Text(
                    text = tick.label,
                    style = WallmaticTextStyles.micro,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntervalPickerPreview() {
    WallmaticTheme {
        var value by remember { mutableStateOf(15.minutes) }
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            IntervalPicker(
                value = value,
                onValueChange = { value = it },
                onCustomClick = {},
            )
        }
    }
}

@Preview(name = "Custom Value Light", showBackground = true)
@Preview(name = "Custom Value Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntervalPickerCustomPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            IntervalPicker(
                value = 90.minutes,
                onValueChange = {},
                onCustomClick = {},
            )
        }
    }
}
