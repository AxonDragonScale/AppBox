/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.cardy.database.entity.CardEntity
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.security.BiometricAuthManager
import com.axondragonscale.cardy.security.SessionManager
import com.axondragonscale.cardy.ui.Route
import com.axondragonscale.cardy.ui.common.getNetworkGradient
import com.axondragonscale.cardy.ui.theme.CardyTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: VaultVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val biometricAuthManager: BiometricAuthManager = koinInject()
    val sessionManager: SessionManager = koinInject()
    val context = LocalContext.current

    // Only ask for biometric auth once per app session
    LaunchedEffect(Unit) {
        if (sessionManager.isAuthenticated) {
            viewModel.onEvent(VaultUiEvent.Authenticated)
            return@LaunchedEffect
        }

        val activity = context as? FragmentActivity
        if (activity != null && biometricAuthManager.canAuthenticate(activity)) {
            biometricAuthManager.authenticate(
                activity = activity,
                title = "Unlock Vault",
                subtitle = "Verify your identity to view saved cards",
            ) { result ->
                when (result) {
                    is BiometricAuthManager.AuthResult.Success -> {
                        sessionManager.markAuthenticated()
                        viewModel.onEvent(VaultUiEvent.Authenticated)
                    }
                    is BiometricAuthManager.AuthResult.Error ->
                        viewModel.onEvent(VaultUiEvent.AuthFailed(result.message))
                    is BiometricAuthManager.AuthResult.Unavailable -> {
                        sessionManager.markAuthenticated()
                        viewModel.onEvent(VaultUiEvent.Authenticated)
                    }
                }
            }
        } else {
            sessionManager.markAuthenticated()
            viewModel.onEvent(VaultUiEvent.Authenticated)
        }
    }

    VaultScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is VaultUiEvent.NavigateToDetail -> navController.navigate(Route.CardDetail(event.cardId))
                is VaultUiEvent.NavigateBack -> navController.popBackStack()
                else -> viewModel.onEvent(event)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultScreen(
    modifier: Modifier = Modifier,
    uiState: VaultUiState,
    onEvent: (VaultUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(VaultUiEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (!uiState.isAuthenticated) {
            LockScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                error = uiState.authError,
            )
        } else if (uiState.cards.isEmpty()) {
            EmptyVaultState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.cards, key = { it.id }) { card ->
                    SavedCardTile(
                        card = card,
                        onClick = { onEvent(VaultUiEvent.NavigateToDetail(card.id)) },
                        onDelete = { onEvent(VaultUiEvent.DeleteCard(card.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LockScreen(
    modifier: Modifier = Modifier,
    error: String?,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Vault Locked",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Authenticate to view saved cards",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun EmptyVaultState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No saved cards",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Tap a card to read it, then save it here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedCardTile(
    modifier: Modifier = Modifier,
    card: CardEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val network = try { CardNetwork.valueOf(card.network) } catch (_: Exception) { CardNetwork.UNKNOWN }
    val gradient = getNetworkGradient(network)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true },
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(brush = gradient),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = network.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "•••• ${card.lastFourDigits}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column {
                if (card.expiry != null) {
                    Text(
                        text = card.expiry,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = card.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete \"${card.label}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VaultScreenLockedPreview() {
    CardyTheme {
        VaultScreen(
            uiState = VaultUiState(isAuthenticated = false),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VaultScreenEmptyPreview() {
    CardyTheme {
        VaultScreen(
            uiState = VaultUiState(isAuthenticated = true, cards = emptyList()),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VaultScreenWithCardsPreview() {
    CardyTheme {
        VaultScreen(
            uiState = VaultUiState(
                isAuthenticated = true,
                cards = listOf(
                    CardEntity(
                        id = 1,
                        label = "Visa •••• 4111",
                        network = "VISA",
                        lastFourDigits = "4111",
                        expiry = "12/27",
                        timestamp = 0L,
                        encryptedData = byteArrayOf(),
                    ),
                    CardEntity(
                        id = 2,
                        label = "Mastercard •••• 4444",
                        network = "MASTERCARD",
                        lastFourDigits = "4444",
                        expiry = "03/26",
                        timestamp = 0L,
                        encryptedData = byteArrayOf(),
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}
