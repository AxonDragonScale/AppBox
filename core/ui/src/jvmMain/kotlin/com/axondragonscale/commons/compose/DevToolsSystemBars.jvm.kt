package com.axondragonscale.commons.compose

import androidx.compose.runtime.Composable

/** Desktop windows have no system-bar icon appearance to flip. */
@Composable
public actual fun DevToolsSystemBars() = Unit
