package com.axondragonscale.jest.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareText(text: String) {
    val activityViewController = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null,
    )
    UIApplication.sharedApplication.keyWindow?.rootViewController
        ?.presentViewController(activityViewController, animated = true, completion = null)
}
