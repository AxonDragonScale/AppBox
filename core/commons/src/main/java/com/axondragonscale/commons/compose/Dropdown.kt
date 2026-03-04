package com.axondragonscale.commons.compose

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.UIMode

data class DropdownItem<T>(
    val name: String,
    val value: T,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
)

@Composable
fun <T> Dropdown(
    modifier: Modifier = Modifier,
    selectedItem: DropdownItem<T>,
    items: List<DropdownItem<T>>,
    onItemSelected: (DropdownItem<T>) -> Unit,
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 8.dp),
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
) = Box(modifier = modifier) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .matchParentSize()
            .onGloballyPositioned { dropdownWidth = it.size.width }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape,
            )
            .clip(shape)
            .clickable { dropdownMenuExpanded = true }
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = selectedItem.name,
            textAlign = TextAlign.Center,
        )

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
        )
    }

    DropdownMenu(
        modifier = Modifier.width(with(density) { dropdownWidth.toDp() }),
        expanded = dropdownMenuExpanded,
        onDismissRequest = { dropdownMenuExpanded = false },
        shape = shape,
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = item.leadingIcon?.let {
                    { Icon(imageVector = it, contentDescription = null) }
                },
                text = { Text(text = item.name) },
                trailingIcon = item.trailingIcon?.let {
                    { Icon(imageVector = it, contentDescription = null) }
                },
                onClick = {
                    onItemSelected(item)
                    dropdownMenuExpanded = false
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DropdownPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // topty/limitless Settings — UIMode picker (128×32, with leading icons)
                var selectedUiMode by remember { mutableStateOf(UIMode.DEFAULT) }
                Dropdown(
                    modifier = Modifier.size(width = 128.dp, height = 32.dp),
                    selectedItem = DropdownItem(selectedUiMode.displayName, selectedUiMode, leadingIcon = selectedUiMode.icon),
                    items = UIMode.entries.map { DropdownItem(name = it.displayName, value = it, leadingIcon = it.icon) },
                    onItemSelected = { selectedUiMode = it.value },
                )

                // topty Settings — Font / CodeDesign pickers (128×32, small corner radius, no icons)
                val fontItems = listOf("Default", "Classic", "Minimal").map { DropdownItem(it, it) }
                var selectedFont by remember { mutableStateOf(fontItems.first()) }
                Dropdown(
                    modifier = Modifier.size(width = 128.dp, height = 32.dp),
                    selectedItem = selectedFont,
                    items = fontItems,
                    onItemSelected = { selectedFont = it },
                    shape = RoundedCornerShape(4.dp),
                )

                // limitless Editor — GoalCard / ReminderCard (128×36, 2dp border)
                val goalItems = listOf("Steps", "Distance", "Calories").map { DropdownItem(it, it) }
                var selectedGoal by remember { mutableStateOf(goalItems.first()) }
                Dropdown(
                    modifier = Modifier.size(width = 128.dp, height = 36.dp),
                    selectedItem = selectedGoal,
                    items = goalItems,
                    onItemSelected = { selectedGoal = it },
                    borderWidth = 2.dp,
                )

                // limitless Editor — DayOfMonthSelector (160×32, 2dp border)
                val dayItems = (1..31).map { DropdownItem(it.toString(), it) }
                var selectedDay by remember { mutableStateOf(dayItems.first()) }
                Dropdown(
                    modifier = Modifier.size(width = 160.dp, height = 32.dp),
                    selectedItem = selectedDay,
                    items = dayItems,
                    onItemSelected = { selectedDay = it },
                    borderWidth = 2.dp,
                )

                // topty BackupRestore — full-width, taller (fillMaxWidth × 48dp, small corner radius)
                val formatItems = listOf("Topty (.topty)", "Google Authenticator (.json)").map { DropdownItem(it, it) }
                var selectedFormat by remember { mutableStateOf(formatItems.first()) }
                Dropdown(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    selectedItem = selectedFormat,
                    items = formatItems,
                    onItemSelected = { selectedFormat = it },
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}
