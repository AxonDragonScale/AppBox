package com.axondragonscale.limitless.ui.util

import androidx.compose.ui.Modifier

/**
 * Created by Ronak Harkhani on 03/11/24
 */

fun Modifier.thenIf(predicate: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (predicate) this.modifier() else this
