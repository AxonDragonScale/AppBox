package com.axondragonscale.wallmatic.devtools

import androidx.compose.runtime.Composable

/**
 * Created by Ronak Harkhani on 11/21/26
 *
 * Release counterpart of the debug screen: `:core:devtools` is not on this build's classpath. The
 * route stays in the nav graph, and Settings keeps its entry point behind `BuildConfig.DEV_TOOLS`,
 * so this destination is unreachable.
 */
@Composable
fun WallmaticDevTools() = Unit
