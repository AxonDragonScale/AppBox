package com.axondragonscale.devinator.ui.devtools

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.commons.devtools.DevToolsScreen

/** Created by Ronak on 11/06/26 */

/**
 * Devinator's developer surface: the platform built-ins every app gets, plus the Logcat and Storage
 * viewers `:core:devtools` hosts. Devinator adds no tools of its own here — its settings live on
 * Home and Tweaks.
 */
@Composable
fun DevinatorDevTools(
    modifier: Modifier = Modifier,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Devinator Dev Tools",
        subtitle = "Debug utilities for Devinator",
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DevinatorDevToolsPreview() {
    Surface {
        DevinatorDevTools()
    }
}
