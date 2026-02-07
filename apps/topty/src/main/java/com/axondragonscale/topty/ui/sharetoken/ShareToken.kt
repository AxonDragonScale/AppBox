package com.axondragonscale.topty.ui.sharetoken

import android.content.ClipData
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.qr.QRGenerator
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Created by Ronak Harkhani on 28/12/24
 */

@Composable
fun ShareToken(
    modifier: Modifier = Modifier,
    navController: NavController,
    tokenId: Long,
) {
    val viewModel: ShareTokenVM = koinViewModel { parametersOf(tokenId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is ShareTokenUiEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is ShareTokenUiEffect.ShareUri -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.uri)
                    putExtra(Intent.EXTRA_TITLE, "Share Token URI")
                }
                context.startActivity(Intent.createChooser(intent, "Share Token URI"))
            }
        }
    }

    ShareToken(
        modifier = modifier,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = { viewModel.onEvent(it) },
        onBack = { navController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareToken(
    modifier: Modifier = Modifier,
    uiState: ShareTokenUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ShareTokenUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Share Token") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.error != null ->
                    ErrorState(modifier = Modifier.align(Alignment.Center), message = uiState.error)

                uiState.token != null && uiState.qrBitmap != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.token.issuer.isNotBlank()) {
                                Text(
                                    text = uiState.token.issuer,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (uiState.token.name.isNotBlank()) {
                                Text(
                                    text = uiState.token.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .padding(vertical = 32.dp)
                                .padding(horizontal = 20.dp)
                                .aspectRatio(1f),
                            shape = DefaultShape,
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = uiState.qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Text(
                            modifier = Modifier.padding(bottom = 32.dp),
                            text = "Scan this QR code to add the token",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                shape = DefaultShape,
                                onClick = {
                                    scope.launch {
                                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Token URI", uiState.tokenUri)))
                                    }
                                    onEvent(ShareTokenUiEvent.CopyUri)
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp).size(18.dp),
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                )
                                Text("Copy URI")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                shape = DefaultShape,
                                onClick = { onEvent(ShareTokenUiEvent.ShareUri) },
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp).size(18.dp),
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                )
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareTokenPreview() {
    ToptyTheme {
        Surface {
            ShareToken(
                uiState = ShareTokenUiState(
                    isLoading = false,
                    token = Token.emptyToken().copy(
                        name = "Name",
                        issuer = "Issuer",
                    ),
                    qrBitmap = QRGenerator.generateQRCode("otpauth://totp/Issuer:Name?secret=SECRET&issuer=Issuer&algorithm=SHA1&digits=6&period=30"),
                    tokenUri = "otpauth://totp/Issuer:Name?secret=SECRET&issuer=Issuer&algorithm=SHA1&digits=6&period=30"
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareTokenLoadingPreview() {
    ToptyTheme {
        Surface {
            ShareToken(
                uiState = ShareTokenUiState(isLoading = true),
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareTokenErrorPreview() {
    ToptyTheme {
        Surface {
            ShareToken(
                uiState = ShareTokenUiState(
                    isLoading = false,
                    error = "Token not found"
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onBack = {}
            )
        }
    }
}
