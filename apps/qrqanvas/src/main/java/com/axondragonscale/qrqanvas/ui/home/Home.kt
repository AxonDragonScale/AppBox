package com.axondragonscale.qrqanvas.ui.home

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.ui.Route
import com.axondragonscale.qrqanvas.util.typeIcon
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: HomeVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Home(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.NavigateToCreator -> navController.navigate(Route.Creator())
                is HomeUiEvent.NavigateToScanner -> navController.navigate(Route.Scanner)
                is HomeUiEvent.NavigateToHistory -> navController.navigate(Route.History)
                is HomeUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                is HomeUiEvent.QRCodeClicked -> navController.navigate(Route.Detail(event.qrCodeId))

                else -> viewModel.onEvent(event)
            }
        },
    )
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState = HomeUiState(),
    onEvent: (HomeUiEvent) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "QRQanvas") },
                actions = {
                    IconButton(onClick = { onEvent(HomeUiEvent.NavigateToScanner) }) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                        )
                    }
                    IconButton(onClick = { onEvent(HomeUiEvent.NavigateToHistory) }) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Scan History",
                        )
                    }
                    IconButton(onClick = { onEvent(HomeUiEvent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(HomeUiEvent.NavigateToCreator) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create QR Code",
                )
            }
        },
    ) { innerPadding ->
        if (uiState.qrCodes.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            QRCodeGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                qrCodes = uiState.qrCodes,
                thumbnails = uiState.thumbnails,
                onCardClick = { id -> onEvent(HomeUiEvent.QRCodeClicked(id)) },
            )
        }
    }
}

@Composable
private fun QRCodeGrid(
    modifier: Modifier = Modifier,
    qrCodes: List<QRCodeEntity>,
    thumbnails: Map<Long, Bitmap>,
    onCardClick: (Long) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 12.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = qrCodes,
            key = { it.id },
        ) { entity ->
            QRCodeCard(
                entity = entity,
                thumbnail = thumbnails[entity.id],
                onClick = { onCardClick(entity.id) },
            )
        }
    }
}

@Composable
private fun QRCodeCard(
    modifier: Modifier = Modifier,
    entity: QRCodeEntity,
    thumbnail: Bitmap?,
    onClick: () -> Unit,
) {
    val contentTypeIcon = remember(entity.content) {
        entity.content.typeIcon()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // QR Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (thumbnail != null) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = entity.title,
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.None,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }

            // Title and content type icon row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterStart),
                    imageVector = contentTypeIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp),
                    text = entity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "No saved QR codes yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Tap + to create your first QR code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeEmptyPreview() {
    QRQanvasTheme {
        Surface {
            Home()
        }
    }
}

@Preview(name = "Light Mode - Grid", showBackground = true)
@Preview(name = "Dark Mode - Grid", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeGridPreview() {
    QRQanvasTheme {
        Surface {
            Home(
                uiState = HomeUiState(
                    qrCodes = listOf(
                        QRCodeEntity(
                            id = 1,
                            title = "My Website",
                            content = QRContent.Url(url = "https://example.com"),
                            style = QRStyle(),
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis(),
                        ),
                        QRCodeEntity(
                            id = 2,
                            title = "Home Wi-Fi Network",
                            content = QRContent.WiFi(ssid = "MyWiFi", password = "pass"),
                            style = QRStyle(),
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis(),
                        ),
                        QRCodeEntity(
                            id = 3,
                            title = "Hello World",
                            content = QRContent.Text(value = "Hello"),
                            style = QRStyle(),
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis(),
                        ),
                    ),
                ),
            )
        }
    }
}
