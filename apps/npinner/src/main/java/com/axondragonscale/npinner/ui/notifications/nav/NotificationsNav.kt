package com.axondragonscale.npinner.ui.notifications.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.axondragonscale.npinner.ui.Destination
import com.axondragonscale.npinner.ui.notifications.Notifications

/**
 * Created by Ronak Harkhani on 01/05/23
 */

fun NavGraphBuilder.notificationsGraph(navController: NavController) {
    composable(
        route = Destination.Notifications.setupRoute,
        deepLinks = listOf(navDeepLink { uriPattern = Destination.Notifications.deepLink })
    ) {
        Notifications(navController = navController)
    }
}

fun NavController.navigateToNotifications() {
    navigate(route = Destination.Notifications.route)
}
