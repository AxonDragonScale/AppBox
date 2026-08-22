package com.axondragonscale.cardy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.cardy.ui.detail.DetailScreen
import com.axondragonscale.cardy.ui.home.HomeScreen
import com.axondragonscale.cardy.ui.settings.SettingsScreen
import com.axondragonscale.cardy.ui.vault.VaultScreen

/** Created by Ronak on 05/30/26 */
@Composable
fun CardyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onReadingPageChanged: (Boolean) -> Unit = {},
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Home,
    ) {
        composable<Route.Home> {
            DisposableEffect(Unit) {
                onReadingPageChanged(true)
                onDispose { onReadingPageChanged(false) }
            }
            HomeScreen(navController = navController)
        }
        composable<Route.CardDetail> {
            DetailScreen(navController = navController)
        }
        composable<Route.Vault> {
            VaultScreen(navController = navController)
        }
        composable<Route.Settings> {
            SettingsScreen(navController = navController)
        }
    }
}
