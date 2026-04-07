package com.axondragonscale.jest.ui.util

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Project-local window size classification, replacing the broken
 * [androidx.compose.material3.windowsizeclass.WindowWidthSizeClass] companion object
 * which is unresolvable in commonMain for CMP 1.7+.
 *
 * Resolved via [rememberWindowSize] using [currentWindowAdaptiveInfo] from the
 * `org.jetbrains.compose.material3.adaptive:adaptive` library.
 */
enum class WindowSize {
    Compact,
    Medium,
    Expanded,
}
