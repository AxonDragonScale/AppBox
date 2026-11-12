package com.axondragonscale.wallmatic.ui.component

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun TextAction(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = MaterialTheme.wallmatic.primary,
    emphasized: Boolean = true,
    compact: Boolean = false,
) {
    Box(
        modifier = modifier
            .height(if (compact) 24.dp else 40.dp)
            .clickable(enabled = enabled, onClickLabel = text, onClick = onClick)
            .padding(horizontal = if (compact) 4.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(Locale.ROOT),
            style = WallmaticTextStyles.microStrong.copy(
                fontSize = if (compact) 10.sp else 11.sp,
                lineHeight = if (compact) 13.sp else 16.sp,
                letterSpacing = if (compact) 1.6.sp else 1.sp,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = if (enabled) color else color.copy(alpha = 0.4f),
        )
    }
}
