/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.ui.theme.AmexBlue
import com.axondragonscale.cardy.ui.theme.CardyTheme
import com.axondragonscale.cardy.ui.theme.DiscoverOrange
import com.axondragonscale.cardy.ui.theme.JcbRed
import com.axondragonscale.cardy.ui.theme.MastercardOrange
import com.axondragonscale.cardy.ui.theme.MastercardRed
import com.axondragonscale.cardy.ui.theme.RupayTeal
import com.axondragonscale.cardy.ui.theme.UnknownGray
import com.axondragonscale.cardy.ui.theme.VisaBlue

private const val PAYMENT_CARD_ASPECT_RATIO = 85.60f / 53.98f

@Composable
fun CardVisual(
    modifier: Modifier = Modifier,
    network: CardNetwork,
    pan: String?,
    expiry: String?,
    maskPan: Boolean = false,
) {
    val gradient = getNetworkGradient(network)
    val chipVariant = ("${network.name}:${pan?.takeLast(4).orEmpty()}".hashCode() and Int.MAX_VALUE) % 3

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PAYMENT_CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        EmvChip(
            variant = chipVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 22.dp),
        )

        Text(
            text = network.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = formatPanForDisplay(pan, maskPan),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        letterSpacing = 0.8.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )

                Column {
                    Text(
                        text = "VALID THRU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        text = expiry ?: "--/--",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmvChip(
    variant: Int,
    modifier: Modifier = Modifier,
) {
    val lineColor = Color(0xFF7A642D)
    val chipGradient = when (variant) {
        0 -> listOf(Color(0xFFE9CF7A), Color(0xFFC89B3C))
        1 -> listOf(Color(0xFFF0D98A), Color(0xFFD2AA4F))
        else -> listOf(Color(0xFFE2C067), Color(0xFFB98932))
    }
    Canvas(
        modifier = modifier
            .size(width = 46.dp, height = 35.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                Brush.linearGradient(chipGradient),
            ),
    ) {
        val stroke = 1.dp.toPx()
        val w = size.width
        val h = size.height
        val grooves = Path().apply {
            when (variant) {
                0 -> {
                    moveTo(w / 2, 0f)
                    lineTo(w / 2, h)
                    moveTo(0f, h / 2)
                    lineTo(w, h / 2)

                    moveTo(0f, h * 0.27f)
                    lineTo(w * 0.28f, h * 0.27f)
                    quadraticTo(w * 0.36f, h * 0.27f, w * 0.36f, h * 0.36f)
                    lineTo(w / 2, h * 0.42f)
                    moveTo(w, h * 0.27f)
                    lineTo(w * 0.72f, h * 0.27f)
                    quadraticTo(w * 0.64f, h * 0.27f, w * 0.64f, h * 0.36f)
                    lineTo(w / 2, h * 0.42f)

                    moveTo(0f, h * 0.73f)
                    lineTo(w * 0.28f, h * 0.73f)
                    quadraticTo(w * 0.36f, h * 0.73f, w * 0.36f, h * 0.64f)
                    lineTo(w / 2, h * 0.58f)
                    moveTo(w, h * 0.73f)
                    lineTo(w * 0.72f, h * 0.73f)
                    quadraticTo(w * 0.64f, h * 0.73f, w * 0.64f, h * 0.64f)
                    lineTo(w / 2, h * 0.58f)
                }
                1 -> {
                    moveTo(w / 2, 0f)
                    lineTo(w / 2, h)
                    for (fraction in listOf(0.25f, 0.5f, 0.75f)) {
                        moveTo(0f, h * fraction)
                        lineTo(w, h * fraction)
                    }
                    moveTo(w * 0.23f, 0f)
                    lineTo(w * 0.23f, h * 0.25f)
                    moveTo(w * 0.77f, 0f)
                    lineTo(w * 0.77f, h * 0.25f)
                    moveTo(w * 0.23f, h * 0.75f)
                    lineTo(w * 0.23f, h)
                    moveTo(w * 0.77f, h * 0.75f)
                    lineTo(w * 0.77f, h)
                }
                else -> {
                    moveTo(0f, h / 2)
                    lineTo(w, h / 2)
                    moveTo(w * 0.38f, 0f)
                    lineTo(w * 0.38f, h * 0.34f)
                    lineTo(w / 2, h * 0.43f)
                    lineTo(w * 0.62f, h * 0.34f)
                    lineTo(w * 0.62f, 0f)
                    moveTo(w * 0.38f, h)
                    lineTo(w * 0.38f, h * 0.66f)
                    lineTo(w / 2, h * 0.57f)
                    lineTo(w * 0.62f, h * 0.66f)
                    lineTo(w * 0.62f, h)
                    moveTo(0f, h * 0.25f)
                    lineTo(w * 0.38f, h * 0.25f)
                    moveTo(w * 0.62f, h * 0.25f)
                    lineTo(w, h * 0.25f)
                    moveTo(0f, h * 0.75f)
                    lineTo(w * 0.38f, h * 0.75f)
                    moveTo(w * 0.62f, h * 0.75f)
                    lineTo(w, h * 0.75f)
                }
            }
        }
        drawPath(
            path = grooves,
            color = lineColor,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

fun getNetworkGradient(network: CardNetwork): Brush = when (network) {
    CardNetwork.VISA -> Brush.linearGradient(listOf(VisaBlue, Color(0xFF2A3080)))
    CardNetwork.MASTERCARD -> Brush.linearGradient(listOf(MastercardRed, MastercardOrange))
    CardNetwork.RUPAY -> Brush.linearGradient(listOf(RupayTeal, Color(0xFF0CA88A)))
    CardNetwork.AMEX -> Brush.linearGradient(listOf(AmexBlue, Color(0xFF0099FF)))
    CardNetwork.DISCOVER -> Brush.linearGradient(listOf(DiscoverOrange, Color(0xFFFF8C33)))
    CardNetwork.JCB -> Brush.linearGradient(listOf(JcbRed, Color(0xFFFF3333)))
    CardNetwork.UNIONPAY -> Brush.linearGradient(listOf(Color(0xFF1F4E8C), Color(0xFF003366)))
    CardNetwork.UNKNOWN -> Brush.linearGradient(listOf(UnknownGray, Color(0xFF4A4A5A)))
}

private fun formatPanForDisplay(pan: String?, maskPan: Boolean): String {
    if (pan == null) return "•••• •••• •••• ••••"
    if (maskPan) return "•••• •••• •••• ${pan.takeLast(4)}"
    return pan.chunked(4).joinToString(" ")
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardVisualPreview() {
    CardyTheme {
        CardVisual(
            network = CardNetwork.VISA,
            pan = "4111111111111111",
            expiry = "12/27",
        )
    }
}
