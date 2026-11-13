package com.axondragonscale.wallmatic.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.axondragonscale.wallmatic.ui.album.Album
import com.axondragonscale.wallmatic.ui.bottombar.Tab
import com.axondragonscale.wallmatic.ui.dashboard.Dashboard
import com.axondragonscale.wallmatic.ui.devtools.WallmaticDevTools
import com.axondragonscale.wallmatic.ui.folder.Folder
import com.axondragonscale.wallmatic.ui.wallpaper.Wallpaper

/**
 * Created by Ronak Harkhani on 06/06/24
 */

@Composable
fun WallmaticApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Route.Dashboard(),
        enterTransition = { slideIntoContainer(SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(SlideDirection.Left) },
        popEnterTransition = { slideIntoContainer(SlideDirection.Right) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Right) }
    ) {
        // TODO: Support deep links

        composable<Route.Dashboard> { backStackEntry ->
            val dashboard: Route.Dashboard = backStackEntry.toRoute()
            Dashboard(
                navController = navController,
                tab = Tab.entries.firstOrNull { it.name == dashboard.tab } ?: Tab.Home,
            )
        }

        composable<Route.Album> {
            Album(navController = navController)
        }

        composable<Route.Folder> {
            Folder(navController = navController)
        }

        composable<Route.Wallpaper> {
            Wallpaper(navController = navController)
        }

        composable<Route.DevTools> {
            WallmaticDevTools()
        }
    }
}
