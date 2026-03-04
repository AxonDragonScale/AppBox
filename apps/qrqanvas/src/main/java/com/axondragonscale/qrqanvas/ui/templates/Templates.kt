package com.axondragonscale.qrqanvas.ui.templates

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.qrqanvas.model.Template
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 09/07/25
 */

@Composable
fun Templates(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val viewModel: TemplatesVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-shot effects
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is TemplatesUiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Templates(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun Templates(
    modifier: Modifier = Modifier,
    uiState: TemplatesUiState = TemplatesUiState(),
    onEvent: (TemplatesUiEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Templates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Predefined Templates Section
            if (uiState.predefinedTemplates.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(text = "Predefined")
                }

                items(
                    items = uiState.predefinedTemplates,
                    key = { it.id },
                ) { template ->
                    PredefinedTemplateCard(
                        template = template,
                        thumbnail = uiState.templateThumbnails[template.id],
                    )
                }
            }

            // User Templates Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "My Templates",
                )
            }

            if (uiState.userTemplates.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    UserTemplatesEmptyState()
                }
            } else {
                items(
                    items = uiState.userTemplates,
                    key = { it.id },
                ) { template ->
                    UserTemplateCard(
                        template = template,
                        thumbnail = uiState.templateThumbnails[template.id],
                        onRename = {
                            onEvent(
                                TemplatesUiEvent.RenameClicked(
                                    templateId = template.id,
                                    currentName = template.name,
                                )
                            )
                        },
                        onDelete = {
                            onEvent(
                                TemplatesUiEvent.DeleteClicked(
                                    templateId = template.id,
                                    name = template.name,
                                )
                            )
                        },
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Rename dialog
    if (uiState.showRenameDialog) {
        RenameTemplateDialog(
            name = uiState.renameTemplateName,
            onNameChanged = { onEvent(TemplatesUiEvent.RenameNameChanged(it)) },
            onConfirm = { onEvent(TemplatesUiEvent.ConfirmRename) },
            onDismiss = { onEvent(TemplatesUiEvent.DismissRename) },
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        DeleteTemplateDialog(
            templateName = uiState.deleteTemplateName,
            onConfirm = { onEvent(TemplatesUiEvent.ConfirmDelete) },
            onDismiss = { onEvent(TemplatesUiEvent.DismissDelete) },
        )
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier.padding(start = 4.dp, bottom = 4.dp),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun PredefinedTemplateCard(
    modifier: Modifier = Modifier,
    template: Template,
    thumbnail: Bitmap?,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TemplateThumbnail(
                thumbnail = thumbnail,
                contentDescription = "${template.name} template preview",
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun UserTemplateCard(
    modifier: Modifier = Modifier,
    template: Template,
    thumbnail: Bitmap?,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { },
                        onLongClick = { showContextMenu = true },
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TemplateThumbnail(
                    thumbnail = thumbnail,
                    contentDescription = "${template.name} template preview",
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Rename") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        onRename()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun TemplateThumbnail(
    modifier: Modifier = Modifier,
    thumbnail: Bitmap?,
    contentDescription: String?,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnail != null) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        } else {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Filled.QrCode2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun UserTemplatesEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Filled.Dashboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "No custom templates yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Save a style from the Creator as a template",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RenameTemplateDialog(
    modifier: Modifier = Modifier,
    name: String,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Rename Template") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("Template name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = name.isNotBlank(),
            ) {
                Text(text = "Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
private fun DeleteTemplateDialog(
    modifier: Modifier = Modifier,
    templateName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Template") },
        text = {
            Text(
                text = "Are you sure you want to delete \"$templateName\"? This action cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TemplatesEmptyPreview() {
    QRQanvasTheme {
        androidx.compose.material3.Surface {
            Templates()
        }
    }
}
