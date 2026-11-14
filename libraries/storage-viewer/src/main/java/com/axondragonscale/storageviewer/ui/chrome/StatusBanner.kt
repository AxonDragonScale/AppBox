package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.StorageError
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val BannerRadius = 20.dp // design: card20
private val BannerBorder = 1.dp
private val BannerPadding = 16.dp
private val BannerIcon = 20.dp
private val BannerGap = 10.dp
private val LineGap = 2.dp
private val BannerActionHeight = 40.dp
private val BannerActionRadius = 12.dp // design: tile12
private val BannerActionPadding = 14.dp

internal enum class BannerTone { Warn, Error, Busy }

internal enum class BannerActionVariant { Primary, Secondary }

/** A banner action; the variant carries the one visual decision so no call site picks a colour. */
@Immutable
internal data class BannerAction(
    val label: String,
    val variant: BannerActionVariant,
    val onClick: () -> Unit,
)

/**
 * The surface's one state statement. A warn or error tone names the state in plain language;
 * [details] is the only place a technical identifier is allowed.
 */
@Composable
internal fun StorageStatusBanner(
    modifier: Modifier = Modifier,
    tone: BannerTone,
    title: String,
    body: String,
    details: String? = null,
    action: BannerAction? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = LocalStorageViewerStyle.current
    val icon: ImageVector = when (tone) {
        BannerTone.Warn -> Icons.Filled.Warning
        BannerTone.Error -> Icons.Filled.ErrorOutline
        BannerTone.Busy -> Icons.Filled.Schedule
    }
    val accent = when (tone) {
        BannerTone.Warn -> style.warn
        BannerTone.Error -> scheme.error
        BannerTone.Busy -> scheme.primary
    }
    val container = when (tone) {
        BannerTone.Warn -> style.warnSurface
        BannerTone.Error -> style.errorSurface
        BannerTone.Busy -> scheme.surfaceContainerHigh
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BannerRadius))
            .background(container)
            .border(BannerBorder, scheme.outline, RoundedCornerShape(BannerRadius))
            .padding(BannerPadding),
        verticalArrangement = Arrangement.spacedBy(BannerGap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BannerGap),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(BannerIcon),
                tint = accent,
            )
            Column(verticalArrangement = Arrangement.spacedBy(LineGap)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = scheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
                if (details != null) {
                    Text(
                        text = "Details · $details",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (action != null) {
            BannerButton(action = action)
        }
    }
}

@Composable
private fun BannerButton(action: BannerAction) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(BannerActionRadius)
    val primary = action.variant == BannerActionVariant.Primary
    Box(
        modifier = Modifier
            .height(BannerActionHeight)
            .clip(shape)
            .background(if (primary) scheme.primary else Color.Transparent)
            .border(BannerBorder, if (primary) scheme.primary else scheme.outline, shape)
            .clickable(role = Role.Button, onClick = action.onClick)
            .padding(horizontal = BannerActionPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) scheme.onPrimary else scheme.primary,
            maxLines = 1,
        )
    }
}

/**
 * The one failure statement the database surfaces share: a writer's lock reads as `in use` with a
 * busy tone, everything else as an unreadable file, and a driver code only ever lands in Details.
 */
@Composable
internal fun StorageErrorBanner(
    modifier: Modifier = Modifier,
    error: StorageError,
    onRetry: () -> Unit,
) {
    val busy = error is StorageError.Busy
    StorageStatusBanner(
        modifier = modifier,
        tone = if (busy) BannerTone.Busy else BannerTone.Error,
        title = if (busy) "Database is in use" else "Couldn't read this database",
        body = if (busy) {
            "Close the screen that is writing, then retry."
        } else {
            "The database file could not be read. It may have been moved or deleted."
        },
        details = (error as? StorageError.Unreadable)?.detail,
        action = BannerAction(
            label = "Retry",
            variant = BannerActionVariant.Secondary,
            onClick = onRetry,
        ),
    )
}
