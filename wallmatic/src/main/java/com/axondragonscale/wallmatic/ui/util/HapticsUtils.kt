package com.axondragonscale.wallmatic.ui.util

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Created by Ronak Harkhani on 25/09/24
 */

fun View.performLongPressHapticFeedback() =
    this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

fun View.performTickHapticFeedback() =
    this.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
