package com.axondragonscale.qrqanvas.ui.creator

import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.qrqanvas.model.ColorData
import com.axondragonscale.qrqanvas.model.ContentType
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.LogoData
import com.axondragonscale.qrqanvas.model.LogoShape
import com.axondragonscale.qrqanvas.model.QRErrorCorrection
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.model.Template
import com.axondragonscale.qrqanvas.model.WifiEncryption
import com.axondragonscale.qrqanvas.ui.common.BACKGROUND_PRESET_COLORS
import com.axondragonscale.qrqanvas.ui.common.ColorPicker
import com.axondragonscale.qrqanvas.ui.common.QRPreview
import com.axondragonscale.qrqanvas.ui.common.QRPreviewStyle
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/**
 * Created by Ronak Harkhani on 09/07/25
 */

// region Top-Level Composable

@Composable
fun Creator(
    modifier: Modifier = Modifier,
    qrCodeId: Long? = null,
    onNavigateBack: () -> Unit,
) {
    val viewModel: CreatorVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(qrCodeId) {
        if (qrCodeId != null) viewModel.loadQRCode(qrCodeId)
    }

    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is CreatorUiEffect.SaveCompleted -> onNavigateBack()
            is CreatorUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    Creator(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

// endregion

// region Main Creator Screen

@Composable
private fun Creator(
    modifier: Modifier = Modifier,
    uiState: CreatorUiState,
    onEvent: (CreatorUiEvent) -> Unit,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = CreatorTab.entries

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (uiState.isEditing) "Edit QR Code" else "Create QR Code")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(CreatorUiEvent.SaveClicked) }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // QR Preview — always visible at the top
            QRPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 8.dp),
                bitmap = uiState.qrBitmap,
                style = QRPreviewStyle.Empty,
                contentPadding = 12.dp,
            )

            // Tab Row
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = tab.label) },
                        icon = {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = tab.icon,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }

            // Tab Content — scrollable per tab
            when (tabs[selectedTabIndex]) {
                CreatorTab.Content -> ContentTabContent(
                    modifier = Modifier.fillMaxSize(),
                    contentType = uiState.contentType,
                    contentInput = uiState.contentInput,
                    onEvent = onEvent,
                    qrBitmap = uiState.qrBitmap,
                )

                CreatorTab.Dots -> DotsTabContent(
                    modifier = Modifier.fillMaxSize(),
                    selectedDotStyle = uiState.style.dotStyle,
                    dotColor = uiState.style.dotColor,
                    onEvent = onEvent,
                )

                CreatorTab.Eyes -> EyesTabContent(
                    modifier = Modifier.fillMaxSize(),
                    selectedEyeStyle = uiState.style.eyeStyle,
                    eyeFrameColor = uiState.style.eyeFrameColor,
                    eyeDotColor = uiState.style.eyeDotColor,
                    onEvent = onEvent,
                )

                CreatorTab.Background -> BackgroundTabContent(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = uiState.style.backgroundColor,
                    errorCorrection = uiState.style.errorCorrection,
                    hasLogo = uiState.style.logoData != null,
                    quietZone = uiState.style.quietZone,
                    onEvent = onEvent,
                )

                CreatorTab.Logo -> LogoTabContent(
                    modifier = Modifier.fillMaxSize(),
                    logoData = uiState.style.logoData,
                    onEvent = onEvent,
                )

                CreatorTab.Templates -> TemplatesTabContent(
                    modifier = Modifier.fillMaxSize(),
                    templates = uiState.templates,
                    templateThumbnails = uiState.templateThumbnails,
                    onEvent = onEvent,
                )
            }
        }
    }

    // Save as Template dialog
    if (uiState.showSaveTemplateDialog) {
        SaveTemplateDialog(
            name = uiState.saveTemplateName,
            onNameChanged = { onEvent(CreatorUiEvent.SaveTemplateNameChanged(it)) },
            onConfirm = { onEvent(CreatorUiEvent.ConfirmSaveTemplate) },
            onDismiss = { onEvent(CreatorUiEvent.DismissSaveTemplate) },
        )
    }
}

// endregion

// region Tab Content — Content

@Composable
private fun ContentTabContent(
    modifier: Modifier = Modifier,
    contentType: ContentType,
    contentInput: ContentInputState,
    onEvent: (CreatorUiEvent) -> Unit,
    qrBitmap: Bitmap?,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Content Type")
        ContentTypeSelector(
            selectedType = contentType,
            onTypeSelected = { onEvent(CreatorUiEvent.ContentTypeSelected(it)) },
        )

        SectionHeader(text = "Input")
        ContentInputFields(
            contentInput = contentInput,
            onContentInputChanged = { onEvent(CreatorUiEvent.ContentInputChanged(it)) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Share & Download buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(CreatorUiEvent.ShareClicked) },
                enabled = qrBitmap != null,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Share")
            }

            FilledTonalButton(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(CreatorUiEvent.ExportClicked) },
                enabled = qrBitmap != null,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Download")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Tab Content — Dots

@Composable
private fun DotsTabContent(
    modifier: Modifier = Modifier,
    selectedDotStyle: DotStyle,
    dotColor: ColorData,
    onEvent: (CreatorUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Dot Style")
        DotStyleSelector(
            selectedStyle = selectedDotStyle,
            onStyleSelected = { onEvent(CreatorUiEvent.DotStyleSelected(it)) },
        )

        SectionHeader(text = "Dot Color")
        ColorPicker(
            selectedColor = dotColor,
            onColorSelected = { onEvent(CreatorUiEvent.DotColorSelected(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Tab Content — Eyes

@Composable
private fun EyesTabContent(
    modifier: Modifier = Modifier,
    selectedEyeStyle: EyeStyle,
    eyeFrameColor: ColorData,
    eyeDotColor: ColorData,
    onEvent: (CreatorUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Eye Style")
        EyeStyleSelector(
            selectedStyle = selectedEyeStyle,
            onStyleSelected = { onEvent(CreatorUiEvent.EyeStyleSelected(it)) },
        )

        SectionHeader(text = "Eye Frame Color")
        ColorPicker(
            selectedColor = eyeFrameColor,
            onColorSelected = { onEvent(CreatorUiEvent.EyeFrameColorSelected(it)) },
        )

        SectionHeader(text = "Eye Dot Color")
        ColorPicker(
            selectedColor = eyeDotColor,
            onColorSelected = { onEvent(CreatorUiEvent.EyeDotColorSelected(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Tab Content — Background

@Composable
private fun BackgroundTabContent(
    modifier: Modifier = Modifier,
    backgroundColor: ColorData,
    errorCorrection: QRErrorCorrection,
    hasLogo: Boolean,
    quietZone: Int,
    onEvent: (CreatorUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Background Color")
        ColorPicker(
            selectedColor = backgroundColor,
            presetColors = BACKGROUND_PRESET_COLORS,
            onColorSelected = { onEvent(CreatorUiEvent.BackgroundColorSelected(it)) },
        )

        SectionHeader(text = "Error Correction")
        ErrorCorrectionSelector(
            selectedLevel = errorCorrection,
            onLevelSelected = { onEvent(CreatorUiEvent.ErrorCorrectionSelected(it)) },
            isLockedByLogo = hasLogo,
        )

        SectionHeader(text = "Quiet Zone")
        QuietZoneSlider(
            quietZone = quietZone,
            onQuietZoneChanged = { onEvent(CreatorUiEvent.QuietZoneChanged(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Tab Content — Logo

@Composable
private fun LogoTabContent(
    modifier: Modifier = Modifier,
    logoData: LogoData?,
    onEvent: (CreatorUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Logo")
        LogoSection(
            logoData = logoData,
            onLogoSelected = { onEvent(CreatorUiEvent.LogoSelected(it)) },
            onLogoSizeChanged = { onEvent(CreatorUiEvent.LogoSizeChanged(it)) },
            onLogoPaddingChanged = { onEvent(CreatorUiEvent.LogoPaddingChanged(it)) },
            onLogoShapeSelected = { onEvent(CreatorUiEvent.LogoShapeSelected(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Tab Content — Templates

@Composable
private fun TemplatesTabContent(
    modifier: Modifier = Modifier,
    templates: List<Template>,
    templateThumbnails: Map<String, Bitmap>,
    onEvent: (CreatorUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(text = "Templates")
        TemplatePicker(
            templates = templates,
            templateThumbnails = templateThumbnails,
            onTemplateSelected = { onEvent(CreatorUiEvent.TemplateSelected(it)) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save as Template button
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEvent(CreatorUiEvent.SaveAsTemplateClicked) },
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Filled.Dashboard,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save as Template")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// endregion

// region Dialogs

@Composable
private fun SaveTemplateDialog(
    modifier: Modifier = Modifier,
    name: String,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Save as Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Save the current style as a reusable template.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("Template name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = name.isNotBlank(),
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

// endregion

// region Content Input Components

@Composable
private fun ContentTypeSelector(
    modifier: Modifier = Modifier,
    selectedType: ContentType,
    onTypeSelected: (ContentType) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ContentType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = { Text(text = type.label) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        imageVector = type.icon,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun ContentInputFields(
    modifier: Modifier = Modifier,
    contentInput: ContentInputState,
    onContentInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (contentInput) {
            is ContentInputState.Text -> TextInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Url -> UrlInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.WiFi -> WiFiInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Email -> EmailInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Phone -> PhoneInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Sms -> SmsInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Contact -> ContactInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )

            is ContentInputState.Location -> LocationInputFields(
                input = contentInput,
                onInputChanged = onContentInputChanged,
            )
        }
    }
}

@Composable
private fun TextInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Text,
    onInputChanged: (ContentInputState) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = input.value,
        onValueChange = { onInputChanged(input.copy(value = it)) },
        label = { Text(text = "Text") },
        placeholder = { Text(text = "Enter any text") },
        shape = RoundedCornerShape(12.dp),
        minLines = 2,
        maxLines = 5,
    )
}

@Composable
private fun UrlInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Url,
    onInputChanged: (ContentInputState) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = input.url,
        onValueChange = { onInputChanged(input.copy(url = it)) },
        label = { Text(text = "URL") },
        placeholder = { Text(text = "https://example.com") },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )
}

@Composable
private fun WiFiInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.WiFi,
    onInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.ssid,
            onValueChange = { onInputChanged(input.copy(ssid = it)) },
            label = { Text(text = "Network Name (SSID)") },
            placeholder = { Text(text = "My WiFi Network") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.password,
            onValueChange = { onInputChanged(input.copy(password = it)) },
            label = { Text(text = "Password") },
            placeholder = { Text(text = "Enter password") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                value = input.encryption.name,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Encryption") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                WifiEncryption.entries.forEach { encryption ->
                    DropdownMenuItem(
                        text = { Text(text = encryption.name) },
                        onClick = {
                            onInputChanged(input.copy(encryption = encryption))
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Email,
    onInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.address,
            onValueChange = { onInputChanged(input.copy(address = it)) },
            label = { Text(text = "Email Address") },
            placeholder = { Text(text = "user@example.com") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.subject,
            onValueChange = { onInputChanged(input.copy(subject = it)) },
            label = { Text(text = "Subject") },
            placeholder = { Text(text = "Optional subject line") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.body,
            onValueChange = { onInputChanged(input.copy(body = it)) },
            label = { Text(text = "Body") },
            placeholder = { Text(text = "Optional message body") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 4,
        )
    }
}

@Composable
private fun PhoneInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Phone,
    onInputChanged: (ContentInputState) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = input.number,
        onValueChange = { onInputChanged(input.copy(number = it)) },
        label = { Text(text = "Phone Number") },
        placeholder = { Text(text = "+1 234 567 8900") },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    )
}

@Composable
private fun SmsInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Sms,
    onInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.number,
            onValueChange = { onInputChanged(input.copy(number = it)) },
            label = { Text(text = "Phone Number") },
            placeholder = { Text(text = "+1 234 567 8900") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.message,
            onValueChange = { onInputChanged(input.copy(message = it)) },
            label = { Text(text = "Message") },
            placeholder = { Text(text = "Optional message") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 4,
        )
    }
}

@Composable
private fun ContactInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Contact,
    onInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.name,
            onValueChange = { onInputChanged(input.copy(name = it)) },
            label = { Text(text = "Name") },
            placeholder = { Text(text = "Full name") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.phone,
            onValueChange = { onInputChanged(input.copy(phone = it)) },
            label = { Text(text = "Phone") },
            placeholder = { Text(text = "+1 234 567 8900") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.email,
            onValueChange = { onInputChanged(input.copy(email = it)) },
            label = { Text(text = "Email") },
            placeholder = { Text(text = "user@example.com") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.organization,
            onValueChange = { onInputChanged(input.copy(organization = it)) },
            label = { Text(text = "Organization") },
            placeholder = { Text(text = "Company name") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.title,
            onValueChange = { onInputChanged(input.copy(title = it)) },
            label = { Text(text = "Title") },
            placeholder = { Text(text = "Job title") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.url,
            onValueChange = { onInputChanged(input.copy(url = it)) },
            label = { Text(text = "Website") },
            placeholder = { Text(text = "https://example.com") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.address,
            onValueChange = { onInputChanged(input.copy(address = it)) },
            label = { Text(text = "Address") },
            placeholder = { Text(text = "Street, City, Country") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 3,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.note,
            onValueChange = { onInputChanged(input.copy(note = it)) },
            label = { Text(text = "Note") },
            placeholder = { Text(text = "Additional notes") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 3,
        )
    }
}

@Composable
private fun LocationInputFields(
    modifier: Modifier = Modifier,
    input: ContentInputState.Location,
    onInputChanged: (ContentInputState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = input.latitude,
                onValueChange = { onInputChanged(input.copy(latitude = it)) },
                label = { Text(text = "Latitude") },
                placeholder = { Text(text = "37.7749") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = input.longitude,
                onValueChange = { onInputChanged(input.copy(longitude = it)) },
                label = { Text(text = "Longitude") },
                placeholder = { Text(text = "-122.4194") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input.label,
            onValueChange = { onInputChanged(input.copy(label = it)) },
            label = { Text(text = "Label") },
            placeholder = { Text(text = "Location name (optional)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
    }
}

// endregion

// region Dot Style Components

@Composable
private fun DotStyleSelector(
    modifier: Modifier = Modifier,
    selectedStyle: DotStyle,
    onStyleSelected: (DotStyle) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DotStyle.entries.forEach { style ->
            val isSelected = style == selectedStyle
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .background(containerColor)
                    .clickable { onStyleSelected(style) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val dotColor = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
                Canvas(
                    modifier = Modifier.size(28.dp),
                ) {
                    drawDotStylePreview(style, dotColor)
                }
            }
        }
    }
}

private fun DrawScope.drawDotStylePreview(
    style: DotStyle,
    color: androidx.compose.ui.graphics.Color,
) {
    val padding = size.width * 0.1f
    val dotSize = size.width - padding * 2

    when (style) {
        DotStyle.Square -> {
            drawRect(
                color = color,
                topLeft = Offset(padding, padding),
                size = Size(dotSize, dotSize),
            )
        }

        DotStyle.Circle -> {
            drawCircle(
                color = color,
                radius = dotSize / 2f,
                center = center,
            )
        }

        DotStyle.RoundedSquare -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(padding, padding),
                size = Size(dotSize, dotSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(dotSize * 0.3f),
            )
        }

        DotStyle.Diamond -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, padding)
                lineTo(size.width - padding, center.y)
                lineTo(center.x, size.height - padding)
                lineTo(padding, center.y)
                close()
            }
            drawPath(path, color)
        }

        DotStyle.Star -> {
            val cx = center.x
            val cy = center.y
            val outerRadius = dotSize / 2f
            val innerRadius = outerRadius * 0.38f
            val path = androidx.compose.ui.graphics.Path().apply {
                for (i in 0 until 5) {
                    val outerAngle = Math.toRadians(-90.0 + i * 72.0)
                    val ox = cx + outerRadius * kotlin.math.cos(outerAngle).toFloat()
                    val oy = cy + outerRadius * kotlin.math.sin(outerAngle).toFloat()
                    val innerAngle = Math.toRadians(-90.0 + i * 72.0 + 36.0)
                    val ix = cx + innerRadius * kotlin.math.cos(innerAngle).toFloat()
                    val iy = cy + innerRadius * kotlin.math.sin(innerAngle).toFloat()
                    if (i == 0) moveTo(ox, oy) else lineTo(ox, oy)
                    lineTo(ix, iy)
                }
                close()
            }
            drawPath(path, color)
        }

        DotStyle.Heart -> {
            val w = dotSize
            val h = dotSize
            val left = padding
            val top = padding
            val cx = center.x
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, top + h * 0.38f)
                cubicTo(
                    cx - w * 0.02f, top + h * 0.1f,
                    left, top + h * 0.05f,
                    left, top + h * 0.3f,
                )
                cubicTo(
                    left, top + h * 0.6f,
                    cx, top + h * 0.7f,
                    cx, top + h,
                )
                cubicTo(
                    cx, top + h * 0.7f,
                    left + w, top + h * 0.6f,
                    left + w, top + h * 0.3f,
                )
                cubicTo(
                    left + w, top + h * 0.05f,
                    cx + w * 0.02f, top + h * 0.1f,
                    cx, top + h * 0.38f,
                )
                close()
            }
            drawPath(path, color)
        }
    }
}

// endregion

// region Eye Style Components

@Composable
private fun EyeStyleSelector(
    modifier: Modifier = Modifier,
    selectedStyle: EyeStyle,
    onStyleSelected: (EyeStyle) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EyeStyle.entries.forEach { style ->
            val isSelected = style == selectedStyle
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .background(containerColor)
                    .clickable { onStyleSelected(style) },
                contentAlignment = Alignment.Center,
            ) {
                val drawColor = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
                Canvas(
                    modifier = Modifier.size(32.dp),
                ) {
                    drawEyeStylePreview(style, drawColor)
                }
            }
        }
    }
}

private fun DrawScope.drawEyeStylePreview(
    style: EyeStyle,
    color: androidx.compose.ui.graphics.Color,
) {
    val padding = size.width * 0.05f
    val outerSize = size.width - padding * 2
    val moduleSize = outerSize / 7f

    val outerLeft = padding
    val outerTop = padding

    when (style) {
        EyeStyle.Square -> {
            drawRect(color, Offset(outerLeft, outerTop), Size(outerSize, outerSize))
            drawRect(
                androidx.compose.ui.graphics.Color.White,
                Offset(outerLeft + moduleSize, outerTop + moduleSize),
                Size(moduleSize * 5, moduleSize * 5),
            )
            drawRect(
                color,
                Offset(outerLeft + moduleSize * 2, outerTop + moduleSize * 2),
                Size(moduleSize * 3, moduleSize * 3),
            )
        }

        EyeStyle.Circle -> {
            drawOval(color, Offset(outerLeft, outerTop), Size(outerSize, outerSize))
            drawOval(
                androidx.compose.ui.graphics.Color.White,
                Offset(outerLeft + moduleSize, outerTop + moduleSize),
                Size(moduleSize * 5, moduleSize * 5),
            )
            drawOval(
                color,
                Offset(outerLeft + moduleSize * 2, outerTop + moduleSize * 2),
                Size(moduleSize * 3, moduleSize * 3),
            )
        }

        EyeStyle.RoundedSquare -> {
            val outerRadius = androidx.compose.ui.geometry.CornerRadius(moduleSize * 2f)
            val innerRadius = androidx.compose.ui.geometry.CornerRadius(moduleSize * 1.4f)
            val dotRadius = androidx.compose.ui.geometry.CornerRadius(moduleSize * 0.9f)
            drawRoundRect(color, Offset(outerLeft, outerTop), Size(outerSize, outerSize), outerRadius)
            drawRoundRect(
                androidx.compose.ui.graphics.Color.White,
                Offset(outerLeft + moduleSize, outerTop + moduleSize),
                Size(moduleSize * 5, moduleSize * 5),
                innerRadius,
            )
            drawRoundRect(
                color,
                Offset(outerLeft + moduleSize * 2, outerTop + moduleSize * 2),
                Size(moduleSize * 3, moduleSize * 3),
                dotRadius,
            )
        }

        EyeStyle.Leaf -> {
            drawLeafPreview(color, outerLeft, outerTop, outerSize, moduleSize)
        }

        EyeStyle.Diamond -> {
            drawDiamondPreview(color, outerLeft, outerTop, outerSize, moduleSize)
        }
    }
}

private fun DrawScope.drawLeafPreview(
    color: androidx.compose.ui.graphics.Color,
    left: Float,
    top: Float,
    outerSize: Float,
    moduleSize: Float,
) {
    val outerRadius = moduleSize * 2.5f
    val innerRadius = moduleSize * 1.8f
    val dotRadius = moduleSize * 1.0f

    fun leafPath(x: Float, y: Float, w: Float, r: Float): androidx.compose.ui.graphics.Path {
        return androidx.compose.ui.graphics.Path().apply {
            moveTo(x, y + r)
            arcTo(
                androidx.compose.ui.geometry.Rect(x, y, x + r * 2, y + r * 2),
                180f, 90f, false,
            )
            lineTo(x + w, y)
            lineTo(x + w, y + w - r)
            arcTo(
                androidx.compose.ui.geometry.Rect(x + w - r * 2, y + w - r * 2, x + w, y + w),
                0f, 90f, false,
            )
            lineTo(x, y + w)
            close()
        }
    }

    drawPath(leafPath(left, top, outerSize, outerRadius), color)
    drawPath(
        leafPath(left + moduleSize, top + moduleSize, moduleSize * 5, innerRadius),
        androidx.compose.ui.graphics.Color.White,
    )
    drawPath(
        leafPath(left + moduleSize * 2, top + moduleSize * 2, moduleSize * 3, dotRadius),
        color,
    )
}

private fun DrawScope.drawDiamondPreview(
    color: androidx.compose.ui.graphics.Color,
    left: Float,
    top: Float,
    outerSize: Float,
    moduleSize: Float,
) {
    fun diamondPath(x: Float, y: Float, s: Float): androidx.compose.ui.graphics.Path {
        val cx = x + s / 2f
        val cy = y + s / 2f
        return androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, y)
            lineTo(x + s, cy)
            lineTo(cx, y + s)
            lineTo(x, cy)
            close()
        }
    }

    drawPath(diamondPath(left, top, outerSize), color)
    drawPath(
        diamondPath(left + moduleSize, top + moduleSize, moduleSize * 5),
        androidx.compose.ui.graphics.Color.White,
    )
    drawPath(
        diamondPath(left + moduleSize * 2, top + moduleSize * 2, moduleSize * 3),
        color,
    )
}

// endregion

// region Background Components

@Composable
private fun ErrorCorrectionSelector(
    modifier: Modifier = Modifier,
    selectedLevel: QRErrorCorrection,
    onLevelSelected: (QRErrorCorrection) -> Unit,
    isLockedByLogo: Boolean,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            QRErrorCorrection.entries.forEachIndexed { index, level ->
                SegmentedButton(
                    selected = level == selectedLevel,
                    onClick = { if (!isLockedByLogo) onLevelSelected(level) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = QRErrorCorrection.entries.size,
                    ),
                    enabled = !isLockedByLogo,
                ) {
                    Text(text = level.name)
                }
            }
        }

        if (isLockedByLogo) {
            Text(
                text = "Locked to High — required for logo embedding",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun QuietZoneSlider(
    modifier: Modifier = Modifier,
    quietZone: Int,
    onQuietZoneChanged: (Int) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Margin: $quietZone module${if (quietZone != 1) "s" else ""}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = quietZone.toFloat(),
            onValueChange = { onQuietZoneChanged(it.roundToInt()) },
            valueRange = 0f..4f,
            steps = 3,
        )
    }
}

// endregion

// region Logo Components

@Composable
private fun LogoSection(
    modifier: Modifier = Modifier,
    logoData: LogoData?,
    onLogoSelected: (String?) -> Unit,
    onLogoSizeChanged: (Float) -> Unit,
    onLogoPaddingChanged: (Float) -> Unit,
    onLogoShapeSelected: (LogoShape) -> Unit,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        onLogoSelected(uri?.toString())
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (logoData == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            )
                        )
                    }
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Add Logo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else {
            LogoPreview(
                logoUri = logoData.uri,
                logoShape = logoData.shape,
                onRemove = { onLogoSelected(null) },
                onChangeImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        )
                    )
                },
            )

            LogoShapeSelector(
                selectedShape = logoData.shape,
                onShapeSelected = onLogoShapeSelected,
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Size: ${(logoData.size * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = logoData.size,
                    onValueChange = onLogoSizeChanged,
                    valueRange = 0.1f..0.4f,
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Padding: ${(logoData.padding * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = logoData.padding,
                    onValueChange = onLogoPaddingChanged,
                    valueRange = 0f..0.1f,
                )
            }
        }
    }
}

@Composable
private fun LogoPreview(
    modifier: Modifier = Modifier,
    logoUri: String,
    logoShape: LogoShape,
    onRemove: () -> Unit,
    onChangeImage: () -> Unit,
) {
    val context = LocalContext.current
    val clipShape = when (logoShape) {
        LogoShape.Circle -> CircleShape
        LogoShape.RoundedRect -> RoundedCornerShape(12.dp)
        LogoShape.Square -> RoundedCornerShape(0.dp)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .size(64.dp)
                .clip(clipShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = clipShape,
                )
                .clickable(onClick = onChangeImage),
            model = ImageRequest.Builder(context)
                .data(logoUri)
                .crossfade(true)
                .build(),
            contentDescription = "Logo preview",
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Logo selected",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Tap image to change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove logo",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun LogoShapeSelector(
    modifier: Modifier = Modifier,
    selectedShape: LogoShape,
    onShapeSelected: (LogoShape) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LogoShape.entries.forEach { shape ->
            val isSelected = shape == selectedShape
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer

            val displayName = when (shape) {
                LogoShape.Circle -> "Circle"
                LogoShape.RoundedRect -> "Rounded"
                LogoShape.Square -> "Square"
            }

            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .background(containerColor)
                    .clickable { onShapeSelected(shape) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val drawColor = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
                Canvas(
                    modifier = Modifier.size(28.dp),
                ) {
                    drawLogoShapePreview(shape, drawColor)
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun DrawScope.drawLogoShapePreview(
    shape: LogoShape,
    color: androidx.compose.ui.graphics.Color,
) {
    val padding = size.width * 0.1f
    val shapeSize = size.width - padding * 2

    when (shape) {
        LogoShape.Circle -> {
            drawCircle(
                color = color,
                radius = shapeSize / 2f,
                center = center,
            )
        }

        LogoShape.RoundedRect -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(padding, padding),
                size = Size(shapeSize, shapeSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeSize * 0.2f),
            )
        }

        LogoShape.Square -> {
            drawRect(
                color = color,
                topLeft = Offset(padding, padding),
                size = Size(shapeSize, shapeSize),
            )
        }
    }
}

// endregion

// region Template Components

@Composable
private fun TemplatePicker(
    modifier: Modifier = Modifier,
    templates: List<Template>,
    templateThumbnails: Map<String, Bitmap>,
    onTemplateSelected: (QRStyle) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        templates.forEach { template ->
            val thumbnail = templateThumbnails[template.id]
            TemplateCard(
                name = template.name,
                thumbnail = thumbnail,
                isUserTemplate = !template.isPredefined,
                onClick = { onTemplateSelected(template.style) },
            )
        }
    }
}

@Composable
private fun TemplateCard(
    modifier: Modifier = Modifier,
    name: String,
    thumbnail: Bitmap?,
    isUserTemplate: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (thumbnail != null) {
            Image(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp)),
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = "$name template preview",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        } else {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUserTemplate) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

// endregion

// region Shared Components

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier.padding(start = 4.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

// endregion

// region Previews

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreatorPreview() {
    QRQanvasTheme {
        Surface {
            Creator(
                uiState = CreatorUiState(),
                onEvent = {},
            )
        }
    }
}

@Preview(name = "WiFi Light", showBackground = true)
@Preview(name = "WiFi Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreatorWiFiPreview() {
    QRQanvasTheme {
        Surface {
            Creator(
                uiState = CreatorUiState(
                    contentType = ContentType.WiFi,
                    contentInput = ContentInputState.WiFi(),
                ),
                onEvent = {},
            )
        }
    }
}

// endregion
