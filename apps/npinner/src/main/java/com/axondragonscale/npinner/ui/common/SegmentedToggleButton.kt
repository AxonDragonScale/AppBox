package com.axondragonscale.npinner.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.axondragonscale.npinner.ui.theme.NPinnerTheme

/**
 * Created by Ronak Harkhani on 29/04/23
 */

private val BORDER_WIDTH = 2.dp

@Composable
fun SegmentedToggleButton(
    items: List<String>,
    onItemSelected: (selectedItemIndex: Int) -> Unit,
    defaultSelectedItemIndex: Int = 0,
    enabled: Boolean = true,
    cornerRadius: Dp = 2.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        var selectedItemIndex by remember { mutableStateOf(defaultSelectedItemIndex) }
        items.forEachIndexed { index, item ->
            OutlinedButton(
                // Manipulate offset to merge the borders of adjacent buttons
                modifier = Modifier
                    .offset(if (index == 0) 0.dp else BORDER_WIDTH * -index, 0.dp)
                    .weight(1F)
                    .zIndex(if (selectedItemIndex == index) 1F else 0F),
                onClick = {
                    selectedItemIndex = index
                    onItemSelected(selectedItemIndex)
                },
                enabled = enabled,
                shape = when (index) {
                    // LeftMost Button has rounded corners on the left side
                    0 -> RoundedCornerShape(
                        topStart = cornerRadius,
                        bottomStart = cornerRadius
                    )
                    // RightMost Button has rounded corners on the right side
                    items.lastIndex -> RoundedCornerShape(
                        topEnd = cornerRadius,
                        bottomEnd = cornerRadius
                    )
                    // Inner Buttons have not rounded corners
                    else -> RoundedCornerShape(0.dp)
                },
                border = BorderStroke(
                    width = BORDER_WIDTH,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2F)
                ),
                colors = if (selectedItemIndex == index) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SegmentedToggleButtonPreview() {
    NPinnerTheme {
        Surface {
            SegmentedToggleButton(
                items = listOf("DAY", "WEEK", "MONTH", "YEAR"),
                onItemSelected = {},
                enabled = true,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SegmentedToggleButtonDisabledPreview() {
    NPinnerTheme {
        Surface {
            SegmentedToggleButton(
                items = listOf("DAY", "WEEK", "MONTH", "YEAR"),
                onItemSelected = {},
                enabled = false,
            )
        }
    }
}
