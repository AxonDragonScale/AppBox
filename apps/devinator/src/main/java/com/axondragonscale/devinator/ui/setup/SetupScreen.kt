package com.axondragonscale.devinator.ui.setup

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.ui.Route
import com.axondragonscale.devinator.ui.components.CodeBlock
import com.axondragonscale.devinator.ui.components.DevinatorCard
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.DevinatorTopBar
import com.axondragonscale.devinator.ui.components.IconBadge
import com.axondragonscale.devinator.ui.components.rememberCopyToClipboard
import com.axondragonscale.devinator.ui.theme.ActionTextStyle
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.SectionTitleTextStyle
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 11/06/26 */

private const val PREVIEW_PACKAGE = "com.axondragonscale.devinator"

@Composable
fun SetupScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: SetupVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SetupUiEvent.OnResume)
    }

    // Setup is a gate, not a step: Home replaces it rather than stacking on top of it.
    LaunchedEffect(uiState.permissionGranted) {
        if (uiState.permissionGranted) {
            navController.navigate(Route.Home) { popUpTo<Route.Setup> { inclusive = true } }
        }
    }

    SetupScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event -> viewModel.onEvent(event) },
    )
}

@Composable
private fun SetupScreen(
    modifier: Modifier = Modifier,
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val copy = rememberCopyToClipboard()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { DevinatorTopBar() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SetupHeader()
                WhyPermissionCard()
                SetupSteps(
                    grantCommand = uiState.grantCommand,
                    onOpenDeveloperOptions = {
                        context.startActivity(SecureSettingsPermission.developmentSettingsIntent())
                    },
                    onCopyCommand = { copy("Setup command", uiState.grantCommand) },
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.checkFailed) NotGrantedCard()

                Button(
                    onClick = { onEvent(SetupUiEvent.CheckAgain) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = "Check again", style = MaterialTheme.typography.titleSmall)
                }

                Text(
                    text = "Setup stays done until you uninstall the app.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SetupHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(
            icon = Icons.Filled.Key,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "One-time setup",
                style = SectionTitleTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "One permission, granted once over ADB.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The only place the permission is explained: what it is for, then the reassurance. Both lines stay
 * short so the card never wraps into a wall of text.
 */
@Composable
private fun WhyPermissionCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Devinator needs the Write Secure Settings permission to control Developer " +
                        "options.",
                    style = ActionTextStyle,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = "It is safe to grant and is used only to adjust Developer options.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f),
                )
            }
        }
    }
}

/** A failed check is a state of this screen, not a validation error on a field. */
@Composable
private fun NotGrantedCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Permission not granted yet.",
                style = ActionTextStyle,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun SetupSteps(
    modifier: Modifier = Modifier,
    grantCommand: String,
    onOpenDeveloperOptions: () -> Unit,
    onCopyCommand: () -> Unit,
) {
    DevinatorCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SetupStep(number = 1, title = "Enable USB debugging") {
                StepCaption(text = "Settings → System → Developer options → USB debugging")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onOpenDeveloperOptions),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(
                        text = "Open Developer options",
                        style = ActionTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            DevinatorDivider(inset = false)
            SetupStep(number = 2, title = "Run this command") {
                CodeBlock(command = grantCommand, onCopy = onCopyCommand)
                StepCaption(text = "Paste it in a terminal on the computer that has adb installed.")
            }
            DevinatorDivider(inset = false)
            SetupStep(number = 3, title = "Come back here") {
                StepCaption(text = "Devinator checks automatically when you return.")
            }
        }
    }
}

@Composable
private fun SetupStep(
    modifier: Modifier = Modifier,
    number: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun StepCaption(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetupScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        SetupScreen(uiState = SetupUiState(grantCommand = SecureSettingsPermission.grantCommand(PREVIEW_PACKAGE)), onEvent = {})
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetupScreenCheckFailedPreview() {
    DevinatorTheme(dynamicColor = false) {
        SetupScreen(
            uiState = SetupUiState(
                grantCommand = SecureSettingsPermission.grantCommand(PREVIEW_PACKAGE),
                checkFailed = true,
            ),
            onEvent = {},
        )
    }
}
