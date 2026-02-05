package com.axondragonscale.topty.ui.qrscanner

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.common.Camera
import com.axondragonscale.topty.ui.common.QRCodeAnalyzer
import com.axondragonscale.topty.ui.common.ScannerOverlay
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import com.axondragonscale.commons.collectWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 12/10/24
 */

@Composable
fun QRScanner(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: QRScannerVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    vm.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is QRScannerUiEffect.NavigateToAddToken ->
                navController.navigate(Route.Token.Add(effect.tokenUri))
        }
    }

    QRScanner(
        modifier = modifier,
        uiState = uiState,
        onEvent = vm::onEvent,
    )
}

@Composable
private fun QRScanner(
    modifier: Modifier = Modifier,
    uiState: QRScannerUiState,
    onEvent: (QRScannerUiEvent) -> Unit,
) = Box(
    modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center,
) {
    val analyzer = remember {
        QRCodeAnalyzer { barcodes ->
            onEvent(QRScannerUiEvent.QRCodeScanned(barcodes))
        }
    }

    LaunchedEffect(Unit) {
        // Unpause scanner in case the user is coming back from the Token screen
        // This will also be fired on first composition but will have no effect
        onEvent(QRScannerUiEvent.UnpauseScanner)
    }

    LaunchedEffect(uiState.isPaused) {
        analyzer.isPaused = uiState.isPaused
    }

    Camera(
        flashEnabled = uiState.isFlashEnabled,
        analyzer = analyzer
    )

    ScannerOverlay()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    IconToggleButton(
        modifier = Modifier
            .padding(top = screenWidth * 1.2f)
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape,
            ),
        checked = uiState.isFlashEnabled,
        onCheckedChange = { onEvent(QRScannerUiEvent.ToggleFlash(it)) }
    ) {
        val icon = if (uiState.isFlashEnabled) Icons.Filled.FlashOff else Icons.Filled.FlashOn
        val contentDescription = if (uiState.isFlashEnabled) "Disable Flash" else "Enable Flash"
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }

    CameraPermission()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermission(
    modifier: Modifier = Modifier,
) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { }
    )

    if (permissionState.status.isGranted) return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Access",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Grant camera permission to scan the QR Code",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Button(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            onClick = { permissionState.launchPermissionRequest() },
            shape = DefaultShape,
        ) {
            Text(
                text = "Grant",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        val uiState by remember { mutableStateOf(QRScannerUiState()) }
        QRScanner(
            uiState = uiState,
            onEvent = { }
        )
    }
}
