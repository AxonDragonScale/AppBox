package com.axondragonscale.topty.ui.common

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme

/**
 * Created by Ronak Harkhani on 07/10/24
 */

enum class TextDirection {
    LEFT,
    RIGHT,
}

data class ExpandingFabOptions(
    val showText: Boolean = true,
    val textDirection: TextDirection = TextDirection.LEFT,
    val duration: Int = 300,
    val animationSpec: AnimationSpec<Float> = tween(
        durationMillis = duration,
        easing = LinearEasing,
    ),
    val size: Dp = 56.dp,
    val spacing: Dp = 8.dp,
) {
    val itemOffset = size + spacing

    val contentAlignment = when {
        !showText -> Alignment.BottomCenter
        textDirection == TextDirection.LEFT -> Alignment.BottomEnd
        else -> Alignment.BottomStart
    }
}

data class ExpandingFabItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun ExpandingFabButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    items: List<ExpandingFabItem>,
    options: ExpandingFabOptions = ExpandingFabOptions(),
) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = options.animationSpec,
    )

    Box(
        modifier = modifier,
        contentAlignment = options.contentAlignment,
    ) {
        items.forEachIndexed { index, item ->

            FABItemIcon(
                modifier = Modifier
                    .scale(progress)
                    .padding(bottom = (index + 1) * options.itemOffset * progress)
                    .clip(DefaultShape)
                    .clickable {
                        isExpanded = false
                        item.onClick()
                    },
                item = item,
                options = options
            )

            if (!options.showText) return@forEachIndexed
            Box(
                modifier = Modifier
                    .scale(progress)
                    .padding(bottom = (index + 1) * options.itemOffset * progress)
                    .padding(
                        start = if (options.textDirection == TextDirection.LEFT) 0.dp else 64.dp * progress,
                        end = if (options.textDirection == TextDirection.RIGHT) 0.dp else 64.dp * progress,
                    )
                    .height(options.size),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            shape = DefaultShape
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    text = item.label,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        FAB(
            isExpanded = isExpanded,
            enabled = enabled,
            onClick = { isExpanded = !isExpanded },
            options = options,
        )
    }
}

@Composable
private fun FABItemIcon(
    modifier: Modifier = Modifier,
    item: ExpandingFabItem,
    options: ExpandingFabOptions,
) = Box(
    modifier = modifier
        .size(options.size)
        .padding(options.spacing / 2)
        .background(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = DefaultShape
        ),
    contentAlignment = Alignment.Center
) {
    Icon(
        imageVector = item.icon,
        contentDescription = item.label,
        tint = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer)
    )
}

@Composable
private fun FAB(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    options: ExpandingFabOptions,
    shape: Shape = DefaultShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    val progress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = options.animationSpec
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (!isExpanded) contentColor else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(
            durationMillis = options.duration,
            easing = LinearEasing
        ),
    )

    Surface(
        modifier = modifier.size(options.size),
        enabled = enabled,
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        contentColor = animatedContentColor,
        shadowElevation = if (progress > 0.1f) 0.dp else 4.dp,
    ) {
        Box(
            modifier = Modifier
                .scale(progress)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = progress),
                    shape = DefaultShape
                )
        )

        Box(
            modifier = Modifier
                .scale(1 - progress)
                .background(color = containerColor, shape = DefaultShape)
        )

        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.rotate(45 * 5 * progress),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }

    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomEnd,
        ) {
            val fabItems = remember {
                listOf(
                    ExpandingFabItem(
                        icon = Icons.Default.Home,
                        label = "Action 1",
                        onClick = {},
                    ),
                    ExpandingFabItem(
                        icon = Icons.Default.Settings,
                        label = "Action 2",
                        onClick = {},
                    ),
                    ExpandingFabItem(
                        icon = Icons.Default.AccountCircle,
                        label = "Action 3",
                        onClick = {},
                    ),
                )
            }
            ExpandingFabButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                items = fabItems,
            )
        }
    }
}
