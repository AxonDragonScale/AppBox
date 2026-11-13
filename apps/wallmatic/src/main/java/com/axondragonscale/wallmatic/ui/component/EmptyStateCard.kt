package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** The outlined empty card from the Components page, 364 × 360. */
@Composable
fun EmptyStateCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .background(MaterialTheme.wallmatic.paper)
            .border(1.dp, MaterialTheme.wallmatic.ink)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = WallmaticTextStyles.dialogTitle,
            color = MaterialTheme.wallmatic.ink,
        )
        Text(
            text = description,
            style = WallmaticTextStyles.caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = MaterialTheme.wallmatic.inkMuted,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            TextAction(
                modifier = Modifier.padding(top = 8.dp),
                text = action,
                onClick = onAction,
                emphasized = true,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 420)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 420)
@Composable
private fun EmptyStateCardPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
        ) {
            EmptyStateCard(
                title = "No albums yet",
                description = "Create your first album to start rotating wallpapers.",
            )
        }
    }
}
