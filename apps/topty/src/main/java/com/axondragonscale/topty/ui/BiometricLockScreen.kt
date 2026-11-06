package com.axondragonscale.topty.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.onFailure
import com.axondragonscale.commons.onSuccess
import com.axondragonscale.topty.auth.BiometricAuthManager
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.ui.theme.DefaultShape
import kotlinx.coroutines.runBlocking

/**
 * Lock screen that requires biometric authentication to proceed.
 *
 * Created by Ronak Harkhani on 25/01/25
 */
@Composable
fun BiometricLockScreen(
    biometricAuthManager: BiometricAuthManager,
    prefsRepository: ToptyPrefsRepository,
    onAuthenticated: () -> Unit,
) {
    val context = LocalContext.current

    val authenticate: () -> Unit = {
        // Double-check availability before attempting authentication
        biometricAuthManager.canAuthenticate()
            .onSuccess {
                biometricAuthManager.authenticate(
                    onSuccess = { onAuthenticated() },
                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                    onFailed = { } // Individual authentication attempts failed. User can retry by tapping the unlock button
                )
            }
            .onFailure {
                Toast.makeText(context, "Biometric authentication is no longer available. Disabling biometric lock.", Toast.LENGTH_SHORT).show()
                runBlocking { prefsRepository.biometricLock.set(false) }
                onAuthenticated()
            }
    }

    LaunchedEffect(Unit) {
        authenticate()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Topty is Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock with biometric authentication to access your tokens",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = authenticate,
                shape = DefaultShape,
            ) {
                Text("Unlock")
            }
        }
    }
}
