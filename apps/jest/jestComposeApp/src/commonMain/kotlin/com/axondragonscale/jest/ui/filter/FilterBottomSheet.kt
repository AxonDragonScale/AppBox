package com.axondragonscale.jest.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.ui.theme.DarkSurface
import com.axondragonscale.jest.ui.theme.DarkSurfaceVariant
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Adaptive filter bottom sheet for mobile (Compact width).
 * Wraps [PrefsVM] and renders either the Category or Flags filter depending on [filterType].
 * Matches the "Bottom Sheet — Category" and "Bottom Sheet — Flags" designs in jest.pen.
 *
 * Layout (dark surface #1A1A1E, 24dp corner radius):
 *   1. Drag handle — 36×4dp rounded pill at top center
 *   2. Header row — title (Fraunces/titleLarge) + "Clear" text link (accent-colored, right-aligned)
 *   3. Subtitle — muted description text
 *   4. Vertical list of checkbox rows — each 52dp tall, 10dp corner, 16dp padding
 *        • Selected: label in onBackground, filled accent-color checkbox with check icon
 *        • Unselected: label in onSurfaceVariant, dark checkbox with border
 *   5. "Apply Filters" CTA button — full-width, 52dp, 14dp corner, accent fill
 */
@Composable
fun FilterBottomSheet(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    vm: PrefsVM = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    FilterBottomSheet(
        modifier = modifier,
        filterType = filterType,
        uiState = uiState,
        onEvent = { vm.onEvent(it) },
        onDismiss = onDismiss,
    )
}

@Composable
fun FilterBottomSheet(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    uiState: PrefsUiState,
    onEvent: (PrefsUiEvent) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceVariant,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = Color(0xFF3A3A40),
                        shape = RoundedCornerShape(2.dp),
                    )
            )
        }

        FilterSheetContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            filterType = filterType,
            uiState = uiState,
            onEvent = onEvent,
            onDismiss = onDismiss,
        )

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
internal fun FilterSheetContent(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    uiState: PrefsUiState,
    onEvent: (PrefsUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val accentColor = when (filterType) {
        FilterType.Category -> MaterialTheme.colorScheme.primary
        FilterType.Flags -> MaterialTheme.colorScheme.secondary
    }
    val onAccentColor = when (filterType) {
        FilterType.Category -> MaterialTheme.colorScheme.onPrimary
        FilterType.Flags -> MaterialTheme.colorScheme.onSecondary
    }
    val title = when (filterType) {
        FilterType.Category -> "Category"
        FilterType.Flags -> "Exclude Flags"
    }
    val subtitle = when (filterType) {
        FilterType.Category -> "Show jokes from these categories"
        FilterType.Flags -> "Hide jokes containing these flags"
    }
    val clearEvent: PrefsUiEvent = when (filterType) {
        FilterType.Category -> PrefsUiEvent.CategoryCleared
        FilterType.Flags -> PrefsUiEvent.FlagsCleared
    }

    Column(modifier = modifier) {
        // Header row: title + Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                modifier = Modifier.clickable { onEvent(clearEvent) },
                text = "Clear",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = accentColor,
            )
        }

        // Subtitle
        Text(
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Item list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            when (filterType) {
                FilterType.Category -> {
                    Category.entries.forEach { category ->
                        val isSelected = uiState.selectedCategories.contains(category)
                        FilterCheckboxRow(
                            label = category.name,
                            isSelected = isSelected,
                            accentColor = accentColor,
                            onAccentColor = onAccentColor,
                            onToggle = {
                                val updated = if (isSelected)
                                    uiState.selectedCategories - category
                                else
                                    uiState.selectedCategories + category
                                onEvent(PrefsUiEvent.CategorySelectionChanged(updated.map { it.ordinal }))
                            },
                        )
                    }
                }

                FilterType.Flags -> {
                    BlacklistFlag.entries.forEach { flag ->
                        val isSelected = uiState.selectedBlacklistFlags.contains(flag)
                        FilterCheckboxRow(
                            label = flag.name,
                            isSelected = isSelected,
                            accentColor = accentColor,
                            onAccentColor = onAccentColor,
                            onToggle = {
                                val updated = if (isSelected)
                                    uiState.selectedBlacklistFlags - flag
                                else
                                    uiState.selectedBlacklistFlags + flag
                                onEvent(PrefsUiEvent.BlacklistFlagSelectionChanged(updated.map { it.ordinal }))
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Apply CTA
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor,
            ),
        ) {
            Text(
                text = "Apply Filters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun FilterCheckboxRow(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onAccentColor: Color,
    onToggle: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                color = if (isSelected) DarkSurface else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSelected)
                MaterialTheme.colorScheme.onBackground
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(color = accentColor, shape = RoundedCornerShape(6.dp))
                    } else {
                        Modifier
                            .background(color = DarkSurface, shape = RoundedCornerShape(6.dp))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF3A3A40),
                                shape = RoundedCornerShape(6.dp),
                            )
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = onAccentColor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FilterBottomSheetCategoryPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FilterBottomSheet(
                filterType = FilterType.Category,
                uiState = PrefsUiState(
                    selectedCategories = listOf(Category.Programming),
                ),
                onEvent = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun FilterBottomSheetFlagsPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FilterBottomSheet(
                filterType = FilterType.Flags,
                uiState = PrefsUiState(
                    selectedBlacklistFlags = listOf(BlacklistFlag.NSFW),
                ),
                onEvent = {},
                onDismiss = {},
            )
        }
    }
}
