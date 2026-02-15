package com.axondragonscale.qrqanvas.ui.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.qrqanvas.model.ColorData
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Created by Ronak Harkhani on 13/02/26
 *
 * A color picker component supporting [ColorData.Solid], [ColorData.LinearGradient],
 * and [ColorData.RadialGradient] modes. Users can switch between modes via segmented buttons,
 * pick preset or custom hex colors, and configure gradient angle (linear) or center (radial).
 */

private val PRESET_COLORS = listOf(
    0xFF000000L, // Black
    0xFFFFFFFF, // White
    0xFFF44336, // Red
    0xFFE91E63, // Pink
    0xFF9C27B0, // Purple
    0xFF673AB7, // Deep Purple
    0xFF3F51B5, // Indigo
    0xFF2196F3, // Blue
    0xFF03A9F4, // Light Blue
    0xFF00BCD4, // Cyan
    0xFF009688, // Teal
    0xFF4CAF50, // Green
    0xFF8BC34A, // Light Green
    0xFFCDDC39, // Lime
    0xFFFFEB3B, // Yellow
    0xFFFFC107, // Amber
    0xFFFF9800, // Orange
    0xFFFF5722, // Deep Orange
    0xFF795548, // Brown
    0xFF607D8B, // Blue Grey
)

private enum class ColorMode(val label: String) {
    Solid("Solid"),
    Linear("Linear"),
    Radial("Radial"),
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    selectedColor: ColorData,
    onColorSelected: (ColorData) -> Unit,
) {
    var mode by remember(selectedColor) {
        mutableStateOf(
            when (selectedColor) {
                is ColorData.Solid -> ColorMode.Solid
                is ColorData.LinearGradient -> ColorMode.Linear
                is ColorData.RadialGradient -> ColorMode.Radial
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Mode selector
        ColorModeSelector(
            selectedMode = mode,
            onModeSelected = { newMode ->
                mode = newMode
                // Convert current config to new mode with sensible defaults
                val converted = convertColorConfig(selectedColor, newMode)
                onColorSelected(converted)
            },
        )

        // Mode-specific content
        AnimatedContent(
            targetState = mode,
            label = "ColorPickerMode",
        ) { currentMode ->
            when (currentMode) {
                ColorMode.Solid -> SolidColorPicker(
                    selectedColor = selectedColor.asSolidOrFirst(),
                    onColorSelected = { onColorSelected(ColorData.Solid(it)) },
                )

                ColorMode.Linear -> LinearGradientPicker(
                    config = selectedColor as? ColorData.LinearGradient
                        ?: ColorData.LinearGradient(
                            colors = listOf(0xFF000000L, 0xFFFFFFFF),
                            angleDegrees = 0f,
                        ),
                    onConfigChanged = onColorSelected,
                )

                ColorMode.Radial -> RadialGradientPicker(
                    config = selectedColor as? ColorData.RadialGradient
                        ?: ColorData.RadialGradient(
                            colors = listOf(0xFF000000L, 0xFFFFFFFF),
                            centerX = 0.5f,
                            centerY = 0.5f,
                        ),
                    onConfigChanged = onColorSelected,
                )
            }
        }
    }
}

@Composable
private fun ColorModeSelector(
    modifier: Modifier = Modifier,
    selectedMode: ColorMode,
    onModeSelected: (ColorMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ColorMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ColorMode.entries.size,
                ),
            ) {
                Text(text = mode.label)
            }
        }
    }
}

// region Solid Color Picker

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SolidColorPicker(
    modifier: Modifier = Modifier,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
) {
    val isCustomColor = selectedColor !in PRESET_COLORS
    var showCustomInput by remember { mutableStateOf(isCustomColor) }
    var customHex by remember(selectedColor) {
        mutableStateOf(
            if (isCustomColor) selectedColor.toHexString() else ""
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PRESET_COLORS.forEach { colorLong ->
                ColorSwatch(
                    color = colorLong,
                    isSelected = colorLong == selectedColor && !showCustomInput,
                    onClick = {
                        showCustomInput = false
                        onColorSelected(colorLong)
                    },
                )
            }

            // Custom color swatch
            CustomColorSwatch(
                isSelected = showCustomInput || isCustomColor,
                selectedArgb = if (isCustomColor) selectedColor else null,
                onClick = { showCustomInput = !showCustomInput },
            )
        }

        AnimatedVisibility(visible = showCustomInput) {
            HexColorInput(
                hex = customHex,
                onHexChanged = { customHex = it },
                onColorConfirmed = { colorLong -> onColorSelected(colorLong) },
            )
        }
    }
}

// endregion

// region Linear Gradient Picker

@Composable
private fun LinearGradientPicker(
    modifier: Modifier = Modifier,
    config: ColorData.LinearGradient,
    onConfigChanged: (ColorData) -> Unit,
) {
    val startColor = config.colors.getOrElse(0) { 0xFF000000L }
    val endColor = config.colors.getOrElse(1) { 0xFFFFFFFF }
    var angle by remember(config.angleDegrees) { mutableFloatStateOf(config.angleDegrees) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Gradient preview
        GradientPreview(
            colorData = ColorData.LinearGradient(
                colors = listOf(startColor, endColor),
                angleDegrees = angle,
            ),
        )

        // Start color
        GradientColorSection(
            label = "Start Color",
            selectedColor = startColor,
            onColorSelected = { color ->
                onConfigChanged(
                    ColorData.LinearGradient(
                        colors = listOf(color, endColor),
                        angleDegrees = angle,
                    )
                )
            },
        )

        // End color
        GradientColorSection(
            label = "End Color",
            selectedColor = endColor,
            onColorSelected = { color ->
                onConfigChanged(
                    ColorData.LinearGradient(
                        colors = listOf(startColor, color),
                        angleDegrees = angle,
                    )
                )
            },
        )

        // Angle slider
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Angle: ${angle.roundToInt()}°",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = angle,
                onValueChange = { newAngle ->
                    angle = newAngle
                    onConfigChanged(
                        ColorData.LinearGradient(
                            colors = listOf(startColor, endColor),
                            angleDegrees = newAngle,
                        )
                    )
                },
                valueRange = 0f..360f,
                steps = 7, // 0, 45, 90, 135, 180, 225, 270, 315, 360
            )
        }
    }
}

// endregion

// region Radial Gradient Picker

@Composable
private fun RadialGradientPicker(
    modifier: Modifier = Modifier,
    config: ColorData.RadialGradient,
    onConfigChanged: (ColorData) -> Unit,
) {
    val centerColor = config.colors.getOrElse(0) { 0xFF000000L }
    val edgeColor = config.colors.getOrElse(1) { 0xFFFFFFFF }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Gradient preview
        GradientPreview(
            colorData = ColorData.RadialGradient(
                colors = listOf(centerColor, edgeColor),
                centerX = config.centerX,
                centerY = config.centerY,
            ),
        )

        // Center color
        GradientColorSection(
            label = "Center Color",
            selectedColor = centerColor,
            onColorSelected = { color ->
                onConfigChanged(
                    ColorData.RadialGradient(
                        colors = listOf(color, edgeColor),
                        centerX = config.centerX,
                        centerY = config.centerY,
                    )
                )
            },
        )

        // Edge color
        GradientColorSection(
            label = "Edge Color",
            selectedColor = edgeColor,
            onColorSelected = { color ->
                onConfigChanged(
                    ColorData.RadialGradient(
                        colors = listOf(centerColor, color),
                        centerX = config.centerX,
                        centerY = config.centerY,
                    )
                )
            },
        )
    }
}

// endregion

// region Gradient Common Components

@Composable
private fun GradientPreview(
    modifier: Modifier = Modifier,
    colorData: ColorData,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .drawBehind {
                val brush = when (colorData) {
                    is ColorData.Solid -> {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(colorData.color.toInt()),
                                Color(colorData.color.toInt()),
                            ),
                        )
                    }

                    is ColorData.LinearGradient -> {
                        val colors = colorData.colors.map { Color(it.toInt()) }
                        val angleRad = Math.toRadians(colorData.angleDegrees.toDouble())
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val halfW = size.width / 2f
                        val halfH = size.height / 2f
                        val diagonal = kotlin.math.sqrt(halfW * halfW + halfH * halfH)
                        val dx = cos(angleRad).toFloat() * diagonal
                        val dy = sin(angleRad).toFloat() * diagonal
                        Brush.linearGradient(
                            colors = colors,
                            start = Offset(cx - dx, cy - dy),
                            end = Offset(cx + dx, cy + dy),
                        )
                    }

                    is ColorData.RadialGradient -> {
                        val colors = colorData.colors.map { Color(it.toInt()) }
                        val cx = size.width * colorData.centerX
                        val cy = size.height * colorData.centerY
                        val maxDx = maxOf(cx, size.width - cx)
                        val maxDy = maxOf(cy, size.height - cy)
                        val radius = kotlin.math.sqrt(maxDx * maxDx + maxDy * maxDy)
                        Brush.radialGradient(
                            colors = colors,
                            center = Offset(cx, cy),
                            radius = radius,
                        )
                    }
                }
                drawRect(brush)
            },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GradientColorSection(
    modifier: Modifier = Modifier,
    label: String,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
) {
    val isCustomColor = selectedColor !in PRESET_COLORS
    var showCustomInput by remember { mutableStateOf(false) }
    var customHex by remember(selectedColor) {
        mutableStateOf(
            if (isCustomColor) selectedColor.toHexString() else ""
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(selectedColor.toInt()), CircleShape)
                    .then(
                        if (Color(selectedColor.toInt()).luminance() > 0.85f) {
                            Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape,
                            )
                        } else Modifier
                    ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PRESET_COLORS.forEach { colorLong ->
                ColorSwatch(
                    color = colorLong,
                    isSelected = colorLong == selectedColor && !showCustomInput,
                    onClick = {
                        showCustomInput = false
                        onColorSelected(colorLong)
                    },
                )
            }

            CustomColorSwatch(
                isSelected = showCustomInput || isCustomColor,
                selectedArgb = if (isCustomColor) selectedColor else null,
                onClick = { showCustomInput = !showCustomInput },
            )
        }

        AnimatedVisibility(visible = showCustomInput) {
            HexColorInput(
                hex = customHex,
                onHexChanged = { customHex = it },
                onColorConfirmed = { colorLong -> onColorSelected(colorLong) },
            )
        }
    }
}

// endregion

// region Shared Swatch Components

@Composable
private fun ColorSwatch(
    modifier: Modifier = Modifier,
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val composeColor = Color(color.toInt())
    val needsOutline = composeColor.luminance() > 0.85f

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(composeColor, CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else if (needsOutline) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            val checkColor = if (composeColor.luminance() > 0.5f) Color.Black else Color.White
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = checkColor,
            )
        }
    }
}

@Composable
private fun CustomColorSwatch(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    selectedArgb: Long?,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selectedArgb != null) Color(selectedArgb.toInt())
    else MaterialTheme.colorScheme.surfaceContainer
    val needsOutline = selectedArgb == null || Color(selectedArgb.toInt()).luminance() > 0.85f

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else if (needsOutline) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val iconTint = when {
            selectedArgb != null && Color(selectedArgb.toInt()).luminance() > 0.5f -> Color.Black
            selectedArgb != null -> Color.White
            else -> MaterialTheme.colorScheme.onSurface
        }
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Filled.Edit,
            contentDescription = "Custom color",
            tint = iconTint,
        )
    }
}

// endregion

// region Hex Input

@Composable
private fun HexColorInput(
    modifier: Modifier = Modifier,
    hex: String,
    onHexChanged: (String) -> Unit,
    onColorConfirmed: (Long) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val parsedColor = parseHexColor(hex)
    val isValid = parsedColor != null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = hex,
            onValueChange = { input ->
                // Allow only valid hex characters and limit length
                val filtered = input
                    .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '#' }
                    .take(9) // #AARRGGBB max
                onHexChanged(filtered)

                // Auto-apply when valid
                parseHexColor(filtered)?.let { onColorConfirmed(it) }
            },
            label = { Text("Hex Color") },
            placeholder = { Text("#FF0000") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = hex.isNotEmpty() && !isValid,
            supportingText = if (hex.isNotEmpty() && !isValid) {
                { Text("Use format #RGB, #RRGGBB, or #AARRGGBB") }
            } else null,
            leadingIcon = if (parsedColor != null) {
                {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(parsedColor.toInt()), CircleShape)
                            .then(
                                if (Color(parsedColor.toInt()).luminance() > 0.85f) {
                                    Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape,
                                    )
                                } else Modifier
                            ),
                    )
                }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (parsedColor != null) {
                        onColorConfirmed(parsedColor)
                    }
                    focusManager.clearFocus()
                },
            ),
        )
    }
}

// endregion

// region Utility Functions

/**
 * Converts a [ColorData] to a different [ColorMode], preserving colors where possible.
 */
private fun convertColorConfig(config: ColorData, targetMode: ColorMode): ColorData {
    val colors = when (config) {
        is ColorData.Solid -> listOf(config.color, config.color)
        is ColorData.LinearGradient -> config.colors
        is ColorData.RadialGradient -> config.colors
    }
    val firstColor = colors.getOrElse(0) { 0xFF000000L }
    val secondColor = colors.getOrElse(1) { 0xFFFFFFFF }

    return when (targetMode) {
        ColorMode.Solid -> ColorData.Solid(firstColor)
        ColorMode.Linear -> ColorData.LinearGradient(
            colors = listOf(firstColor, secondColor),
            angleDegrees = (config as? ColorData.LinearGradient)?.angleDegrees ?: 0f,
        )
        ColorMode.Radial -> ColorData.RadialGradient(
            colors = listOf(firstColor, secondColor),
            centerX = (config as? ColorData.RadialGradient)?.centerX ?: 0.5f,
            centerY = (config as? ColorData.RadialGradient)?.centerY ?: 0.5f,
        )
    }
}

/**
 * Extracts the first solid color from any [ColorData] as a Long ARGB value.
 */
private fun ColorData.asSolidOrFirst(): Long = when (this) {
    is ColorData.Solid -> color
    is ColorData.LinearGradient -> colors.firstOrNull() ?: 0xFF000000L
    is ColorData.RadialGradient -> colors.firstOrNull() ?: 0xFF000000L
}

/**
 * Parses a hex color string into a Long ARGB value.
 * Supports formats: #RGB, #RRGGBB, #AARRGGBB (with or without #).
 */
private fun parseHexColor(hex: String): Long? {
    val cleaned = hex.removePrefix("#").trim()
    return when (cleaned.length) {
        3 -> {
            // #RGB -> #FFRRGGBB
            val r = cleaned[0]
            val g = cleaned[1]
            val b = cleaned[2]
            "FF$r$r$g$g$b$b".toLongOrNull(16)
        }
        6 -> {
            // #RRGGBB -> #FFRRGGBB
            "FF$cleaned".toLongOrNull(16)
        }
        8 -> {
            // #AARRGGBB
            cleaned.toLongOrNull(16)
        }
        else -> null
    }
}

/**
 * Converts a Long ARGB color to a hex string in #RRGGBB or #AARRGGBB format.
 */
private fun Long.toHexString(): String {
    val alpha = (this shr 24) and 0xFF
    val hex = this.and(0xFFFFFFFFL).toString(16).padStart(8, '0').uppercase()
    return if (alpha == 0xFFL) "#${hex.substring(2)}" else "#$hex"
}

// endregion

// region Previews

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorPickerSolidPreview() {
    QRQanvasTheme {
        Surface {
            ColorPicker(
                modifier = Modifier.padding(16.dp),
                selectedColor = ColorData.Solid(0xFFF44336),
                onColorSelected = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerLinearGradientPreview() {
    QRQanvasTheme {
        Surface {
            ColorPicker(
                modifier = Modifier.padding(16.dp),
                selectedColor = ColorData.LinearGradient(
                    colors = listOf(0xFF00E5FF, 0xFFFF00FF),
                    angleDegrees = 135f,
                ),
                onColorSelected = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerRadialGradientPreview() {
    QRQanvasTheme {
        Surface {
            ColorPicker(
                modifier = Modifier.padding(16.dp),
                selectedColor = ColorData.RadialGradient(
                    colors = listOf(0xFFFF6B35, 0xFFD32F2F),
                ),
                onColorSelected = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerCustomPreview() {
    QRQanvasTheme {
        Surface {
            ColorPicker(
                modifier = Modifier.padding(16.dp),
                selectedColor = ColorData.Solid(0xFF123456),
                onColorSelected = {},
            )
        }
    }
}

// endregion
