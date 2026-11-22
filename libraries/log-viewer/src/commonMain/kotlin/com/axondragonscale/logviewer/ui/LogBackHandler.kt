package com.axondragonscale.logviewer.ui

import androidx.compose.runtime.Composable

/** Android routes the system back gesture here; the other targets have no such gesture. */
@Composable
internal expect fun LogBackHandler(onBack: () -> Unit)
