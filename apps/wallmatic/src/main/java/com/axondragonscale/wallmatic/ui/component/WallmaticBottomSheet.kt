package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** Square paper sheet anchored to the bottom edge, with the drag mark the design uses. */
@Composable
fun WallmaticBottomSheet(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    WallmaticOverlay(onDismiss = onDismiss) {
        Column(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier)
                .background(MaterialTheme.wallmatic.paper)
                .border(1.dp, MaterialTheme.wallmatic.ink)
                .navigationBarsPadding()
                .padding(
                    start = Dimens.screenPadding,
                    top = 16.dp,
                    end = Dimens.screenPadding,
                    bottom = 24.dp,
                ),
            content = content,
        )
    }
}

@Composable
fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .background(MaterialTheme.wallmatic.ink)
        )
    }
}
