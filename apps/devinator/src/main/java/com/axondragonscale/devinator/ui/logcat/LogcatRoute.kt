package com.axondragonscale.devinator.ui.logcat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.axondragonscale.logcatviewer.LogcatViewer

/** Created by Ronak on 09/11/26 */

/**
 * Devinator's entry into the shared viewer. `logcat` shows the lines written under our own UID, so
 * the screen opens straight into the log; the viewer keeps its own saved views.
 */
@Composable
fun LogcatRoute(navController: NavHostController) {
    LogcatViewer(onBack = { navController.popBackStack() })
}
