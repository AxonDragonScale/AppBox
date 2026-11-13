package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** Centred ink/caption message used by the loading, empty and not-found states. */
@Composable
fun StateMessage(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = WallmaticTextStyles.microStrong,
                color = MaterialTheme.wallmatic.inkMuted,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = WallmaticTextStyles.caption,
                    color = MaterialTheme.wallmatic.inkMuted,
                )
            }
        }
    }
}
