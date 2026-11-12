package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** First-run content shared by Home, Library and Album; the caller places its own action button. */
@Composable
fun EmptyAlbumLayout(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
    description: String,
    steps: List<String>,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        EmptyPreview(
            modifier = Modifier
                .weight(180f)
                .aspectRatio(Dimens.DeviceAspectRatio),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(168f)) {
            Text(
                text = eyebrow,
                style = WallmaticTextStyles.microStrong.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.wallmatic.primary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = WallmaticTextStyles.headline,
                color = MaterialTheme.wallmatic.ink,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = WallmaticTextStyles.caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.wallmatic.inkMuted,
            )

            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(52.dp))
                steps.forEachIndexed { index, step ->
                    GuideStep(
                        number = "0${index + 1}",
                        text = step,
                        showRule = index != steps.lastIndex,
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 500)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 500)
@Composable
private fun EmptyAlbumLayoutPreview() {
    WallmaticTheme {
        EmptyAlbumLayout(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
            eyebrow = "GET STARTED",
            title = "No album\nyet.",
            description = "An album is a folder of wallpapers. Create one, add a few, and Wallmatic rotates them on your schedule.",
            steps = listOf("Create an album", "Add wallpapers", "Set it active"),
        )
    }
}
