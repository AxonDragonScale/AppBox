package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

enum class RuledIndicator { Top, Bottom }

/**
 * The ruled segment language: equal cells, 1 px dividers, and a full-width 3 px ink rule on the
 * active cell. Callers supply the label and the outer geometry.
 */
@Composable
fun <T> RuledTabs(
    modifier: Modifier = Modifier,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    height: Dp,
    indicator: RuledIndicator,
    border: Boolean,
    dividerColor: Color = MaterialTheme.wallmatic.ink,
    role: Role = Role.Tab,
    width: Dp? = null,
    content: @Composable (item: T, selected: Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .then(if (border) Modifier.border(1.dp, MaterialTheme.wallmatic.ink) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(dividerColor)
                )
            }

            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(
                        selected = isSelected,
                        role = role,
                        onClick = { onSelect(item) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                content(item, isSelected)

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(
                                when (indicator) {
                                    RuledIndicator.Top -> Alignment.TopCenter
                                    RuledIndicator.Bottom -> Alignment.BottomCenter
                                }
                            )
                            .fillMaxWidth()
                            .height(Dimens.activeRule)
                            .background(MaterialTheme.wallmatic.ink)
                    )
                }
            }
        }
    }
}
