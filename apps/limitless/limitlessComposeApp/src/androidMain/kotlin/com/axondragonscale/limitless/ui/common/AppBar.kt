package com.axondragonscale.limitless.ui.common

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

/**
 * Created by Ronak Harkhani on 03/11/24
 */

data class AppBarItem(
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String,
    leadingItems: List<AppBarItem> = emptyList(),
    trailingItems: List<AppBarItem> = emptyList(),
    uiAlign: UIAlign = UIAlign.Bottom,
    height: Dp = AppBarDefaults.AppBarHeight,
) = Column(modifier = modifier) {

    if (uiAlign == UIAlign.Bottom)
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingItems.forEach { item ->
            IconButton(
                modifier = Modifier,
                onClick = item.onClick,
                enabled = item.enabled,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                )
            }
        }

        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.weight(1f))

        trailingItems.forEach { item ->
            IconButton(
                modifier = Modifier,
                onClick = item.onClick,
                enabled = item.enabled,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                )
            }
        }
    }

    if (uiAlign == UIAlign.Top)
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )

}

object AppBarDefaults {
    val AppBarHeight = 56.dp
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        Scaffold { _ ->
            Column {
                AppBar(
                    uiAlign = UIAlign.Top,
                    title = "Limitless",
                    leadingItems = listOf(
                        AppBarItem(
                            icon = Icons.Default.Settings,
                            onClick = {},
                        ),
                    ),
                    trailingItems = listOf(
                        AppBarItem(
                            icon = Icons.Default.BarChart,
                            onClick = {},
                        ),
                        AppBarItem(
                            icon = Icons.Default.AddCircle,
                            onClick = {},
                        ),
                    ),
                )

                Spacer(modifier = Modifier.weight(1f))

                AppBar(
                    uiAlign = UIAlign.Bottom,
                    title = "Limitless",
                    leadingItems = listOf(
                        AppBarItem(
                            icon = Icons.Default.Settings,
                            onClick = {},
                        ),
                    ),
                    trailingItems = listOf(
                        AppBarItem(
                            icon = Icons.Default.BarChart,
                            onClick = {},
                        ),
                        AppBarItem(
                            icon = Icons.Default.AddCircle,
                            onClick = {},
                        ),
                    ),
                )
            }
        }
    }
}
