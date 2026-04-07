package com.axondragonscale.jest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.jest.ui.theme.DarkError
import com.axondragonscale.jest.ui.theme.DarkSecondary
import com.axondragonscale.jest.ui.theme.DarkSuccess
import com.axondragonscale.jest.ui.theme.DarkTextTertiary
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Stat card component matching the StatCard component in jest.pen.
 * Dark surface (#16161A), colored numeric value, muted label below.
 * Used in the Home screen stats row: Seen (green), Liked (red), Streak (amber).
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    letterSpacing = (-1.2).sp,
                ),
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = DarkTextTertiary,
            )
        }
    }
}

@Preview
@Composable
private fun StatCardPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    modifier = Modifier.width(120.dp),
                    value = "142",
                    label = "Seen",
                    valueColor = DarkSuccess,
                )
                StatCard(
                    modifier = Modifier.width(120.dp),
                    value = "38",
                    label = "Liked",
                    valueColor = DarkError,
                )
                StatCard(
                    modifier = Modifier.width(120.dp),
                    value = "—",
                    label = "Streak",
                    valueColor = DarkSecondary,
                )
            }
        }
    }
}
