package com.axondragonscale.qrqanvas.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

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
            PlaceholderScreen(title = "Home")
        }

        composable<Route.Creator> { backStackEntry ->
            val creator: Route.Creator = backStackEntry.toRoute()
            PlaceholderScreen(
                title = if (creator.qrCodeId != null) "Edit QR Code" else "Create QR Code",
            )
        }

        composable<Route.Detail> { backStackEntry ->
            val detail: Route.Detail = backStackEntry.toRoute()
            PlaceholderScreen(title = "QR Code #${detail.qrCodeId}")
        }

        composable<Route.Scanner> {
            PlaceholderScreen(title = "Scanner")
        }

        composable<Route.History> {
            PlaceholderScreen(title = "Scan History")
        }

        composable<Route.Templates> {
            PlaceholderScreen(title = "Templates")
        }

        composable<Route.Settings> {
            PlaceholderScreen(title = "Settings")
        }

        composable<Route.DevTools> {
            PlaceholderScreen(title = "Dev Tools")
        }
    }
}

@Composable
private fun PlaceholderScreen(
    modifier: Modifier = Modifier,
    title: String,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
