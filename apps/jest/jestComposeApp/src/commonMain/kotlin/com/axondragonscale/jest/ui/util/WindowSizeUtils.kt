package com.axondragonscale.jest.ui.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Returns the project-local [WindowSize] based on the current window's adaptive info,
 * using [currentWindowAdaptiveInfo] from the `org.jetbrains.compose.material3.adaptive:adaptive`
 * library which is available in commonMain across all CMP platforms.
 *
 * Uses [WindowSizeClass.isWidthAtLeastBreakpoint] with the Material3 breakpoint constants:
 *   - Compact  : width < 600dp  (below MEDIUM lower bound)
 *   - Medium   : 600dp ≤ width < 840dp (below EXPANDED lower bound)
 *   - Expanded : width ≥ 840dp
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val info = currentWindowAdaptiveInfo()
    return when {
        !info.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)   -> WindowSize.Compact
        !info.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowSize.Medium
        else                                                                                           -> WindowSize.Expanded
    }
}
