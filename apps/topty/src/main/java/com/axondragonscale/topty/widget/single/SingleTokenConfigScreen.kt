package com.axondragonscale.topty.widget.single

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.ui.theme.DefaultShape
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 30/12/24
 */

@Composable
fun SingleTokenConfigScreen(
    modifier: Modifier = Modifier,
    onTokenSelected: (tokenId: Long) -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: SingleTokenConfigVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SingleTokenConfigUiEffect.TokenSelected -> onTokenSelected(effect.tokenId)
                is SingleTokenConfigUiEffect.Cancel -> onCancel()
            }
        }
    }

    SingleTokenConfigScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleTokenConfigScreen(
    modifier: Modifier = Modifier,
    uiState: SingleTokenConfigUiState,
    onEvent: (SingleTokenConfigUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Select Token") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SingleTokenConfigUiEvent.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar (similar to Home screen)
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(SingleTokenConfigUiEvent.UpdateSearch(it)) },
                onClearSearch = { onEvent(SingleTokenConfigUiEvent.UpdateSearch("")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Token list
            if (uiState.filteredTokens.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "No tokens available.\nAdd a token in the app first."
                        } else {
                            "No tokens match your search"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(uiState.filteredTokens, key = { it.id }) { token ->
                        TokenSelectItem(
                            token = token,
                            onClick = { onEvent(SingleTokenConfigUiEvent.SelectToken(token.id)) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    TextField(
        modifier = modifier,
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by name, issuer, or group") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        shape = DefaultShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
private fun TokenSelectItem(
    modifier: Modifier = Modifier,
    token: Token,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Issuer
            Text(
                text = token.issuer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Name
            if (token.name.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Group
            if (token.group.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = token.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
