package com.axondragonscale.logviewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun LogBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
