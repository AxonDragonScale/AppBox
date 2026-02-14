package com.axondragonscale.qrqanvas.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.axondragonscale.qrqanvas.ui.creator.Creator
import com.axondragonscale.qrqanvas.ui.detail.Detail
import com.axondragonscale.qrqanvas.ui.devtools.QRQanvasDevTools
import com.axondragonscale.qrqanvas.ui.history.History
import com.axondragonscale.qrqanvas.ui.home.Home
import com.axondragonscale.qrqanvas.ui.scanner.Scanner
import com.axondragonscale.qrqanvas.ui.settings.Settings
import com.axondragonscale.qrqanvas.ui.templates.Templates

/**
 * Created by Ronak Harkhani on 13/02/26
 */

@Composable
fun QRQanvasApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Route.Home,
        enterTransition = { slideIntoContainer(SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(SlideDirection.Left) },
        popEnterTransition = { slideIntoContainer(SlideDirection.Right) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Right) },
    ) {
        composable<Route.Home> {
            Home(
                onNavigateToCreator = { navController.navigate(Route.Creator()) },
                onNavigateToScanner = {
                    navController.navigate(Route.Scanner)
                },
                onNavigateToHistory = {
                    navController.navigate(Route.History)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                },
                onNavigateToDetail = { qrCodeId ->
                    navController.navigate(Route.Detail(qrCodeId))
                },
            )
        }

        composable<Route.Creator> { backStackEntry ->
            val creator: Route.Creator = backStackEntry.toRoute()
            Creator(
                qrCodeId = creator.qrCodeId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.Detail> { backStackEntry ->
            val detail: Route.Detail = backStackEntry.toRoute()
            Detail(
                qrCodeId = detail.qrCodeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { qrCodeId ->
                    navController.navigate(Route.Creator(qrCodeId))
                },
            )
        }

        composable<Route.Scanner> {
            Scanner(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.History> {
            History(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.Templates> {
            Templates(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.Settings> {
            Settings(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDevTools = {
                    navController.navigate(Route.DevTools)
                },
            )
        }

        composable<Route.DevTools> {
            QRQanvasDevTools()
        }
    }
}
