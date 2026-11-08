package com.axondragonscale.devinator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.devinator.permission.GrantRepository
import com.axondragonscale.devinator.ui.home.HomeScreen
import com.axondragonscale.devinator.ui.settings.PermissionScreen
import com.axondragonscale.devinator.ui.settings.SettingsScreen
import com.axondragonscale.devinator.ui.setup.SetupScreen
import com.axondragonscale.devinator.ui.tiles.QuickSettingsTilesScreen
import com.axondragonscale.devinator.ui.tweaks.TweaksScreen
import org.koin.compose.koinInject

/** Created by Ronak on 11/06/26 */
@Composable
fun DevinatorApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val grant: GrantRepository = koinInject()

    // The grant is the app's only hard gate: losing it anywhere outside the app returns to Setup.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val onSetup = navController.currentDestination?.hasRoute<Route.Setup>() == true
        if (!grant.isGranted() && !onSetup) {
            navController.navigate(Route.Setup) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Setup,
    ) {
        composable<Route.Setup> {
            SetupScreen(navController = navController)
        }
        composable<Route.Home> {
            HomeScreen(navController = navController)
        }
        composable<Route.Settings> {
            SettingsScreen(navController = navController)
        }
        composable<Route.Tweaks> {
            TweaksScreen(navController = navController)
        }
        composable<Route.Permission> {
            PermissionScreen(navController = navController)
        }
        composable<Route.QuickSettingsTiles> {
            QuickSettingsTilesScreen(navController = navController)
        }
    }
}
