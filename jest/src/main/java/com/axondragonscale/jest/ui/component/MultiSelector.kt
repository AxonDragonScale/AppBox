package com.axondragonscale.jest.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.ui.theme.JestTheme

data class MultiSelectorOption(
    val displayText: String,
    val id: Int,
    val isSelected: Boolean,
)

fun <T : Enum<T>> createMultiSelectorOptions(
    options: List<Enum<T>>,
    selectedOptions: List<Enum<T>>,
) = options.map { option ->
    MultiSelectorOption(
        displayText = option.name.uppercase(),
        id = option.ordinal,
        isSelected = selectedOptions.contains(option)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelector(
    modifier: Modifier = Modifier,
    label: String,
    options: List<MultiSelectorOption>,
    onSelectionChanged: (List<Int>) -> Unit,
) = Column(modifier = modifier) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val bgColor = MaterialTheme.colorScheme.run {
                if (option.isSelected) primary else primaryContainer
            }
            val fontColor = MaterialTheme.colorScheme.run {
                if (option.isSelected) onPrimary else onPrimaryContainer
            }
            Text(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        shape = RoundedCornerShape(40)
                    )
                    .background(
                        color = bgColor,
                        shape = RoundedCornerShape(40),
                    )
                    .defaultMinSize(minWidth = 64.dp, minHeight = 40.dp)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .wrapContentSize()
                    .clickable {
                        val updatedSelection = options
                            .filter {
                                if (it == option) !option.isSelected
                                else it.isSelected
                            }
                            .map { it.id }

                        onSelectionChanged(updatedSelection)
                    },
                text = option.displayText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = fontColor,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrefsBottomSheetPreview() {
    JestTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            MultiSelector(
                label = "CATEGORY",
                options = createMultiSelectorOptions(
                    Category.entries,
                    Category.entries.take(3)
                ),
                onSelectionChanged = {},
            )

            MultiSelector(
                modifier = Modifier.padding(top = 16.dp),
                label = "JOKE TYPE",
                options = createMultiSelectorOptions(
                    JokeType.entries,
                    JokeType.entries.take(1)
                ),
                onSelectionChanged = {},
            )

            MultiSelector(
                modifier = Modifier.padding(top = 16.dp),
                label = "BLACKLIST",
                options = createMultiSelectorOptions(
                    BlacklistFlag.entries,
                    BlacklistFlag.entries.take(3)
                ),
                onSelectionChanged = {},
            )
        }
    }
}
