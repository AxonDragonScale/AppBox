package com.axondragonscale.qrqanvas.ui.common

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Shared QR code preview composable used by both Creator and Detail screens.
 * Displays a rendered QR code bitmap inside a styled container, or a placeholder
 * when the bitmap is not yet available.
 */

/**
 * A QR code preview that shows a [bitmap] inside a rounded container.
 * When [bitmap] is null, displays a placeholder appropriate to the [style].
 *
 * @param modifier Modifier for the outer container.
 * @param bitmap The rendered QR code bitmap, or null if not yet available.
 * @param style The placeholder style to show when [bitmap] is null.
 * @param contentPadding Padding between the container edge and the QR image.
 */
@Composable
fun QRPreview(
    modifier: Modifier = Modifier,
    bitmap: Bitmap?,
    style: QRPreviewStyle = QRPreviewStyle.Loading,
    contentPadding: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        } else {
            when (style) {
                QRPreviewStyle.Loading -> LoadingPlaceholder()
                QRPreviewStyle.Empty -> EmptyPlaceholder()
            }
        }
    }
}

/**
 * The placeholder style to show when a QR bitmap is not available.
 */
enum class QRPreviewStyle {
    /** Shows a progress indicator with "Rendering…" text (used on Detail screen). */
    Loading,

    /** Shows a QR icon with instructional text (used on Creator screen). */
    Empty,
}

@Composable
private fun LoadingPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
        )
        Text(
            text = "Rendering…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun EmptyPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.QrCode2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = "Enter content to generate QR",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
