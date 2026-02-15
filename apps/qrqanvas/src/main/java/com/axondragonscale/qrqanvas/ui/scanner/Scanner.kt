package com.axondragonscale.qrqanvas.ui.scanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun Scanner(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val viewModel: ScannerVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ScannerUiEvent.GalleryImagePicked(it.toString())) }
    }

    // Handle one-shot effects
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is ScannerUiEffect.CopyToClipboard -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", effect.text))
                snackbarHostState.showSnackbar("Copied to clipboard")
            }

            is ScannerUiEffect.ShareText -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Share").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }

            is ScannerUiEffect.OpenUrl -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Cannot open this content")
                }
            }

            is ScannerUiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scanner(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        onGalleryClick = { galleryLauncher.launch("image/*") },
        snackbarHostState = snackbarHostState,
    )
}

@kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun Scanner(
    modifier: Modifier = Modifier,
    uiState: ScannerUiState = ScannerUiState(),
    onEvent: (ScannerUiEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { },
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (permissionState.status.isGranted) {
                // Camera Preview
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    flashEnabled = uiState.isFlashEnabled,
                    isPaused = uiState.isScanPaused,
                    onQRCodeScanned = { rawValue ->
                        onEvent(ScannerUiEvent.QRCodeScanned(rawValue))
                    },
                )

                // Scanner Overlay (dimming + viewfinder + scan line)
                ScannerOverlay(
                    modifier = Modifier.fillMaxSize(),
                    isPaused = uiState.isScanPaused,
                )

                // Bottom Controls
                BottomControls(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    flashEnabled = uiState.isFlashEnabled,
                    onFlashToggle = { onEvent(ScannerUiEvent.ToggleFlash) },
                    onGalleryClick = onGalleryClick,
                )
            } else {
                // Permission not granted — show request UI
                CameraPermissionRequest(
                    modifier = Modifier.fillMaxSize(),
                    showRationale = permissionState.status.shouldShowRationale,
                    onRequestPermission = { permissionState.launchPermissionRequest() },
                )
            }
        }

        // Result Bottom Sheet
        if (uiState.showResultSheet && uiState.scannedContent != null) {
            ScanResultSheet(
                content = uiState.scannedContent,
                rawValue = uiState.scannedRawValue ?: "",
                onDismiss = { onEvent(ScannerUiEvent.DismissResult) },
                onCopy = { onEvent(ScannerUiEvent.CopyResult) },
                onShare = { onEvent(ScannerUiEvent.ShareResult) },
                onOpen = { onEvent(ScannerUiEvent.OpenResult) },
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    isPaused: Boolean,
    onQRCodeScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzer = remember {
        QRCodeImageAnalyzer(
            onQRCodeDetected = { rawValue ->
                onQRCodeScanned(rawValue)
            }
        )
    }

    LaunchedEffect(isPaused) {
        analyzer.isPaused = isPaused
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
            bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(flashEnabled) {
        cameraController.cameraControl?.enableTorch(flashEnabled)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                controller = cameraController
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
    )
}

/**
 * ML Kit barcode analyzer for CameraX [ImageAnalysis].
 * Detects QR codes from the camera feed and invokes [onQRCodeDetected] with the raw value.
 */
@ExperimentalGetImage
private class QRCodeImageAnalyzer(
    private val onQRCodeDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    var isPaused = false

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused || imageProxy.image == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees,
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstOrNull()?.rawValue
                if (rawValue != null && !isPaused) {
                    isPaused = true
                    onQRCodeDetected(rawValue)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

@Composable
private fun ScannerOverlay(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
) {
    val horizontalMargin = 48.dp
    val cornerRadius = 24.dp
    val verticalOffset = 40.dp

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLine",
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val marginPx = horizontalMargin.toPx()
        val vfSizePx = size.width - 2 * marginPx
        val crPx = cornerRadius.toPx()
        val offsetPx = verticalOffset.toPx()

        val left = marginPx
        val top = (size.height - vfSizePx) / 2f - offsetPx

        // Dim overlay
        drawRect(color = Color.Black.copy(alpha = 0.45f))

        // Cut out the viewfinder area
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(left, top),
            size = Size(vfSizePx, vfSizePx),
            cornerRadius = CornerRadius(crPx),
            blendMode = BlendMode.Clear,
        )

        // Viewfinder border
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(left, top),
            size = Size(vfSizePx, vfSizePx),
            cornerRadius = CornerRadius(crPx),
            style = Stroke(width = 3.dp.toPx()),
        )

        // Animated scanning line
        if (!isPaused) {
            val inset = 20.dp.toPx()
            val lineY = top + inset + scanLineProgress * (vfSizePx - 2 * inset)

            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = 0.6f),
                        primaryColor,
                        primaryColor.copy(alpha = 0.6f),
                        Color.Transparent,
                    ),
                    startX = left + inset,
                    endX = left + vfSizePx - inset,
                ),
                start = Offset(left + inset, lineY),
                end = Offset(left + vfSizePx - inset, lineY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onFlashToggle,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = if (flashEnabled) Icons.Filled.FlashOn
                else Icons.Filled.FlashOff,
                contentDescription = if (flashEnabled) "Turn off flash"
                else "Turn on flash",
            )
        }

        FilledTonalIconButton(
            onClick = onGalleryClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = "Scan from gallery",
            )
        }
    }
}

@Composable
private fun CameraPermissionRequest(
    modifier: Modifier = Modifier,
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "Camera Access Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (showRationale) {
                    "Camera permission is needed to scan QR codes. Please grant the permission to continue."
                } else {
                    "Grant camera permission to scan QR codes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRequestPermission) {
                Text(text = "Grant Permission")
            }
        }
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanResultSheet(
    modifier: Modifier = Modifier,
    content: QRContent,
    rawValue: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header with content type icon and label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = contentTypeIcon(content),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(
                        text = contentTypeLabel(content),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Scanned successfully",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            // Content summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(16.dp),
            ) {
                Text(
                    text = contentSummary(content, rawValue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy")
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Share")
                }

                if (isOpenable(content)) {
                    FilledTonalButton(
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = openLabel(content))
                    }
                }
            }

            // Scan again button
            Spacer(modifier = Modifier.height(4.dp))

            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Scan Again")
            }
        }
    }
}

/**
 * Returns an icon representing the [QRContent] type.
 */
private fun contentTypeIcon(content: QRContent): ImageVector = when (content) {
    is QRContent.Url -> Icons.Filled.Link
    is QRContent.WiFi -> Icons.Filled.Wifi
    is QRContent.Email -> Icons.Filled.Email
    is QRContent.Phone -> Icons.Filled.Phone
    is QRContent.Sms -> Icons.Filled.Sms
    is QRContent.Contact -> Icons.Filled.Person
    is QRContent.Location -> Icons.Filled.LocationOn
    is QRContent.Text -> Icons.Filled.TextFields
}

/**
 * Returns a human-readable label for the [QRContent] type.
 */
private fun contentTypeLabel(content: QRContent): String = when (content) {
    is QRContent.Url -> "URL"
    is QRContent.WiFi -> "Wi-Fi Network"
    is QRContent.Email -> "Email"
    is QRContent.Phone -> "Phone Number"
    is QRContent.Sms -> "SMS"
    is QRContent.Contact -> "Contact"
    is QRContent.Location -> "Location"
    is QRContent.Text -> "Text"
}

/**
 * Returns a formatted summary string for the scanned content.
 */
private fun contentSummary(content: QRContent, rawValue: String): String = when (content) {
    is QRContent.Url -> content.url
    is QRContent.WiFi -> buildString {
        appendLine("SSID: ${content.ssid}")
        appendLine("Security: ${content.encryptionType.name}")
        if (content.password.isNotEmpty()) appendLine("Password: ${content.password}")
        if (content.hidden) append("Hidden: Yes")
    }.trimEnd()
    is QRContent.Email -> buildString {
        appendLine("To: ${content.address}")
        content.subject?.let { appendLine("Subject: $it") }
        content.body?.let { append("Body: $it") }
    }.trimEnd()
    is QRContent.Phone -> content.number
    is QRContent.Sms -> buildString {
        appendLine("To: ${content.number}")
        content.message?.let { append("Message: $it") }
    }.trimEnd()
    is QRContent.Contact -> buildString {
        appendLine("Name: ${content.name}")
        content.phone?.let { appendLine("Phone: $it") }
        content.email?.let { appendLine("Email: $it") }
        content.organization?.let { appendLine("Organization: $it") }
        content.title?.let { appendLine("Title: $it") }
        content.url?.let { appendLine("URL: $it") }
        content.address?.let { appendLine("Address: $it") }
        content.note?.let { append("Note: $it") }
    }.trimEnd()
    is QRContent.Location -> buildString {
        appendLine("Lat: ${content.latitude}")
        appendLine("Lng: ${content.longitude}")
        content.label?.let { append("Label: $it") }
    }.trimEnd()
    is QRContent.Text -> content.value
}

/**
 * Whether the content type supports an "Open" action.
 */
private fun isOpenable(content: QRContent): Boolean = when (content) {
    is QRContent.Url, is QRContent.Phone, is QRContent.Email,
    is QRContent.Sms, is QRContent.Location -> true
    else -> false
}

/**
 * Returns the label for the "Open" action button depending on content type.
 */
private fun openLabel(content: QRContent): String = when (content) {
    is QRContent.Url -> "Open"
    is QRContent.Phone -> "Call"
    is QRContent.Email -> "Email"
    is QRContent.Sms -> "SMS"
    is QRContent.Location -> "Map"
    else -> "Open"
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScannerPreview() {
    QRQanvasTheme {
        androidx.compose.material3.Surface {
            Scanner()
        }
    }
}

@Preview(name = "Permission Request", showBackground = true)
@Composable
private fun PermissionRequestPreview() {
    QRQanvasTheme {
        CameraPermissionRequest(
            showRationale = false,
            onRequestPermission = {},
        )
    }
}
