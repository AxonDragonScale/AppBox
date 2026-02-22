package com.axondragonscale.limitless.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.limitless.ui.devtools.LimitlessDevTools
import com.axondragonscale.limitless.ui.editor.Editor
import com.axondragonscale.limitless.ui.habit.Habit
import com.axondragonscale.limitless.ui.day.HabitDay
import com.axondragonscale.limitless.ui.home.Home
import com.axondragonscale.limitless.ui.settings.Settings
import com.axondragonscale.limitless.ui.stats.Stats

/**
 * Created by Ronak Harkhani on 31/10/24
 */

@Composable
fun LimitlessApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Route.Home,
        enterTransition = { slideIntoContainer(SlideDirection.Up) },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutOfContainer(SlideDirection.Down) }
    ) {
        composable<Route.Home> {
            Home(navController = navController)
        }

        composable<Route.Settings> {
            Settings(navController = navController)
        }

        composable<Route.Editor> {
            Editor(navController = navController)
        }

        composable<Route.Habit> {
            Habit(navController = navController)
        }

        composable<Route.HabitDay> {
            HabitDay(navController = navController)
        }

        composable<Route.Stats> {
            Stats(navController = navController)
        }

        composable<Route.DevTools> {
            LimitlessDevTools()
        }
    }
}
