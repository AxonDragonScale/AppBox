package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun Masthead(
    modifier: Modifier = Modifier,
    section: String,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.mastheadHeight)
                .padding(horizontal = Dimens.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "WALLMATIC",
                style = WallmaticTextStyles.micro.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.wallmatic.ink,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = section.uppercase(Locale.ROOT),
                style = WallmaticTextStyles.micro,
                color = MaterialTheme.wallmatic.inkMuted,
            )
        }

        HorizontalRule(
            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            color = MaterialTheme.wallmatic.ink,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MastheadPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            Masthead(section = "Home")
        }
    }
}
