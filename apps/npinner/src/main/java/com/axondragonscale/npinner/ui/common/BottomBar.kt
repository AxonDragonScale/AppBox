package com.axondragonscale.npinner.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.npinner.ui.theme.Dimen
import com.axondragonscale.npinner.ui.theme.NPinnerTheme

/**
 * Created by Ronak Harkhani on 26/04/23
 */

@Composable
fun BottomBar(
    mainAction: @Composable RowScope.() -> Unit,
    leftAction: @Composable RowScope.() -> Unit = { Spacer(modifier = Modifier.width(48.dp)) },
    rightAction: @Composable RowScope.() -> Unit = { Spacer(modifier = Modifier.width(48.dp)) },
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.BOTTOM_BAR_HEIGHT)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Divider(modifier = Modifier.align(Alignment.TopCenter))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            leftAction()
            Spacer(modifier = Modifier.width(16.dp))
            mainAction()
            Spacer(modifier = Modifier.width(16.dp))
            rightAction()
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun BottomButton(
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(2.dp),
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun IconActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    iconDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomBarPreview() {
    NPinnerTheme {
        BottomBar(
            mainAction = {
                BottomButton(
                    modifier = Modifier.weight(1f),
                    title = "MAIN ACTION",
                    onClick = {}
                )
            },
            leftAction = {
                IconActionButton(
                    icon = Icons.Outlined.DarkMode,
                    onClick = {},
                )
            },
            rightAction = {
                IconActionButton(
                    icon = Icons.Outlined.Info,
                    onClick = {}
                )
            }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomBarMainOnlyPreview() {
    NPinnerTheme {
        BottomBar(
            mainAction = {
                BottomButton(
                    modifier = Modifier.weight(1F),
                    title = "MAIN ACTION",
                    onClick = {}
                )
            },
        )
    }
}
