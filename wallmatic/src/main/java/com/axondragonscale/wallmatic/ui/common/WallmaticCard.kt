package com.axondragonscale.wallmatic.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

/**
 * Created by Ronak Harkhani on 27/09/24
 */

@Composable
fun WallmaticCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    title: String,
    cardShape: Shape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) = Card(
    modifier = modifier.fillMaxWidth(),
    shape = cardShape,
) {
    Column(modifier = contentModifier.padding(8.dp)) {
        Text(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            text = title,
            style = MaterialTheme.typography.labelLarge,
        )

        content()
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            WallmaticCard(title = "Title") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .debugBorder()
                )
            }
        }
    }
}


