package com.axondragonscale.qrqanvas.ui.devtools

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.qrqanvas.BuildConfig
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun QRQanvasDevTools(
    modifier: Modifier = Modifier,
) {
    val qrCodeRepository: QRCodeRepository = koinInject()
    val scope = rememberCoroutineScope()

    QRQanvasDevTools(
        modifier = modifier,
        onAddTestData = {
            scope.launch {
                getTestQRCodes().forEach { qrCodeRepository.insert(it) }
            }
        },
        onDeleteAll = {
            scope.launch { qrCodeRepository.deleteAll() }
        },
    )
}

@Composable
private fun QRQanvasDevTools(
    modifier: Modifier = Modifier,
    onAddTestData: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
) {
    DevToolsScreen(
        modifier = modifier,
        title = "QRQanvas Dev Tools",
        subtitle = "Debug utilities for QRQanvas",
        customToolsBuilder = {
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            section("Dev Testing", Icons.Default.TouchApp)
            action(
                icon = Icons.Default.Add,
                title = "Add Test QR Codes",
                subtitle = "Add one QR code of each content type (8 total)",
                onClick = onAddTestData,
                completionAlert = "Test QR codes added!",
            )
            action(
                icon = Icons.Default.Delete,
                title = "Clear All QR Codes",
                subtitle = "Remove all QR codes from the database",
                destructive = true,
                onClick = onDeleteAll,
                completionAlert = "All QR codes deleted!",
            )
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QRQanvasDevToolsPreview() {
    QRQanvasTheme {
        Surface {
            QRQanvasDevTools(
                onAddTestData = {},
                onDeleteAll = {},
            )
        }
    }
}
