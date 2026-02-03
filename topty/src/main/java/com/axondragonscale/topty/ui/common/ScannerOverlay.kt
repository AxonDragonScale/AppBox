package com.axondragonscale.topty.ui.common

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.topty.ui.theme.ToptyTheme

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f), //
    ) {
        // Gray out the background
        drawRect(color = Color.Black.copy(alpha = 0.5f))

        // Draw Cutout
        val cutoutScale = 1 + progress * 0.05f
        val cutoutSize = Size(size.width, size.width) * 0.8f * cutoutScale
        val cutoutOffset = Offset(
            x = (size.width - cutoutSize.width) / 2,
            y = (size.height - cutoutSize.height) / 2,
        )
        val cornerRadius = 4.dp.toPx()
        drawRoundRect(
            topLeft = cutoutOffset,
            size = cutoutSize,
            cornerRadius = CornerRadius(cornerRadius),
            color = Color.Transparent,
            blendMode = BlendMode.Clear,
        )

        // Draw Corners Brackets
        val cornerScale = 1 + progress * 0.1f
        val cornerSize = cutoutSize.width / 6 * cornerScale
        val cornerGap = 16.dp.toPx()
        val strokeWidth = 8.dp.toPx() * (1.2f - progress * 0.2f)

        // Top Left
        val tlCorner = cutoutOffset + Offset(cornerGap, cornerGap)
        val tlCornerPath = Path().apply {
            moveTo(tlCorner.x, tlCorner.y)
            relativeLineTo(cornerSize, 0f)
            moveTo(tlCorner.x, tlCorner.y)
            relativeLineTo(0f, cornerSize)
        }

        // Top Right
        val trCorner = cutoutOffset + Offset(cutoutSize.width - cornerGap, cornerGap)
        val trCornerPath = Path().apply {
            moveTo(trCorner.x, trCorner.y)
            relativeLineTo(-cornerSize, 0f)
            moveTo(trCorner.x, trCorner.y)
            relativeLineTo(0f, cornerSize)
        }

        // Bottom Left
        val blCorner = cutoutOffset + Offset(cornerGap, cutoutSize.height - cornerGap)
        val blCornerPath = Path().apply {
            moveTo(blCorner.x, blCorner.y)
            relativeLineTo(cornerSize, 0f)
            moveTo(blCorner.x, blCorner.y)
            relativeLineTo(0f, -cornerSize)
        }

        // Bottom Right
        val brCorner = cutoutOffset + Offset(cutoutSize.width - cornerGap, cutoutSize.height - cornerGap)
        val brCornerPath = Path().apply {
            moveTo(brCorner.x, brCorner.y)
            relativeLineTo(-cornerSize, 0f)
            moveTo(brCorner.x, brCorner.y)
            relativeLineTo(0f, -cornerSize)
        }

        listOf(tlCornerPath, trCornerPath, blCornerPath, brCornerPath).forEach { path ->
            drawPath(
                path,
                color = Color.Black.copy(0.8f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.offset(y = -50.dp),
                text = "Text below Overlay",
                color = MaterialTheme.colorScheme.onBackground,
            )

            ScannerOverlay()

            Text(
                modifier = Modifier.offset(y = 50.dp),
                text = "Text atop Overlay",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
