package com.axondragonscale.verso.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.verso.ui.component.Tab
import com.axondragonscale.verso.ui.component.VersoTabBar
import com.axondragonscale.verso.ui.library.LibraryScreen
import com.axondragonscale.verso.ui.settings.SettingsScreen
import com.axondragonscale.verso.ui.stats.StatsScreen
import com.axondragonscale.verso.ui.theme.verso

/**
 * Three destinations: Library, Stats, Settings. The reader will sit outside this shell.
 */
@Composable
fun VersoApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val selected = when {
        destination?.hasRoute<Route.Stats>() == true -> Tab.Stats
        destination?.hasRoute<Route.Settings>() == true -> Tab.Settings
        else -> Tab.Library
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.verso.paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            VersoTabBar(
                selected = selected,
                onSelect = { tab ->
                    val route = when (tab) {
                        Tab.Library -> Route.Library
                        Tab.Stats -> Route.Stats
                        Tab.Settings -> Route.Settings
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Library,
            modifier = Modifier.padding(padding),
        ) {
            composable<Route.Library> {
                LibraryScreen(
                    onImportBooks = {},
                    onImportFolder = {},
                )
            }
            composable<Route.Stats> { StatsScreen() }
            composable<Route.Settings> { SettingsScreen() }
        }
    }
}
