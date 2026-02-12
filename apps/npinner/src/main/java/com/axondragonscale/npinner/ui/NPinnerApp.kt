package com.axondragonscale.npinner.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.npinner.ui.notificationEditor.nav.notificationEditorGraph
import com.axondragonscale.npinner.ui.notifications.nav.notificationsGraph

/**
 * Created by Ronak Harkhani on 22/04/23
 */

@Composable
fun NPinnerApp() {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.systemBarsPadding(),
        navController = navController,
        startDestination = Destination.Notifications.route,
        enterTransition = { slideIntoContainer(SlideDirection.Up) },
        exitTransition = { slideOutOfContainer(SlideDirection.Up) },
        popEnterTransition = { slideIntoContainer(SlideDirection.Down) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Down) }
    ) {

        notificationsGraph(navController)

        notificationEditorGraph(navController)

    }
}


