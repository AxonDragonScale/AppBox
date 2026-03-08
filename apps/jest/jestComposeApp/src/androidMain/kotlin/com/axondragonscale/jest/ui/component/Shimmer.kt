package com.axondragonscale.jest.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Created by Ronak Harkhani on 12/04/24
 */

fun Modifier.shimmer(
    isEnabled: Boolean,
    baseColor: Color,
    durationMillis: Int = 1000,
): Modifier {
    if (!isEnabled) return this
    return composed {
        val shimmerColors = listOf(
            baseColor.copy(alpha = 0.3f),
            baseColor.copy(alpha = 0.2f),
            baseColor.copy(alpha = 0.1f),
            baseColor.copy(alpha = 0.2f),
            baseColor.copy(alpha = 0.3f),
        )

        val transition = rememberInfiniteTransition()

        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "Shimmer",
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset.Zero,
                end = Offset(
                    x = translateAnimation.value,
                    y = translateAnimation.value
                ),
            ),
        )
    }
}
