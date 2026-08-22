/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.home

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NearbyError
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.cardy.ui.Route
import com.axondragonscale.cardy.ui.theme.CardyTheme
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.logD
import org.koin.androidx.compose.koinViewModel

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val activity = LocalActivity.current as FragmentActivity
    val viewModel: HomeVM = koinViewModel(viewModelStoreOwner = activity)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TAG.logD { "composing with viewModel instance=${viewModel.hashCode()}, isNfcEnabled=${uiState.isNfcEnabled}" }

    val context = LocalContext.current

    viewModel.uiEffect.collectWithLifecycle { effect ->
        TAG.logD { "received effect=$effect" }
        when (effect) {
            is HomeUiEffect.NavigateToCardDetail -> navController.navigate(Route.CardDetail())
        }
    }

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onVaultClick = { navController.navigate(Route.Vault) },
        onSettingsClick = { navController.navigate(Route.Settings) },
        onOpenNfcSettings = {
            context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        },
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onVaultClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.readError) {
        uiState.readError?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomBar(
                onVaultClick = onVaultClick,
                onSettingsClick = onSettingsClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            if (!uiState.isNfcEnabled) {
                NfcDisabledContent(
                    isNfcSupported = uiState.isNfcSupported,
                    onOpenNfcSettings = onOpenNfcSettings,
                )
            } else {
                NfcTapPrompt(isReading = uiState.isReading)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (uiState.isReading) "Reading card..." else "Tap your card",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = if (uiState.isReading) "Hold steady"
                    else "Hold your card against the back of your phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NfcDisabledContent(
    modifier: Modifier = Modifier,
    isNfcSupported: Boolean,
    onOpenNfcSettings: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.NearbyError,
                contentDescription = "NFC Disabled",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isNfcSupported) "NFC is Disabled" else "NFC Not Available",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isNfcSupported)
                "NFC is required to read payment cards. Enable it in your device settings to get started."
            else
                "This device does not support NFC. A device with NFC capability is required to read payment cards.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (isNfcSupported) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onOpenNfcSettings,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Open NFC Settings")
            }
        }
    }
}

@Composable
private fun NfcTapPrompt(
    modifier: Modifier = Modifier,
    isReading: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring1Scale",
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring2Scale",
    )
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring3Scale",
    )

    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring1Alpha",
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring2Alpha",
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring3Alpha",
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary

        if (isReading) {
            CircularProgressIndicator(
                modifier = Modifier.size(140.dp),
                color = primaryColor,
                strokeWidth = 5.dp,
            )
        } else {
            // Outermost ring (most delayed)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(ring3Scale)
                    .alpha(ring3Alpha)
                    .background(color = primaryColor.copy(alpha = 0.1f), shape = CircleShape)
            )
            // Middle ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(ring2Scale)
                    .alpha(ring2Alpha)
                    .background(color = primaryColor.copy(alpha = 0.15f), shape = CircleShape)
            )
            // Innermost ring (least delayed)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(ring1Scale)
                    .alpha(ring1Alpha)
                    .background(color = primaryColor.copy(alpha = 0.2f), shape = CircleShape)
            )
        }

        // Center icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = if (isReading) "Reading card" else "Tap card",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    onVaultClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onVaultClick) {
            Icon(
                imageVector = Icons.Default.Wallet,
                contentDescription = "Vault",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    CardyTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onVaultClick = {},
            onSettingsClick = {},
            onOpenNfcSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenNfcDisabledPreview() {
    CardyTheme {
        HomeScreen(
            uiState = HomeUiState(isNfcEnabled = false),
            onVaultClick = {},
            onSettingsClick = {},
            onOpenNfcSettings = {},
        )
    }
}
