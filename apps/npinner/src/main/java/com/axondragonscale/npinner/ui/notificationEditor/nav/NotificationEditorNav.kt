package com.axondragonscale.npinner.ui.notificationEditor.nav

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.axondragonscale.npinner.ui.Destination
import com.axondragonscale.npinner.ui.notificationEditor.NotificationEditor

/**
 * Created by Ronak Harkhani on 01/05/23
 */

fun NavGraphBuilder.notificationEditorGraph(navController: NavController) {
    composable(
        route = Destination.NotificationEditor.setupRoute,
        deepLinks = listOf(
            navDeepLink {
                action = Intent.ACTION_EDIT
                uriPattern = Destination.NotificationEditor.setupDeepLink
            }
        ),
        arguments = listOf(navArgument(Destination.NotificationEditor.argId) {
            type = NavType.StringType
            nullable = true
        })
    ) {
        NotificationEditor(navController = navController)
    }
}

fun NavController.navigateToNotificationEditor(id: String? = null) {
    if (id == null) navigate(Destination.NotificationEditor.route)
    else navigate("${Destination.NotificationEditor.route}?${Destination.NotificationEditor.argId}=$id")
}
