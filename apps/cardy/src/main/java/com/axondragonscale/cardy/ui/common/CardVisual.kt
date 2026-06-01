/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun CardVisual(
    modifier: Modifier = Modifier,
    network: CardNetwork,
    pan: String?,
    expiry: String?,
    cardholderName: String?,
) {
    val gradient = getNetworkGradient(network)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = gradient)
            .padding(24.dp),
    ) {
        // Network name top-right
        Text(
            text = network.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd),
        )

        Column(
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            // PAN
            Text(
                text = formatPanForDisplay(pan),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                ),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // Expiry
                Column {
                    Text(
                        text = "VALID THRU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        text = expiry ?: "--/--",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Cardholder name
                if (cardholderName != null) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Text(
                            text = cardholderName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }
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

private fun formatPanForDisplay(pan: String?): String {
    if (pan == null) return "•••• •••• •••• ••••"
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
            cardholderName = "JOHN DOE",
        )
    }
}
