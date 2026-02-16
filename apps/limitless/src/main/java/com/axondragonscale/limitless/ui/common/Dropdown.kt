package com.axondragonscale.limitless.ui.common

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.axondragonscale.limitless.model.UIMode
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

/**
 * Created by Ronak Harkhani on 04/11/24
 */

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
    size: DpSize = DpSize(width = 128.dp, 32.dp),
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 8.dp),
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 2.dp,
) = Box(modifier = modifier) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .size(size)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.outline,
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
        modifier = Modifier.width(size.width),
        expanded = dropdownMenuExpanded,
        onDismissRequest = { dropdownMenuExpanded = false },
        shape = shape,
    ) {
        items.forEach { item ->
            DropdownMenuItem(
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
    LimitlessTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically),
        ) {
            var selectedUiMode by remember { mutableStateOf(UIMode.DEFAULT) }
            Dropdown(
                selectedItem = DropdownItem(selectedUiMode.displayName, selectedUiMode),
                items = UIMode.entries.map {
                    DropdownItem(name = it.displayName, value = it, leadingIcon = it.icon)
                },
                onItemSelected = { selectedUiMode = it.value },
            )
        }
    }
}
