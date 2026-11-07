package com.axondragonscale.devinator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.devinator.ui.home.HomeScreen
import com.axondragonscale.devinator.ui.settings.PermissionScreen
import com.axondragonscale.devinator.ui.settings.SettingsScreen
import com.axondragonscale.devinator.ui.setup.SetupScreen
import com.axondragonscale.devinator.ui.tiles.QuickSettingsTilesScreen

/** Created by Ronak on 11/06/26 */
@Composable
fun DevinatorApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
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
        composable<Route.Permission> {
            PermissionScreen(navController = navController)
        }
        composable<Route.QuickSettingsTiles> {
            QuickSettingsTilesScreen(navController = navController)
        }
    }
}
