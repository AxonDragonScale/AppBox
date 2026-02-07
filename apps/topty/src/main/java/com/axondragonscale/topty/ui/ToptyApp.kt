package com.axondragonscale.topty.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.axondragonscale.topty.ui.backuprestore.BackupRestore
import com.axondragonscale.topty.ui.devtools.ToptyDevTools
import com.axondragonscale.topty.ui.token.Token
import com.axondragonscale.topty.ui.home.Home
import com.axondragonscale.topty.ui.onboarding.Onboarding
import com.axondragonscale.topty.ui.qrscanner.QRScanner
import com.axondragonscale.topty.ui.settings.Settings
import com.axondragonscale.topty.ui.sharetoken.ShareToken

/**
 * Created by Ronak Harkhani on 15/09/24
 */

@Composable
fun ToptyApp(
    modifier: Modifier = Modifier,
    startDestination: Any,
) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideIntoContainer(SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(SlideDirection.Left) },
        popEnterTransition = { slideIntoContainer(SlideDirection.Right) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Right) }
    ) {
        composable<Route.Onboarding> {
            Onboarding(navController = navController)
        }

        composable<Route.Home> {
            Home(navController = navController)
        }

        composable<Route.QRScanner> {
            QRScanner(navController = navController)
        }

        composable<Route.Token.Add> {
            val tokenRoute = it.toRoute<Route.Token.Add>()
            Token(
                navController = navController,
                tokenRoute = tokenRoute,
            )
        }

        composable<Route.Token.Edit> {
            val tokenRoute = it.toRoute<Route.Token.Edit>()
            Token(
                navController = navController,
                tokenRoute = tokenRoute,
            )
        }

        composable<Route.Settings> {
            Settings(navController = navController)
        }

        composable<Route.BackupRestore> {
            BackupRestore(navController = navController)
        }

        composable<Route.DevTools> {
            ToptyDevTools()
        }

        composable<Route.ShareToken> {
            val shareTokenRoute = it.toRoute<Route.ShareToken>()
            ShareToken(
                navController = navController,
                tokenId = shareTokenRoute.tokenId,
            )
        }
    }
}
