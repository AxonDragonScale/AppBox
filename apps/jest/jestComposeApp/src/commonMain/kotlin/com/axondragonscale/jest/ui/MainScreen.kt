package com.axondragonscale.jest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.axondragonscale.jest.ui.home.Home
import com.axondragonscale.jest.ui.library.Library
import com.axondragonscale.jest.ui.navigation.BottomBar
import com.axondragonscale.jest.ui.navigation.Destination
import com.axondragonscale.jest.ui.navigation.Route
import com.axondragonscale.jest.ui.navigation.Sidebar
import com.axondragonscale.jest.ui.settings.Settings
import com.axondragonscale.jest.ui.util.WindowSize
import com.axondragonscale.jest.ui.util.rememberWindowSize

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Adaptive main screen shell.
 *
 * - **Compact** (mobile): bottom tab bar floating over content
 * - **Medium** (tablet): fixed 240dp sidebar on the left
 * - **Expanded** (desktop): fixed 280dp sidebar on the left
 *
 * Content is rendered via a `when` block on [activeDestination] — no HorizontalPager.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val windowSize = rememberWindowSize()

    var activeDestination by rememberSaveable { mutableStateOf<Destination>(Destination.Home) }

    MainScreen(
        modifier = modifier,
        windowSize = windowSize,
        activeDestination = activeDestination,
        onDestinationSelected = { activeDestination = it },
        onNavigateToDevTools = { navController.navigate(Route.DevTools) },
    )
}

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
    windowSize: WindowSize,
    activeDestination: Destination,
    onDestinationSelected: (Destination) -> Unit,
    onNavigateToDevTools: () -> Unit,
) {
    val isCompact = windowSize == WindowSize.Compact
    val sidebarWidth = if (windowSize == WindowSize.Expanded) 280.dp else 240.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar — tablet and desktop only
            if (!isCompact) {
                Sidebar(
                    destinations = Destination.all,
                    activeDestination = activeDestination,
                    onDestinationSelected = onDestinationSelected,
                    width = sidebarWidth,
                )
            }

            // Main content area
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (activeDestination) {
                    is Destination.Home -> Home(
                        windowSize = windowSize,
                    )
                    is Destination.Library -> Library(
                        windowSize = windowSize,
                    )
                    is Destination.Settings -> Settings(
                        windowSize = windowSize,
                        onNavigateToDevTools = onNavigateToDevTools,
                    )
                }
            }
        }

        // Bottom bar — mobile (Compact) only, floating over content
        if (isCompact) {
            BottomBar(
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 20.dp)
                    .align(Alignment.BottomCenter),
                destinations = Destination.all,
                activeDestination = activeDestination,
                onDestinationSelected = onDestinationSelected,
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenMobilePreview() {
    com.axondragonscale.jest.ui.theme.JestTheme(darkTheme = true) {
        MainScreen(
            windowSize = WindowSize.Compact,
            activeDestination = Destination.Home,
            onDestinationSelected = {},
            onNavigateToDevTools = {},
        )
    }
}

@Preview
@Composable
private fun MainScreenTabletPreview() {
    com.axondragonscale.jest.ui.theme.JestTheme(darkTheme = true) {
        MainScreen(
            windowSize = WindowSize.Medium,
            activeDestination = Destination.Home,
            onDestinationSelected = {},
            onNavigateToDevTools = {},
        )
    }
}
