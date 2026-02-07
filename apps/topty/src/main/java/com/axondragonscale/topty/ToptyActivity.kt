package com.axondragonscale.topty

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.topty.auth.BiometricAuthManager
import com.axondragonscale.topty.model.UIMode
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.ui.ToptyApp
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class ToptyActivity : FragmentActivity() {

    private val prefsRepository: ToptyPrefsRepository by inject()
    private lateinit var biometricAuthManager: BiometricAuthManager
    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        biometricAuthManager = BiometricAuthManager(this)

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        val (initialUiMode, initialDynamicTheme, initialUiFont) = runBlocking {
            Triple(
                prefsRepository.uiMode.flow.first(),
                prefsRepository.dynamicTheme.flow.first(),
                prefsRepository.uiFont.flow.first(),
            )
        }

        val biometricLockEnabled = runBlocking { prefsRepository.biometricLock.get() }
        val preventScreenshotsEnabled = runBlocking { prefsRepository.preventScreenshots.get() }

        // Apply screenshot prevention if enabled
        if (preventScreenshotsEnabled) window.setFlags(FLAG_SECURE, FLAG_SECURE)

        // Check if biometric authentication is required
        if (biometricLockEnabled) {
            biometricAuthManager.canAuthenticate()
                .onSuccess {
                    biometricAuthManager.authenticate(
                        onSuccess = { isAuthenticated = true },
                        onFailed = {  },
                        onError = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                    )
                }
                .onFailure { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            isAuthenticated = true
        }

        keepSplashScreen = false

        setContent {
            val uiMode by prefsRepository.uiMode.flow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by prefsRepository.dynamicTheme.flow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)
            val uiFont by prefsRepository.uiFont.flow
                .collectAsStateWithLifecycle(initialValue = initialUiFont)
            val biometricLock by prefsRepository.biometricLock.flow
                .collectAsStateWithLifecycle(initialValue = biometricLockEnabled)
            val preventScreenshots by prefsRepository.preventScreenshots.flow
                .collectAsStateWithLifecycle(initialValue = preventScreenshotsEnabled)

            // Update FLAG_SECURE based on preference changes
            LaunchedEffect(preventScreenshots) {
                if (preventScreenshots) {
                    window.setFlags(FLAG_SECURE, FLAG_SECURE)
                } else {
                    window.clearFlags(FLAG_SECURE)
                }
            }

            ToptyTheme(
                darkTheme = isDarkTheme(uiMode),
                dynamicColor = dynamicTheme,
                uiFont = uiFont,
            ) {
                if (biometricLock && !isAuthenticated) {
                    BiometricLockScreen(biometricAuthManager = biometricAuthManager,)
                } else {
                    ToptyApp()
                }
            }
        }


    }

    @Composable
    private fun isDarkTheme(uiMode: UIMode) = when (uiMode) {
        UIMode.LIGHT -> false
        UIMode.DARK -> true
        UIMode.AUTO -> isSystemInDarkTheme()
    }

    @Composable
    private fun BiometricLockScreen(
        biometricAuthManager: BiometricAuthManager,
    ) {
        val authenticate = {
            biometricAuthManager.authenticate(
                onSuccess = { isAuthenticated = true },
                onError = { Toast.makeText(this@ToptyActivity, "Authentication error: $it", Toast.LENGTH_SHORT).show() },
                onFailed = { /* Individual authentication attempts failed, but user can retry */ }
            )
        }

        // Auto-trigger authentication on first appearance
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
}
