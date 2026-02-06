package com.axondragonscale.topty.ui.home

import android.content.ClipData
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Code
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.HOTPCode
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.model.TOTPCode
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.common.ExpandingFabButton
import com.axondragonscale.topty.ui.common.ExpandingFabItem
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import com.axondragonscale.commons.collectWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.concurrent.timer
import kotlin.math.roundToInt

/**
 * Created by Ronak Harkhani on 22/09/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: HomeVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showNoQRFoundDialog by remember { mutableStateOf(false) }

    vm.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is HomeUiEffect.NavigateToAddToken ->
                navController.navigate(Route.Token.Add(effect.tokenUri))

            is HomeUiEffect.ShowNoQRFoundDialog ->
                showNoQRFoundDialog = true
        }
    }

    Home(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.NavigateToQRScanner -> navController.navigate(Route.QRScanner)
                HomeUiEvent.NavigateToAddToken -> navController.navigate(Route.Token.Add(null))
                HomeUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                is HomeUiEvent.NavigateToEditToken -> navController.navigate(Route.Token.Edit(event.tokenId))

                else -> vm.onEvent(event)
            }
        }
    )

    if (showNoQRFoundDialog) {
        NoQRFoundDialog(onDismiss = { showNoQRFoundDialog = false })
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val context = LocalContext.current
    var pendingDeleteTokenId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(onSettingsClick = { onEvent(HomeUiEvent.NavigateToSettings) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onSetupManually = { onEvent(HomeUiEvent.NavigateToAddToken) },
                onScanImage = { onEvent(HomeUiEvent.ScanImage(context, it)) },
                onScanQR = { onEvent(HomeUiEvent.NavigateToQRScanner) }
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.codes.isEmpty() ->
                    EmptyState(modifier = Modifier.fillMaxSize())
                else ->
                    CodeList(
                        codeDesign = uiState.codeDesign,
                        codes = uiState.codes,
                        speakOutOnTap = uiState.speakOutOnTap,
                        onRefreshCode = { index, code -> onEvent(HomeUiEvent.RefreshCode(index, code)) },
                        onEditToken = { onEvent(HomeUiEvent.NavigateToEditToken(it)) },
                        onDeleteToken = { tokenId -> pendingDeleteTokenId = tokenId },
                        onSpeakOut = { onEvent(HomeUiEvent.SpeakOut(it)) },
                    )
            }
        }

        pendingDeleteTokenId?.let { tokenId ->
            DeleteConfirmationDialog(
                onConfirm = {
                    onEvent(HomeUiEvent.DeleteToken(tokenId))
                    pendingDeleteTokenId = null
                },
                onDismiss = { pendingDeleteTokenId = null }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text("Delete this token?") },
        text = { Text("This will permanently remove the token and you won't be able to generate codes for it anymore.") },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = DefaultShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "Topty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    )
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Codes Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first code to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap the + button below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun FloatingActionButton(
    modifier: Modifier = Modifier,
    onSetupManually: () -> Unit,
    onScanImage: (Uri) -> Unit,
    onScanQR: () -> Unit,
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onScanImage(it) } }
    )

    ExpandingFabButton(
        modifier = modifier,
        items = listOf(
            ExpandingFabItem(
                icon = Icons.Outlined.Keyboard,
                label = "Setup Manually",
                onClick = onSetupManually,
            ),
            ExpandingFabItem(
                icon = Icons.Default.ImageSearch,
                label = "Scan Image",
                onClick = {
                    val request =
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    imagePickerLauncher.launch(request)
                },
            ),
            ExpandingFabItem(
                icon = Icons.Default.QrCodeScanner,
                label = "Scan QR Code",
                onClick = onScanQR,
            ),
        ),
    )
}

private enum class SwipeAnchor {
    Closed,
    OpenStart,
    OpenEnd,
}

@Composable
private fun SwipeActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    buttonWidth: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(buttonWidth)
            .fillMaxHeight()
            .clip(DefaultShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
        )
    }
}

@Composable
private fun SwipeActionsRow(
    modifier: Modifier = Modifier,
    buttonWidth: Dp,
    buttonPadding: Dp,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(buttonPadding, Alignment.Start),
    ) {
        SwipeActionButton(
            icon = Icons.Default.Delete,
            contentDescription = "Delete",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            buttonWidth = buttonWidth,
            onClick = onDelete,
        )
        SwipeActionButton(
            icon = Icons.Default.Edit,
            contentDescription = "Edit",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            buttonWidth = buttonWidth,
            onClick = onEdit,
        )
    }

    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(buttonPadding, Alignment.End),
    ) {
        SwipeActionButton(
            icon = Icons.Default.Edit,
            contentDescription = "Edit",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            buttonWidth = buttonWidth,
            onClick = onEdit,
        )
        SwipeActionButton(
            icon = Icons.Default.Delete,
            contentDescription = "Delete",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            buttonWidth = buttonWidth,
            onClick = onDelete,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CodeList(
    modifier: Modifier = Modifier,
    codeDesign: CodeDesign,
    codes: List<Code>,
    speakOutOnTap: Boolean,
    onRefreshCode: (Int, Code) -> Unit,
    onEditToken: (Long) -> Unit,
    onDeleteToken: (Long) -> Unit,
    onSpeakOut: (Code) -> Unit,
) = LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    itemsIndexed(codes) { index, code ->
        var timeRemaining by remember { mutableLongStateOf(10000) }
        DisposableEffect(code) {
            // Don't start timer if code is not time dependent
            if (code !is TOTPCode) return@DisposableEffect onDispose { }

            timeRemaining = code.periodEnd - System.currentTimeMillis()
            val timer = timer(
                initialDelay = System.currentTimeMillis().let { 1000 - it % 1000 },
                period = 1000,
            ) {
                timeRemaining = code.periodEnd - System.currentTimeMillis()
                if (timeRemaining < 100) onRefreshCode(index, code)
            }

            onDispose { timer.cancel() }
        }

        val density = LocalDensity.current
        val buttonWidth = 72.dp
        val buttonPadding = 8.dp
        val actionWidth = buttonWidth * 2 + buttonPadding * 2
        val actionWidthPx = with(density) { actionWidth.toPx() }
        val swipeState = remember(code.token.id, actionWidthPx) {
            AnchoredDraggableState(
                initialValue = SwipeAnchor.Closed,
                anchors = DraggableAnchors {
                    SwipeAnchor.OpenStart at actionWidthPx
                    SwipeAnchor.Closed at 0f
                    SwipeAnchor.OpenEnd at -actionWidthPx
                },
            )
        }

        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()
        val onRefresh = { onRefreshCode(index, code) }

        val onEdit: () -> Unit = {
            scope.launch { swipeState.animateTo(SwipeAnchor.Closed) }
            onEditToken(code.token.id)
        }
        val onDelete: () -> Unit = {
            scope.launch { swipeState.animateTo(SwipeAnchor.Closed) }
            onDeleteToken(code.token.id)
        }

        Box(modifier = Modifier.fillMaxWidth()) {

            SwipeActionsRow(
                modifier = Modifier.matchParentSize(),
                buttonWidth = buttonWidth,
                buttonPadding = buttonPadding,
                onEdit = onEdit,
                onDelete = onDelete,
            )

            val codeModifier = Modifier
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                )
                .combinedClickable(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText("Topty Code", code.code)
                                )
                            )
                            if (speakOutOnTap) onSpeakOut(code)
                        }
                    },
                    onLongClick = { onEditToken(code.token.id) }
                )

            when (codeDesign) {
                CodeDesign.A -> CodeA(codeModifier, code, timeRemaining, onRefresh)
                CodeDesign.B -> CodeB(codeModifier, code, timeRemaining, onRefresh)
                CodeDesign.C -> CodeC(codeModifier, code, timeRemaining, onRefresh)
            }
        }
    }

    item {
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun CodeA(
    modifier: Modifier = Modifier,
    code: Code,
    timeRemaining: Long,
    onRefresh: () -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = DefaultShape
        )
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
) {
    val color = if (timeRemaining < 5000)
        MaterialTheme.colorScheme.error else LocalContentColor.current

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        if (code.token.issuer.isNotBlank()) {
            Text(
                text = code.token.issuer,
                style = MaterialTheme.typography.labelLarge,
            )
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = code.token.name,
            style = MaterialTheme.typography.labelLarge,
        )

        if (code.token.protocol == Protocol.TOTP) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = (timeRemaining / 1000f).roundToInt().toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }

    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code.code,
            style = MaterialTheme.typography.displayMedium,
            color = color,
        )

        if (code.token.protocol == Protocol.HOTP) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun CodeB(
    modifier: Modifier = Modifier,
    code: Code,
    timeRemaining: Long,
    onRefresh: () -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = DefaultShape
        )
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
) {
    val color = if (timeRemaining < 5000)
        MaterialTheme.colorScheme.error else LocalContentColor.current

    Row {
        Text(
            text = code.token.name,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (code.token.issuer.isNotBlank()) {
            Text(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        shape = DefaultShape
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                text = code.token.issuer,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code.code,
            style = MaterialTheme.typography.displayMedium,
            color = color,
        )

        if (code.token.protocol == Protocol.HOTP) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
        }
    }

    if (code.token.protocol == Protocol.TOTP)
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = (timeRemaining / 1000f).roundToInt().coerceAtLeast(0).toString(),
                color = color,
                style = MaterialTheme.typography.labelSmall,
            )

            Spacer(modifier = Modifier.width(8.dp))

            val progressColor = if (timeRemaining < 5000)
                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { (timeRemaining / 30000f).coerceAtLeast(0f) },
                color = progressColor,
                trackColor = LocalContentColor.current.copy(alpha = 0.5f),
                gapSize = 2.dp,
                drawStopIndicator = {}
            )
        }
}

@Composable
private fun CodeC(
    modifier: Modifier = Modifier,
    code: Code,
    timeRemaining: Long,
    onRefresh: () -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = DefaultShape
        )
        .padding(16.dp),
) {
    val color = if (timeRemaining < 5000)
        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = DefaultShape
                )
                .size(36.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = code.token.name.first().toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = code.token.name,
                style = MaterialTheme.typography.labelLarge,
            )

            if (code.token.issuer.isNotBlank()) {
                Text(
                    text = code.token.issuer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (code.token.protocol == Protocol.TOTP) {
            val progressColor = if (timeRemaining < 5000)
                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            CircularProgressIndicator(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp),
                progress = { ((code.token.period * 1000 - timeRemaining) / 30000f).coerceAtLeast(0f) },
                color = LocalContentColor.current.copy(alpha = 0.2f),
                trackColor = progressColor,
                gapSize = 2.dp
            )
        } else if (code.token.protocol == Protocol.HOTP) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onRefresh,
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
        }
    }

    Row(modifier = Modifier.padding(top = 16.dp)) {
        code.code.forEach { digit ->
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = DefaultShape
                    )
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = digit.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun NoQRFoundDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        title = { Text("No Valid QR Code Found") },
        text = { Text("Try again with a different image") },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = DefaultShape,
            ) {
                Text(text = "Ok")
            }
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        val uiState = remember {
            HomeUiState(
                isLoading = false,
                codeDesign = CodeDesign.C,
                codes = listOf(
                    TOTPCode(
                        code = "123456",
                        token = Token.emptyToken().copy(
                            name = "AxonDragonScale",
                            issuer = "Github"
                        ),
                        periodStart = 0,
                        periodEnd = 0,
                    ),
                    HOTPCode(
                        code = "456789",
                        token = Token.emptyToken().copy(
                            name = "Ronak Harkhani",
                            issuer = "Topty",
                            protocol = Protocol.HOTP,
                        ),
                    )
                )
            )
        }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCodes() {
    ToptyTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val code1 = remember {
                    TOTPCode(
                        "123456",
                        Token.emptyToken().copy(
                            name = "AxonDragonScale",
                            issuer = "Github",
                            protocol = Protocol.TOTP,
                            period = 30,
                        ),
                        0,
                        0,
                    )
                }
                CodeA(code = code1, timeRemaining = 10000, onRefresh = {})
                CodeB(code = code1, timeRemaining = 10000, onRefresh = {})
                CodeC(code = code1, timeRemaining = 10000, onRefresh = {})

                val code2 = remember {
                    HOTPCode(
                        "123456",
                        Token.emptyToken().copy(
                            name = "AxonDragonScale",
                            issuer = "Github",
                            protocol = Protocol.HOTP,
                        ),
                    )
                }
                CodeA(code = code2, timeRemaining = 10000, onRefresh = {})
                CodeB(code = code2, timeRemaining = 10000, onRefresh = {})
                CodeC(code = code2, timeRemaining = 10000, onRefresh = {})
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmptyState() {
    ToptyTheme {
        val uiState = remember {
            HomeUiState(
                isLoading = false,
                codeDesign = CodeDesign.C,
                codes = listOf()
            )
        }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoadingState() {
    ToptyTheme {
        val uiState = remember {
            HomeUiState(
                isLoading = true,
                codeDesign = CodeDesign.C,
                codes = listOf()
            )
        }
        Home(
            uiState = uiState,
            onEvent = { }
        )
    }
}
