package com.axondragonscale.limitless.ui.editor

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.Dropdown
import com.axondragonscale.commons.compose.DropdownItem
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

/**
 * Created by Ronak Harkhani on 03/11/24
 */

@Composable
internal fun GoalsSection(
    modifier: Modifier = Modifier,
    goals: List<GoalEntity>,
    onAddGoal: () -> Unit,
    onUpdateGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (Long) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = if (goals.isEmpty()) 8.dp else 0.dp)
        .animateContentSize(),
) {
    val allTypesUsed = goals.size >= GoalType.entries.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp).size(20.dp),
            imageVector = Icons.Default.RadioButtonChecked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Goals",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = onAddGoal,
            enabled = !allTypesUsed,
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add Goal",
                tint = if (allTypesUsed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                       else LocalContentColor.current,
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (goals.isEmpty()) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "No goals set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    goals.forEach { goal ->
        GoalCard(
            modifier = Modifier.padding(bottom = 8.dp),
            goal = goal,
            usedTypes = goals.map { it.type }.toSet(),
            onUpdate = onUpdateGoal,
            onDelete = { onDeleteGoal(goal.id) },
        )
    }
}

@Composable
private fun GoalCard(
    modifier: Modifier = Modifier,
    goal: GoalEntity,
    usedTypes: Set<GoalType>,
    onUpdate: (GoalEntity) -> Unit,
    onDelete: () -> Unit,
) {
    // Available types = all types minus those already used by other goals (current type always selectable)
    val typeItems = remember(usedTypes, goal.type) {
        GoalType.entries
            .filter { it == goal.type || it !in usedTypes }
            .map { DropdownItem(name = it.displayName, value = it) }
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Dropdown(
                    selectedItem = typeItems.first { it.value == goal.type },
                    items = typeItems,
                    modifier = Modifier.size(width = 128.dp, height = 36.dp),
                    onItemSelected = { onUpdate(goal.copy(type = it.value)) },
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            TargetCounter(
                modifier = Modifier.fillMaxWidth(),
                value = goal.target,
                onValueChange = { onUpdate(goal.copy(target = it)) },
            )
        }
    }
}

@Composable
private fun TargetCounter(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
                onClick = {
                    val newVal = value - 1
                    if (newVal >= 1) onValueChange(newVal)
                },
                enabled = value > 1,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease target",
                )
            }

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val parsed = textValue.toIntOrNull()
                            textValue = if (parsed != null && parsed >= 1) parsed.toString()
                            else value.toString()
                        }
                    },
                value = textValue,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }
                    textValue = filtered
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null && parsed >= 1) onValueChange(parsed)
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            IconButton(
                modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
                onClick = { onValueChange(value + 1) },
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase target",
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Light Mode — Empty", showBackground = true)
@Preview(name = "Dark Mode — Empty", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsSectionEmptyPreview() {
    LimitlessTheme {
        Surface {
            GoalsSection(
                goals = emptyList(),
                onAddGoal = {},
                onUpdateGoal = {},
                onDeleteGoal = {},
            )
        }
    }
}

@Preview(name = "Light Mode — Single Goal", showBackground = true)
@Preview(name = "Dark Mode — Single Goal", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsSectionSinglePreview() {
    LimitlessTheme {
        Surface {
            GoalsSection(
                goals = listOf(
                    GoalEntity(id = 1, habitId = 1, type = GoalType.DAILY, target = 3),
                ),
                onAddGoal = {},
                onUpdateGoal = {},
                onDeleteGoal = {},
            )
        }
    }
}

@Preview(name = "Light Mode — All Goals", showBackground = true)
@Preview(name = "Dark Mode — All Goals", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsSectionAllPreview() {
    LimitlessTheme {
        Surface {
            GoalsSection(
                goals = listOf(
                    GoalEntity(id = 1, habitId = 1, type = GoalType.DAILY, target = 1),
                    GoalEntity(id = 2, habitId = 1, type = GoalType.WEEKLY, target = 5),
                    GoalEntity(id = 3, habitId = 1, type = GoalType.MONTHLY, target = 20),
                ),
                onAddGoal = {},
                onUpdateGoal = {},
                onDeleteGoal = {},
            )
        }
    }
}

// endregion
