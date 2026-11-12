package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun StatusRow(
    modifier: Modifier = Modifier,
    title: String,
    detail: String,
    action: String,
    onAction: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalRule()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(Locale.ROOT),
                    style = WallmaticTextStyles.microStrong.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = MaterialTheme.wallmatic.ink,
                )
                Text(
                    text = detail,
                    style = WallmaticTextStyles.caption,
                    color = MaterialTheme.wallmatic.inkMuted,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextAction(
                text = action,
                onClick = onAction,
                compact = true,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusRowPreview() {
    WallmaticTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
        ) {
            StatusRow(
                title = "Last update failed",
                detail = "10 Nov · 11:45 PM",
                action = "Retry",
                onAction = {},
            )
        }
    }
}
