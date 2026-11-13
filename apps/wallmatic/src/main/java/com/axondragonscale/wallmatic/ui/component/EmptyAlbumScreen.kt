package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.Dimens

/**
 * Created by Ronak Harkhani on 11/14/26
 */

/** First-run empty screen with its primary action anchored above the tab bar. */
@Composable
fun EmptyAlbumScreen(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
    description: String,
    steps: List<String>,
    actionText: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        EmptyAlbumLayout(
            eyebrow = eyebrow,
            title = title,
            description = description,
            steps = steps,
        )

        Spacer(modifier = Modifier.weight(1f))

        WallmaticButton(text = actionText, onClick = onAction)

        Spacer(modifier = Modifier.height(24.dp))
    }
}
