package com.axondragonscale.npinner.ui

/**
 * Created by Ronak Harkhani on 29/04/23
 */

object Destination {

    const val SCHEME = "npinner://"

    object Notifications {
        const val route = "notifications"
        const val setupRoute = route
        const val deepLink = SCHEME + route
    }

    object NotificationEditor {
        const val route = "notificationEditor"
        const val argId = "id"
        const val setupRoute = route + "?${argId}={${argId}}"
        const val setupDeepLink = SCHEME + setupRoute
        const val deeplink = SCHEME + route
    }

}
