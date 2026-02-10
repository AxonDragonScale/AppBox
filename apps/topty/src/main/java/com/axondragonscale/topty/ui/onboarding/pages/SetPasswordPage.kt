package com.axondragonscale.topty.ui.onboarding.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.topty.model.PasswordStrength
import com.axondragonscale.topty.ui.common.TextInput
import com.axondragonscale.topty.ui.onboarding.OnboardingUiEvent
import com.axondragonscale.topty.ui.theme.ToptyTheme

/**
 * Created by Ronak Harkhani on 29/01/25
 */

@Composable
fun SetPasswordPage(
    modifier: Modifier = Modifier,
    password: String = "",
    confirmPassword: String = "",
    passwordError: String? = null,
    passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    onEvent: (OnboardingUiEvent) -> Unit = {},
) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Icon(
            modifier = Modifier.padding(bottom = 32.dp).size(64.dp),
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = "Secure Your Codes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier.padding(bottom = 32.dp),
            text = "Set a password to keep your codes secure",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextInput(
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            value = password,
            onValueChange = { onEvent(OnboardingUiEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null && password.isNotEmpty(),
        )

        TextInput(
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
            value = confirmPassword,
            onValueChange = { onEvent(OnboardingUiEvent.ConfirmPasswordChanged(it)) },
            label = { Text("Confirm Password") },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
        )

        // Error Message
        if (passwordError != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = passwordError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Passwords do not match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Password Strength Indicator
        if (password.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { when (passwordStrength) {
                        PasswordStrength.WEAK -> 0.33f
                        PasswordStrength.MEDIUM -> 0.66f
                        PasswordStrength.STRONG -> 1.0f
                    } },
                    modifier = Modifier.fillMaxWidth(),
                    color = when (passwordStrength) {
                        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                        PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                    },
                )

                Text(
                    text = "Strength: ${passwordStrength.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (passwordStrength) {
                        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                        PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordPagePreview() {
    ToptyTheme {
        Surface {
            SetPasswordPage(
                password = "MyPassword123",
                confirmPassword = "MyPassword123",
                passwordStrength = PasswordStrength.MEDIUM,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordPageErrorPreview() {
    ToptyTheme {
        Surface {
            SetPasswordPage(
                password = "short",
                confirmPassword = "different",
                passwordError = "Password must be at least 8 characters",
                passwordStrength = PasswordStrength.WEAK,
            )
        }
    }
}
