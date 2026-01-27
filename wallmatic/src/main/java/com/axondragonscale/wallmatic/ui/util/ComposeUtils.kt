package com.axondragonscale.wallmatic.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 21/09/24
 */

@Composable
fun getAspectRatio(): Float {
    val config = LocalConfiguration.current
    return config.screenWidthDp.dp / config.screenHeightDp.dp
}
