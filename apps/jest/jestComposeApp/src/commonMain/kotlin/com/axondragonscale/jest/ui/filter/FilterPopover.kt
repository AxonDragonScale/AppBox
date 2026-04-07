package com.axondragonscale.jest.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.jest.ui.theme.DarkSurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Popup-based filter panel for tablet and desktop (Medium/Expanded width).
 * Renders either the Category or Flags filter depending on [filterType], matching
 * the "Bottom Sheet — Category" and "Bottom Sheet — Flags" designs in jest.pen.
 *
 * Layout (320dp wide floating card, 20dp corner radius, 8dp elevation):
 *   1. Header row — title (Fraunces/titleLarge) + "Clear" text link (accent-colored, right-aligned)
 *   2. Subtitle — muted description text
 *   3. Vertical list of checkbox rows — each 52dp tall, 10dp corner, 16dp padding
 *        • Selected: label in onBackground, filled accent-color checkbox with check icon
 *        • Unselected: label in onSurfaceVariant, dark checkbox with border
 *   4. "Apply Filters" CTA button — full-width, 52dp, 14dp corner, accent fill
 *
 * [anchorOffset] is the window-space position of the bottom-left corner of the triggering chip.
 * The popup is placed directly below it via a custom [PopupPositionProvider].
 * Wrapped in a [Popup] with [PopupProperties.focusable = true] so it dismisses on outside tap.
 */
@Composable
fun FilterPopover(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    anchorOffset: IntOffset = IntOffset.Zero,
    vm: PrefsVM = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    FilterPopover(
        modifier = modifier,
        filterType = filterType,
        anchorOffset = anchorOffset,
        uiState = uiState,
        onEvent = { vm.onEvent(it) },
        onDismiss = onDismiss,
    )
}

@Composable
fun FilterPopover(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    anchorOffset: IntOffset = IntOffset.Zero,
    uiState: PrefsUiState,
    onEvent: (PrefsUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val positionProvider = chipAnchoredPositionProvider(anchorOffset)

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = modifier
                .width(320.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceVariant)
                .padding(24.dp),
        ) {
            FilterSheetContent(
                filterType = filterType,
                uiState = uiState,
                onEvent = onEvent,
                onDismiss = onDismiss,
            )
        }
    }
}

/**
 * Returns a [PopupPositionProvider] that places the popup's top-left corner at [anchorOffset],
 * i.e. flush below the bottom-left of the chip that triggered it.
 * If the popup would overflow the right edge of the window it is shifted left to fit.
 */
private fun chipAnchoredPositionProvider(anchorOffset: IntOffset) =
    object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            val x = (anchorOffset.x).coerceAtMost(windowSize.width - popupContentSize.width)
            val y = anchorOffset.y
            return IntOffset(x, y)
        }
    }
