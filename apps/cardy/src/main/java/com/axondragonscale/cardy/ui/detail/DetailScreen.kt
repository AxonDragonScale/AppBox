/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.detail

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.emv.interpreter.EmvTagInterpreter
import com.axondragonscale.cardy.emv.interpreter.InterpretedEntry
import com.axondragonscale.cardy.emv.interpreter.TagInterpretation
import com.axondragonscale.cardy.model.ParsedTlvNode
import com.axondragonscale.cardy.repository.SerializableApplication
import com.axondragonscale.cardy.repository.SerializableCardData
import com.axondragonscale.cardy.ui.common.CardVisual
import com.axondragonscale.cardy.ui.theme.CardyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: DetailVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) navController.popBackStack()
    }

    DetailScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is DetailUiEvent.NavigateBack -> navController.popBackStack()
                else -> viewModel.onEvent(event)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    onEvent: (DetailUiEvent) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Card Details") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DetailUiEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isFreshRead) {
                        IconButton(
                            onClick = { onEvent(DetailUiEvent.SaveToVault) },
                            enabled = !uiState.isSaving,
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = if (uiState.isSaving) "Saving to Vault" else "Save to Vault",
                            )
                        }
                    } else if (uiState.isSavedCard) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit label")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (!uiState.isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No card data", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        val cardData = uiState.cardData ?: return@Scaffold
        val selectedApp = uiState.selectedApp ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Card Visual
            CardVisual(
                network = uiState.network,
                pan = cardData.primaryPan,
                expiry = cardData.primaryExpiry,
            )

            if (uiState.label.isNotBlank()) {
                Text(
                    text = uiState.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, end = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Application selector (only shown when multiple apps present)
            if (uiState.hasMultipleApps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cardData.applications.forEachIndexed { index, app ->
                        FilterChip(
                            selected = index == uiState.selectedAppIndex,
                            onClick = { onEvent(DetailUiEvent.SelectApplication(index)) },
                            label = { Text(app.label ?: app.network) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 1: Basic Info
            CollapsibleSection(title = "Basic Info") {
                DataRow(label = "PAN", value = selectedApp.pan ?: "Not available")
                DataRow(label = "Expiry", value = selectedApp.expiry ?: "Not available")
                DataRow(label = "Network", value = selectedApp.network)
            }

            // Section 2: Application Data
            CollapsibleSection(title = "Application Data") {
                DataRow(label = "AID", value = selectedApp.aid)
                DataRow(label = "Label", value = selectedApp.label ?: "Not available")
                if (selectedApp.appPriorityIndicator != null) {
                    InterpretableHexRow(label = "Priority", tagHex = "87", valueHex = selectedApp.appPriorityIndicator)
                }
                if (selectedApp.cvmList != null) {
                    InterpretableHexRow(label = "CVM List", tagHex = "8E", valueHex = selectedApp.cvmList)
                }
                if (selectedApp.aip != null) {
                    InterpretableHexRow(label = "AIP", tagHex = "82", valueHex = selectedApp.aip)
                }
                if (selectedApp.appEffectiveDate != null) {
                    InterpretableHexRow(label = "Effective", tagHex = "5F25", valueHex = selectedApp.appEffectiveDate)
                }
                if (selectedApp.appExpiryDate != null) {
                    InterpretableHexRow(label = "Expiry Date", tagHex = "5F24", valueHex = selectedApp.appExpiryDate)
                }
            }

            // Section 3: Raw TLV Data with tree/flat toggle
            RawTlvSection(nodes = uiState.selectedTlvTree)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to permanently delete this card?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onEvent(DetailUiEvent.DeleteCard)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showEditDialog) {
        var editedLabel by remember { mutableStateOf(uiState.label) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Label") },
            text = {
                OutlinedTextField(
                    value = editedLabel,
                    onValueChange = { editedLabel = it },
                    label = { Text("Card Label") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    onEvent(DetailUiEvent.UpdateLabel(editedLabel))
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// region Collapsible Section

@Composable
private fun CollapsibleSection(
    modifier: Modifier = Modifier,
    title: String,
    defaultExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                content()
            }
        }
    }
}

// endregion

// region Data Rows

@Composable
private fun DataRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InterpretableHexRow(
    modifier: Modifier = Modifier,
    label: String,
    tagHex: String,
    valueHex: String,
) {
    val valueBytes = remember(valueHex) {
        try { valueHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray() }
        catch (_: Exception) { byteArrayOf() }
    }
    val interpretation = remember(tagHex, valueBytes) {
        EmvTagInterpreter.interpret(tagHex, valueBytes)
    }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (interpretation != null) Modifier.clickable { expanded = !expanded } else Modifier),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(100.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = valueHex.chunked(2).joinToString(" "),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (interpretation?.summary != null) {
                    Text(
                        text = "\u2192 ${interpretation.summary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (interpretation != null) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (expanded && interpretation != null) {
            InterpretationDetail(
                interpretation = interpretation,
                modifier = Modifier.padding(start = 100.dp, top = 4.dp, bottom = 4.dp),
            )
        }
    }
}

// endregion

// region Raw TLV Section with Tree/Flat toggle

@Composable
private fun RawTlvSection(
    modifier: Modifier = Modifier,
    nodes: List<ParsedTlvNode>,
) {
    var expanded by remember { mutableStateOf(true) }
    var viewMode by remember { mutableIntStateOf(1) }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Raw TLV Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        label = { Text("Flat") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        label = { Text("Tree") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }

                if (viewMode == 0) {
                    TlvTree(nodes = nodes)
                } else {
                    val leafNodes = remember(nodes) { flattenLeafNodes(nodes) }
                    TlvFlatList(nodes = leafNodes)
                }
            }
        }
    }
}

/** Flatten to only leaf (non-constructed) nodes */
private fun flattenLeafNodes(nodes: List<ParsedTlvNode>): List<ParsedTlvNode> {
    val result = mutableListOf<ParsedTlvNode>()
    for (node in nodes) {
        if (node.isConstructed && node.children.isNotEmpty()) {
            result.addAll(flattenLeafNodes(node.children))
        } else {
            result.add(node)
        }
    }
    return result
}

@Composable
private fun TlvFlatList(
    modifier: Modifier = Modifier,
    nodes: List<ParsedTlvNode>,
) {
    Column(modifier = modifier) {
        nodes.forEach { node ->
            TlvNodeRow(node = node, depth = 0)
        }
    }
}

// endregion

// region TLV Tree View

@Composable
private fun TlvTree(
    modifier: Modifier = Modifier,
    nodes: List<ParsedTlvNode>,
    depth: Int = 0,
) {
    Column(modifier = modifier) {
        nodes.forEach { node ->
            TlvNodeRow(node = node, depth = depth)
        }
    }
}

@Composable
private fun TlvNodeRow(
    modifier: Modifier = Modifier,
    node: ParsedTlvNode,
    depth: Int,
) {
    var expanded by remember { mutableStateOf(false) }
    var interpretationExpanded by remember { mutableStateOf(false) }
    val hasExpandableContent = node.isConstructed || node.interpretation != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasExpandableContent) Modifier.clickable {
                        if (node.isConstructed) expanded = !expanded
                        else interpretationExpanded = !interpretationExpanded
                    }
                    else Modifier
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = node.tagHex,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.tagName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                if (!node.isConstructed) {
                    Text(
                        text = node.valueHex.chunked(2).joinToString(" "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (node.valueAscii != null) {
                        Text(
                            text = "\"${node.valueAscii}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (node.interpretation?.summary != null) {
                        Text(
                            text = "\u2192 ${node.interpretation.summary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            if (hasExpandableContent) {
                Icon(
                    imageVector = if (expanded || interpretationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (node.isConstructed && expanded) {
            TlvTree(nodes = node.children, depth = depth + 1)
        }

        if (!node.isConstructed && interpretationExpanded && node.interpretation != null) {
            InterpretationDetail(
                interpretation = node.interpretation,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
            )
        }
    }
}

// endregion

// region Interpretation Detail

@Composable
private fun InterpretationDetail(
    modifier: Modifier = Modifier,
    interpretation: TagInterpretation,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        interpretation.entries.forEach { entry ->
            InterpretedEntryRow(entry = entry)
        }
    }
}

@Composable
private fun InterpretedEntryRow(
    modifier: Modifier = Modifier,
    entry: InterpretedEntry,
    indent: Int = 0,
) {
    when (entry) {
        is InterpretedEntry.BitField -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = (indent * 12).dp, top = 1.dp, bottom = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (entry.isSet) "\u25CF" else "\u25CB",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.isSet) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "b${entry.byteNumber}.${entry.bitNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.isSet) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        is InterpretedEntry.DecodedValue -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = (indent * 12).dp, top = 2.dp, bottom = 2.dp),
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.4f),
                )
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(
                        text = entry.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    if (entry.rawHex.isNotEmpty()) {
                        Text(
                            text = entry.rawHex,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        is InterpretedEntry.StructuredGroup -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = (indent * 12).dp, top = 4.dp),
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                )
                entry.entries.forEach { subEntry ->
                    InterpretedEntryRow(entry = subEntry, indent = indent + 1)
                }
            }
        }

        is InterpretedEntry.DolEntry -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = (indent * 12).dp, top = 1.dp, bottom = 1.dp),
            ) {
                Text(
                    text = entry.tagHex,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(48.dp),
                )
                Text(
                    text = entry.tagName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${entry.requestedLength}B",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// endregion

// region Previews

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenFreshReadPreview() {
    CardyTheme {
        DetailScreen(
            uiState = DetailUiState(
                isLoaded = true,
                mode = DetailMode.FreshRead,
                label = "Visa - 4111",
                network = CardNetwork.VISA,
                cardData = SerializableCardData(
                    applications = listOf(
                        SerializableApplication(
                            aid = "A0000000031010",
                            network = "VISA",
                            label = "VISA DEBIT",
                            pan = "4111111111111111",
                            expiry = "12/27",
                            cardholderName = "JOHN DOE",
                            aip = "1900",
                            afl = "08010100",
                            cvmList = "000000000000000042034403",
                            cdol1 = "9F02069F03069F1A02",
                            cdol2 = null,
                            appPriorityIndicator = "01",
                            appEffectiveDate = "200101",
                            appExpiryDate = "271231",
                            rawTlvHex = "",
                        ),
                    ),
                    primaryNetwork = "VISA",
                    primaryPan = "4111111111111111",
                    primaryExpiry = "12/27",
                    cardholderName = "JOHN DOE",
                    timestamp = 0L,
                ),
                tlvTrees = listOf(
                    listOf(
                        ParsedTlvNode("4F", "AID", "Application Identifier", "A0000000031010", null, 7, false, emptyList()),
                        ParsedTlvNode("50", "Application Label", "Mnemonic", "564953412044454249540000", "VISA DEBIT", 12, false, emptyList()),
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenSavedPreview() {
    CardyTheme {
        DetailScreen(
            uiState = DetailUiState(
                isLoaded = true,
                mode = DetailMode.SavedCard,
                cardId = 2,
                label = "Mastercard - 4444",
                network = CardNetwork.MASTERCARD,
                lastFourDigits = "4444",
                expiry = "03/26",
                cardData = SerializableCardData(
                    applications = listOf(
                        SerializableApplication(
                            aid = "A0000000041010",
                            network = "MASTERCARD",
                            label = "MASTERCARD",
                            pan = "5500000000004444",
                            expiry = "03/26",
                            cardholderName = "JANE DOE",
                            aip = "7C00",
                            afl = "08010200",
                            cvmList = null,
                            cdol1 = null,
                            cdol2 = null,
                            rawTlvHex = "",
                        ),
                    ),
                    primaryNetwork = "MASTERCARD",
                    primaryPan = "5500000000004444",
                    primaryExpiry = "03/26",
                    cardholderName = "JANE DOE",
                    timestamp = 0L,
                ),
                tlvTrees = emptyList(),
            ),
            onEvent = {},
        )
    }
}

// endregion
