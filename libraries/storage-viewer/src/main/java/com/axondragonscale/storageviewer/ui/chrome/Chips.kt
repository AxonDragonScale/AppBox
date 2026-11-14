package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.CellKind
import com.axondragonscale.storageviewer.model.PreferenceType
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val ChipHeight = 20.dp // design: chip20
private val ChipRadius = 6.dp // design: chip6
private val PillRadius = 999.dp // design: pill999
private val ChipPadding = 8.dp
private const val PillFillAlpha = 0.14f

/** A key, count or flag. Mono, so it reads as data rather than prose. */
@Composable
internal fun MetaChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Chip(
        text = text,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(ChipRadius),
        modifier = modifier,
    )
}

/** The type label on a value row; [color] is the kind's hue, never the only signal. */
@Composable
internal fun TypeChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Chip(
        text = text,
        tint = color,
        shape = RoundedCornerShape(ChipRadius),
        modifier = modifier,
    )
}

/** The one running state the viewer shows, such as `SCANNING`. */
@Composable
internal fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(ChipHeight)
            .clip(RoundedCornerShape(PillRadius))
            .background(color.copy(alpha = PillFillAlpha))
            .padding(horizontal = ChipPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LocalStorageViewerStyle.current.monoChip,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Chip(
    text: String,
    tint: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val style = LocalStorageViewerStyle.current
    Box(
        modifier = modifier
            .height(ChipHeight)
            .clip(shape)
            .background(style.chipFill)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = ChipPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = style.monoChip,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A database cell kind's hue: numbers share one, null and text are neutral, blobs read as bytes. */
@Composable
internal fun kindColor(kind: CellKind): Color {
    val style = LocalStorageViewerStyle.current
    return when (kind) {
        CellKind.NULL -> style.kindNeutral
        CellKind.INTEGER, CellKind.REAL -> style.kindNumber
        CellKind.TEXT -> style.kindNeutral
        CellKind.BLOB -> style.storePreferences
    }
}

/** A preference type's hue, on the same ramp as a cell kind. */
@Composable
internal fun preferenceColor(type: PreferenceType): Color {
    val style = LocalStorageViewerStyle.current
    return when (type) {
        PreferenceType.STRING, PreferenceType.STRING_SET -> style.kindNeutral
        PreferenceType.INT, PreferenceType.LONG, PreferenceType.FLOAT, PreferenceType.DOUBLE ->
            style.kindNumber
        PreferenceType.BOOL -> style.kindBool
        PreferenceType.BYTES -> style.storePreferences
    }
}
