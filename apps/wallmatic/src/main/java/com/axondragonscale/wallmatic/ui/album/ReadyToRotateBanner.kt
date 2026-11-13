package com.axondragonscale.wallmatic.ui.album

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun ReadyToRotateBanner(
    modifier: Modifier = Modifier,
    targets: List<TargetScreen>,
    onSetActive: (TargetScreen) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.wallmatic.primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "READY TO ROTATE",
                style = WallmaticTextStyles.microStrong.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.wallmatic.onPrimary,
            )
            Text(
                text = "Set this album as active.",
                style = WallmaticTextStyles.body,
                color = MaterialTheme.wallmatic.onPrimary,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (TargetScreen.Both in targets) {
            SetActiveButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(120.dp),
                text = "SET AS ACTIVE",
                onClick = { onSetActive(TargetScreen.Both) },
            )
        } else {
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                targets.forEach { target ->
                    SetActiveButton(
                        modifier = Modifier.width(56.dp),
                        text = target.label,
                        onClick = { onSetActive(target) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SetActiveButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .background(MaterialTheme.wallmatic.paper)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = WallmaticTextStyles.microStrong.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.wallmatic.ink,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 120)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 120)
@Composable
private fun ReadyToRotateBannerPreview() {
    WallmaticTheme {
        BannerPreview(targets = listOf(TargetScreen.Both))
    }
}

@Preview(name = "Light Mode · Split", showBackground = true, widthDp = 412, heightDp = 120)
@Preview(name = "Dark Mode · Split", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 120)
@Composable
private fun ReadyToRotateBannerSplitPreview() {
    WallmaticTheme {
        BannerPreview(targets = listOf(TargetScreen.Home, TargetScreen.Lock))
    }
}

@Composable
private fun BannerPreview(targets: List<TargetScreen>) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.wallmatic.paper)
            .padding(24.dp),
    ) {
        ReadyToRotateBanner(
            targets = targets,
            onSetActive = {},
        )
    }
}
