package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** The ruled blank frame that stands in for a wallpaper until an album has one. */
@Composable
fun EmptyPreview(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.wallmatic.paper)
            .border(1.dp, MaterialTheme.wallmatic.ink),
        contentAlignment = Alignment.Center,
    ) {
        EmptyPreviewMark()
    }
}

/** The centred ruled square and primary dot that mark an empty wallpaper preview. */
@Composable
fun EmptyPreviewMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.wallmatic.paper)
            .border(1.dp, MaterialTheme.wallmatic.ruleSoft),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(MaterialTheme.wallmatic.primary)
        )
    }
}

@Composable
fun GuideStep(
    modifier: Modifier = Modifier,
    number: String,
    text: String,
    showRule: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = number,
                style = WallmaticTextStyles.microStrong.copy(letterSpacing = 0.sp),
                color = MaterialTheme.wallmatic.primary,
                modifier = Modifier.width(24.dp),
            )
            Text(
                text = text,
                style = WallmaticTextStyles.caption.copy(fontSize = 11.sp, lineHeight = 16.sp),
                color = MaterialTheme.wallmatic.ink,
            )
        }
        if (showRule) {
            HorizontalRule()
            Spacer(modifier = Modifier.height(11.dp))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreviewPreview() {
    WallmaticTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
        ) {
            EmptyPreview(modifier = Modifier.size(width = 180.dp, height = 400.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                GuideStep(number = "01", text = "Create an album")
                Spacer(modifier = Modifier.height(12.dp))
                GuideStep(number = "02", text = "Add wallpapers")
            }
        }
    }
}
