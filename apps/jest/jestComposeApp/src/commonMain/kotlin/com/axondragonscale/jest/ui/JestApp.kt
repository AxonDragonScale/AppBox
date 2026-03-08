package com.axondragonscale.jest.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.axondragonscale.jest.ui.devtools.JestDevTools
import com.axondragonscale.jest.ui.navigation.Route

/**
 * Created by Ronak Harkhani on 01/04/24
 */

@Composable
fun JestApp() {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Route.Dashboard(),
        enterTransition = { slideIntoContainer(SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(SlideDirection.Left) },
        popEnterTransition = { slideIntoContainer(SlideDirection.Right) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Right) }
    ) {
        composable<Route.Dashboard> {
            val args = it.toRoute<Route.Dashboard>()
            MainScreen(
                navController = navController,
                initialTab = args.initialTab,
            )
        }

        composable<Route.DevTools> {
            JestDevTools()
        }
    }
}
