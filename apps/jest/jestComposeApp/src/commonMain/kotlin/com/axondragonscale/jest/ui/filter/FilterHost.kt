package com.axondragonscale.jest.ui.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.axondragonscale.jest.ui.util.WindowSize

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Identifies which filter chip triggered the filter UI.
 *
 * - [Category] — the "Category" chip; accent color is indigo (primary)
 * - [Flags] — the "Exclude Flags" chip; accent color is amber (secondary)
 */
sealed interface FilterType {
    data object Category : FilterType
    data object Flags : FilterType
}

/**
 * Adaptive filter surface chooser.
 *
 * - **Compact** (mobile): delegates to [FilterBottomSheet] — slides up from the bottom
 * - **Medium / Expanded** (tablet / desktop): delegates to [FilterPopover] — inline popup
 *
 * Callers only need to know about [FilterHost]; they never reference the sheet or popover directly.
 * Pass the [FilterType] matching the chip that was tapped so the sheet renders the correct
 * title, subtitle, item list, accent color, and "Clear" action.
 */
@Composable
fun FilterHost(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    windowSize: WindowSize,
    anchorOffset: IntOffset = IntOffset.Zero,
    onDismiss: () -> Unit,
) {
    if (windowSize == WindowSize.Compact) {
        FilterBottomSheet(
            modifier = modifier,
            filterType = filterType,
            onDismiss = onDismiss,
        )
    } else {
        FilterPopover(
            modifier = modifier,
            filterType = filterType,
            anchorOffset = anchorOffset,
            onDismiss = onDismiss,
        )
    }
}
