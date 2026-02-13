package com.axondragonscale.qrqanvas.ui.devtools

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.qrqanvas.BuildConfig
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun QRQanvasDevTools(
    modifier: Modifier = Modifier,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "QRQanvas Dev Tools",
        subtitle = "Debug utilities for QRQanvas",
        customToolsBuilder = {
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QRQanvasDevToolsPreview() {
    QRQanvasTheme {
        Surface {
            QRQanvasDevTools()
        }
    }
}
