package com.axondragonscale.topty.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.topty.model.PasswordStrength
import com.axondragonscale.topty.ui.common.TextInput
import com.axondragonscale.topty.ui.theme.ToptyTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 29/01/25
 */

@Composable
fun ChangePassword(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: ChangePasswordVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChangePassword(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                ChangePasswordUiEvent.NavigateBack -> navController.popBackStack()
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePassword(
    modifier: Modifier = Modifier,
    uiState: ChangePasswordUiState,
    onEvent: (ChangePasswordUiEvent) -> Unit,
) {
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ChangePasswordUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning
            Text(
                text = "All tokens will be re-encrypted. This may take a moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Password
            TextInput(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.currentPassword,
                onValueChange = { onEvent(ChangePasswordUiEvent.CurrentPasswordChanged(it)) },
                label = { Text("Current Password") },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showCurrentPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = uiState.currentPasswordError != null,
                supportingText = uiState.currentPasswordError?.let { { Text(it) } },
                readOnly = uiState.isProcessing,
            )

            // New Password
            TextInput(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.newPassword,
                onValueChange = { onEvent(ChangePasswordUiEvent.NewPasswordChanged(it)) },
                label = { Text("New Password") },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showNewPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = uiState.newPasswordError != null,
                supportingText = uiState.newPasswordError?.let { { Text(it) } },
                readOnly = uiState.isProcessing,
            )

            // Password Strength Indicator
            if (uiState.newPassword.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { when (uiState.passwordStrength) {
                            PasswordStrength.WEAK -> 0.33f
                            PasswordStrength.MEDIUM -> 0.66f
                            PasswordStrength.STRONG -> 1.0f
                        } },
                        modifier = Modifier.fillMaxWidth(),
                        color = when (uiState.passwordStrength) {
                            PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                            PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                        },
                    )

                    Text(
                        text = "Strength: ${uiState.passwordStrength.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (uiState.passwordStrength) {
                            PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                            PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }

            // Confirm New Password
            TextInput(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ChangePasswordUiEvent.ConfirmPasswordChanged(it)) },
                label = { Text("Confirm New Password") },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword,
                supportingText = if (uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword) {
                    { Text("Passwords do not match") }
                } else null,
                readOnly = uiState.isProcessing,
            )

            // Progress Indicator
            if (uiState.isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.reEncryptionProgress != null) {
                        Text(
                            text = "Re-encrypting tokens... ${uiState.reEncryptionProgress.first}/${uiState.reEncryptionProgress.second}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { uiState.reEncryptionProgress.first.toFloat() / uiState.reEncryptionProgress.second },
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }

            // Success Message
            if (uiState.successMessage != null) {
                Text(
                    text = uiState.successMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Button
            Button(
                onClick = { onEvent(ChangePasswordUiEvent.ChangePassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isProcessing &&
                        uiState.currentPassword.isNotEmpty() &&
                        uiState.newPassword.isNotEmpty() &&
                        uiState.confirmPassword.isNotEmpty() &&
                        uiState.newPassword == uiState.confirmPassword &&
                        uiState.newPasswordError == null &&
                        uiState.currentPasswordError == null
            ) {
                Text(if (uiState.isProcessing) "Processing..." else "Change Password")
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChangePasswordPreview() {
    ToptyTheme {
        Surface {
            ChangePassword(
                uiState = ChangePasswordUiState(
                    currentPassword = "oldpass123",
                    newPassword = "NewPassword123",
                    confirmPassword = "NewPassword123",
                    passwordStrength = PasswordStrength.STRONG,
                ),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChangePasswordProcessingPreview() {
    ToptyTheme {
        Surface {
            ChangePassword(
                uiState = ChangePasswordUiState(
                    currentPassword = "oldpass123",
                    newPassword = "NewPassword123",
                    confirmPassword = "NewPassword123",
                    isProcessing = true,
                    reEncryptionProgress = Pair(45, 100)
                ),
                onEvent = {}
            )
        }
    }
}
