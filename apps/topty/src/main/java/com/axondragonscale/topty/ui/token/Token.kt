package com.axondragonscale.topty.ui.token

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AssuredWorkload
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EnhancedEncryption
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.common.TextInput
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme
import com.google.common.io.BaseEncoding
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Created by Ronak Harkhani on 14/10/24
 */

@Composable
fun Token(
    modifier: Modifier = Modifier,
    navController: NavController,
    tokenRoute: Route.Token,
) {
    val vm: TokenVM = koinViewModel(parameters = { parametersOf(tokenRoute) })
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    vm.uiEffect.collectWithLifecycle { event ->
        when (event) {
            TokenUiEffect.NavigateToHome -> navController.popBackStack(
                route = Route.Home,
                inclusive = false
            )
        }
    }

    Token(
        modifier = modifier, uiState = uiState, onEvent = { event ->
            when (event) {
                is TokenUiEvent.NavigateBack -> navController.popBackStack()

                else -> vm.onEvent(event)
            }
        })
}

@Composable
fun Token(
    modifier: Modifier = Modifier,
    uiState: TokenUiState,
    onEvent: (TokenUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier, topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(TokenUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                },
                title = {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            var isConfigurationValid by remember { mutableStateOf(false) }
            Text(
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp), text = "Configuration"
            )
            Configuration(
                uiState = uiState,
                onEvent = onEvent,
                onValidityChanged = { isConfigurationValid = it }
            )

            var isAdvancedConfigurationValid by remember { mutableStateOf(false) }
            Text(
                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
                text = "Advanced"
            )
            AdvancedConfiguration(
                uiState = uiState,
                onEvent = onEvent,
                onValidityChanged = { isAdvancedConfigurationValid = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            BottomButtons(
                isSaveEnabled = isConfigurationValid && isAdvancedConfigurationValid,
                onSave = { onEvent(TokenUiEvent.Save) },
                onCancel = { onEvent(TokenUiEvent.NavigateBack) },
            )
        }
    }
}

@Composable
private fun Configuration(
    modifier: Modifier = Modifier,
    uiState: TokenUiState,
    onEvent: (TokenUiEvent) -> Unit,
    onValidityChanged: (Boolean) -> Unit,
) = Column(
    modifier = modifier
        .background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = DefaultShape,
        )
        .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {

    val isNameValid by remember(uiState.token) { derivedStateOf { uiState.token.name.isNotBlank() } }
    TextInput(
        value = uiState.token.name,
        onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(name = it))) },
        leadingIcon = { Icon(Icons.Outlined.Person, null) },
        label = { Text("Name") },
        supportingText = if (!isNameValid) {
            { Text("Name cannot be empty") }
        } else null,
        isError = !isNameValid,
    )

    val isIssuerValid by remember(uiState.token) { derivedStateOf { uiState.token.issuer.isNotBlank() } }
    TextInput(
        value = uiState.token.issuer,
        onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(issuer = it))) },
        leadingIcon = { Icon(Icons.Outlined.AssuredWorkload, null) },
        label = { Text("Issuer") },
        supportingText = if (!isIssuerValid) {
            { Text("Issuer cannot be empty") }
        } else null,
        isError = !isIssuerValid,
    )

    LaunchedEffect(isNameValid, isIssuerValid) {
        onValidityChanged(isNameValid && isIssuerValid)
    }

    TextInput(
        value = uiState.token.group,
        onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(group = it))) },
        leadingIcon = { Icon(Icons.Outlined.Group, null) },
        label = { Text("Group") },
    )

    TextInput(
        value = uiState.token.note,
        onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(note = it))) },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
        label = { Text("Note") },
    )
}


@Composable
private fun AdvancedConfiguration(
    modifier: Modifier = Modifier,
    uiState: TokenUiState,
    onEvent: (TokenUiEvent) -> Unit,
    onValidityChanged: (Boolean) -> Unit,
) = Column(
    modifier = modifier
        .background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = DefaultShape,
        )
        .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    val isSecretValid by remember(uiState.token) {
        derivedStateOf {
            val isValid = uiState.token.secret.length >= 16 &&
                    BaseEncoding.base32().canDecode(uiState.token.secret)
            onValidityChanged(isValid)
            isValid
        }
    }
    var showSecret by remember { mutableStateOf(true) }
    TextInput(
        value = uiState.token.secret,
        onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.withSecret(it))) },
        leadingIcon = { Icon(Icons.Outlined.Key, null) },
        label = { Text("Secret") },
        trailingIcon = {
            IconButton(onClick = { showSecret = !showSecret }) {
                Icon(
                    imageVector = if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        supportingText = if (!isSecretValid) {{ Text("Secret must be valid Base32 with 16 or more chars") }} else null,
        isError = !isSecretValid,
        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Characters),
        visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        var typeMenuExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
        ) {
            TextInput(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = uiState.token.protocol.name,
                onValueChange = { },
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.Info, null) },
                label = { Text("Protocol") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            )

            ExposedDropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false },
                matchTextFieldWidth = true,
            ) {
                Protocol.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(protocol = it)))
                            typeMenuExpanded = false
                        },
                    )
                }
            }
        }

        var hashMenuExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
            expanded = hashMenuExpanded,
            onExpandedChange = { hashMenuExpanded = !hashMenuExpanded },
        ) {
            TextInput(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = uiState.token.hashAlgo.name,
                onValueChange = { },
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.EnhancedEncryption, null) },
                label = { Text("Hash") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            )

            ExposedDropdownMenu(
                expanded = hashMenuExpanded,
                onDismissRequest = { hashMenuExpanded = false },
                matchTextFieldWidth = true,
            ) {
                HashAlgo.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(hashAlgo = it)))
                            hashMenuExpanded = false
                        },
                    )
                }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (uiState.token.protocol) {
            Protocol.TOTP -> TextInput(
                modifier = Modifier.weight(1f),
                value = uiState.token.period.toString(),
                onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(period = it.toInt()))) },
                leadingIcon = { Icon(Icons.Outlined.Timer, null) },
                label = { Text("Interval") },
            )

            Protocol.HOTP -> TextInput(
                modifier = Modifier.weight(1f),
                value = uiState.token.counter.toString(),
                onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(counter = it.toLong()))) },
                leadingIcon = { Icon(Icons.Outlined.Timer, null) },
                label = { Text("Counter") },
            )
        }


        TextInput(
            modifier = Modifier.weight(1f),
            value = uiState.token.digits.toString(),
            onValueChange = { onEvent(TokenUiEvent.UpdateToken(uiState.token.copy(digits = it.toInt()))) },
            leadingIcon = { Icon(Icons.Outlined.Pin, null) },
            label = { Text("Digits") },
        )
    }
}

@Composable
private fun BottomButtons(
    modifier: Modifier = Modifier,
    isSaveEnabled: Boolean = true,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f),
            shape = DefaultShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
            onClick = onCancel,
        ) {
            Icon(Icons.Outlined.Close, null)
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Cancel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Button(
            modifier = Modifier.weight(1f),
            shape = DefaultShape,
            onClick = onSave,
            enabled = isSaveEnabled,
        ) {
            Icon(Icons.Outlined.Check, null)
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Save",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        val uiState by remember {
            mutableStateOf(
                TokenUiState(
                    tokenRoute = Route.Token.Add(null),
                    token = Token.emptyToken(),
                )
            )
        }
        Token(
            uiState = uiState,
            onEvent = { },
        )
    }
}
