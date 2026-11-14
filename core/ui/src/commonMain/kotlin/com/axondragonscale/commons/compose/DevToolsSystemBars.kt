package com.axondragonscale.commons.compose

import androidx.compose.runtime.Composable

/**
 * Switches the window's system-bar icons to their light appearance for the dark [DevToolsTheme].
 *
 * A dialog and an activity own different windows, so each composition resolves its own; platforms
 * without system bars do nothing.
 */
@Composable
public expect fun DevToolsSystemBars()
