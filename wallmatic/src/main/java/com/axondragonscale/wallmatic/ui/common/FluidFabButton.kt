package com.axondragonscale.wallmatic.ui.common

import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

/**
 * Created by Ronak Harkhani on 26/06/24
 */

data class FluidFabButtonProperties(
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun FluidFabButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    duration: Int = 1000,
    onClick: () -> Unit,
    leftButton: FluidFabButtonProperties,
    rightButton: FluidFabButtonProperties,
) {
    val fluidChainRenderEffect = remember { fluidChainRenderEffect() }
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {

        // Two ExpandableFabButton are needed here
        // The first one provides the background, blur and fluid effect
        // But the blur restricts it from rendering the icons properly
        // Also drawing icons affects the fluid chain color
        // So we draw a second ExpandableFabButton on top of it without render effect
        // that renders the icons

        ExpandableFabButton(
            isExpanded = isExpanded,
            fluidChainRenderEffect = fluidChainRenderEffect,
            renderIcons = false,
            duration = duration,
        )

        ExpandableFabButton(
            isExpanded = isExpanded,
            fluidChainRenderEffect = null,
            duration = duration,
            onClick = onClick,
            leftButton = leftButton,
            rightButton = rightButton,
        )

        val outlineProgress by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = (duration * 0.4).toInt(),
                delayMillis = if (isExpanded) (duration * 0.4).toInt() else 0,
                easing = LinearEasing
            ),
        )
        ExpandedFabOutline(
            scale = outlineProgress * 0.8f,
            alpha = outlineProgress * 0.7f,
        )
    }
}

@Composable
private fun ExpandableFabButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    fluidChainRenderEffect: RenderEffect?,
    renderIcons: Boolean = true,
    duration: Int,
    onClick: () -> Unit = {},
    leftButton: FluidFabButtonProperties? = null,
    rightButton: FluidFabButtonProperties? = null,
) {
    Box(
        modifier = modifier.graphicsLayer {
            renderEffect = fluidChainRenderEffect
        },
        contentAlignment = Alignment.BottomCenter,
    ) {
        val expandedButtonBottomPadding = 50.dp
        val expandedButtonHorizontalPadding = 120.dp

        val leftFabProgress by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = (duration * 0.8).toInt(),
                delayMillis = if (isExpanded) 0 else (duration * 0.2).toInt(),
                easing = FastOutSlowInEasing
            )
        )
        AnimatedFab(
            modifier = Modifier.padding(
                PaddingValues(
                    bottom = expandedButtonBottomPadding * leftFabProgress,
                    end = expandedButtonHorizontalPadding * leftFabProgress,
                )
            ),
            icon = leftButton?.icon,
            iconAlpha = leftFabProgress,
            onClick = leftButton?.onClick,
        )

        val rightFabProgress by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = (duration * 0.8).toInt(),
                delayMillis = if (isExpanded) (duration * 0.2).toInt() else 0,
                easing = FastOutSlowInEasing
            )
        )
        AnimatedFab(
            modifier = Modifier.padding(
                PaddingValues(
                    bottom = expandedButtonBottomPadding * rightFabProgress,
                    start = expandedButtonHorizontalPadding * rightFabProgress,
                )
            ),
            icon = rightButton?.icon,
            iconAlpha = rightFabProgress,
            onClick = rightButton?.onClick,
        )

        val backgroundScale by animateFloatAsState(
            targetValue = if (isExpanded) 0f else 1f,
            animationSpec = tween(
                durationMillis = if (isExpanded) (duration * 0.6).toInt() else (duration * 0.3).toInt(),
                delayMillis = if (isExpanded) (duration * 0.4).toInt() else (duration * 0.1).toInt(),
                easing = FastOutSlowInEasing
            )
        )
        AnimatedFab(
            modifier = Modifier.scale(backgroundScale),
            icon = null,
        )

        val iconRotation by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = (duration * 0.6).toInt(),
                delayMillis = (duration * 0.2).toInt(),
                easing = FastOutSlowInEasing
            )
        )
        AnimatedFab(
            modifier = Modifier.rotate(45 * 5 * iconRotation),
            icon = if (renderIcons) Icons.Default.Add else null,
            iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            backgroundColor = Color.Transparent,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedFab(
    modifier: Modifier = Modifier,
    backgroundColor: Color = FloatingActionButtonDefaults.containerColor,
    icon: ImageVector?,
    iconAlpha: Float = 1f,
    iconColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        FloatingActionButton(
            modifier = modifier,
            shape = CircleShape,
            containerColor = backgroundColor,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            onClick = onClick ?: {},
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = (iconColor ?: LocalContentColor.current).copy(alpha = iconAlpha)
                )
            }
        }
    }
}

@Composable
fun ExpandedFabOutline(
    modifier: Modifier = Modifier,
    scale: Float,
    alpha: Float,
) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = modifier
                .size(56.dp) // Fab Default Size
                .scale(scale)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
                    shape = CircleShape
                )
        )
    }
}

private fun fluidChainRenderEffect(): RenderEffect? {
    // RenderEffect not supported below android 12
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    // The radius can be provided as an argument if button size is changed
    val blur = android.graphics.RenderEffect.createBlurEffect(
        80f, 80f, Shader.TileMode.DECAL
    )
    val alpha = android.graphics.RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(
            floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 50f, -5000f
            )
        )
    )

    return android.graphics.RenderEffect.createChainEffect(alpha, blur).asComposeRenderEffect()
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            FluidFabButton(
                isExpanded = isExpanded,
                duration = 500,
                onClick = { isExpanded = !isExpanded },
                leftButton = FluidFabButtonProperties(
                    icon = Icons.Default.Folder,
                    onClick = {}
                ),
                rightButton = FluidFabButtonProperties(
                    icon = Icons.Default.Image,
                    onClick = {}
                ),
            )
        }
    }
}
