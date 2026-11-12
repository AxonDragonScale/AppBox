package com.axondragonscale.wallmatic.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Created by Ronak Harkhani on 12/11/26
 */

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object Dimens {
    /** Screen gutter on every side, matching the 412 dp design frames. */
    val screenPadding = 24.dp

    val mastheadHeight = 32.dp
    val topBarHeight = 56.dp
    val tabBarHeight = 64.dp
    val touchTarget = 48.dp
    val rule = 1.dp
    val activeRule = 3.dp

    /** Wallpaper previews use the phone's own 412:915 crop. */
    const val DeviceAspectRatio = 412f / 915f

    /** Album and wallpaper tiles: 178 × 395. */
    const val TileAspectRatio = 178f / 395f

    val fabEndInset = 16.dp
    val fabBottomInset = 24.dp
    val gridBottomInset = 96.dp
}
