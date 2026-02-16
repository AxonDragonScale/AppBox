package com.axondragonscale.topty.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.commons.UIMode
import com.axondragonscale.topty.ui.theme.DefaultShape
import com.axondragonscale.topty.ui.theme.ToptyTheme

/**
 * Created by Ronak Harkhani on 30/10/24
 */

data class DropdownItem<T>(
    val name: String,
    val value: T,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
)

@Composable
fun <T> Dropdown(
    modifier: Modifier = Modifier.size(width = 128.dp, height = 32.dp),
    selectedItem: DropdownItem<T>,
    items: List<DropdownItem<T>>,
    onItemSelected: (DropdownItem<T>) -> Unit,
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 8.dp),
) = Box(modifier = modifier) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .matchParentSize()
            .onGloballyPositioned { coordinates -> dropdownWidth = coordinates.size.width }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = DefaultShape,
            )
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
        shape = DefaultShape,
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
                }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ToptyTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically),
        ) {
            var selectedUiFont by remember { mutableStateOf(UIFont.DEFAULT) }
            Dropdown(
                selectedItem = DropdownItem(selectedUiFont.name, selectedUiFont),
                items = UIFont.entries.map { DropdownItem(name = it.name, value = it) },
                onItemSelected = { selectedUiFont = it.value },
            )

            var selectedCodeDesign by remember { mutableStateOf(CodeDesign.DEFAULT) }
            Dropdown(
                selectedItem = DropdownItem(selectedCodeDesign.name, selectedCodeDesign),
                items = CodeDesign.entries.map { DropdownItem(name = it.name, value = it) },
                onItemSelected = { selectedCodeDesign = it.value },
            )

            var selectedUiMode by remember { mutableStateOf(UIMode.DEFAULT) }
            Dropdown(
                selectedItem = DropdownItem(selectedUiMode.displayName, selectedUiMode),
                items = UIMode.entries.map {
                    DropdownItem(name = it.displayName, value = it, leadingIcon = it.icon)
                },
                onItemSelected = { selectedUiMode = it.value },
            )

            var selectedBackupFormat by remember { mutableStateOf(BackupFormat.TOPTY) }
            Dropdown(
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 24.dp),
                selectedItem = DropdownItem(selectedBackupFormat.displayName, selectedBackupFormat),
                items = BackupFormat.entries.map {
                    DropdownItem(name = it.displayName, value = it)
                },
                onItemSelected = { selectedBackupFormat = it.value },
            )
        }
    }
}
