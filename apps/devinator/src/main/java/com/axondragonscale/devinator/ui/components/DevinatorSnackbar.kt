package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Created by Ronak on 09/11/26 */

/**
 * The app's one snackbar host. A failed write is the only feedback the screens ever show this way,
 * so it uses the same 12 dp corner as every inset row instead of Material's 4 dp default.
 */
@Composable
fun DevinatorSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(12.dp),
        )
    }
}
