package com.axondragonscale.jest.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Created by Ronak Harkhani on 24/10/24
 */
sealed class Destination(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
) {

    companion object {
        val all: List<Destination> by lazy { listOf(Home, Library, Settings) }
    }

    data object Home : Destination(
        label = "Home",
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
    )

    data object Library : Destination(
        label = "Library",
        activeIcon = Icons.Filled.LibraryBooks,
        inactiveIcon = Icons.Outlined.LibraryBooks,
    )

    data object Settings : Destination(
        label = "Settings",
        activeIcon = Icons.Filled.Settings,
        inactiveIcon = Icons.Outlined.Settings,
    )
}
