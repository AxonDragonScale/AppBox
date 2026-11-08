package com.axondragonscale.devinator.ui.tweaks

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.devinator.model.Tweak
import com.axondragonscale.devinator.model.TweakGroup
import com.axondragonscale.devinator.model.TweakIcon
import com.axondragonscale.devinator.repository.TweakCatalog
import com.axondragonscale.devinator.ui.components.BackTopBar
import com.axondragonscale.devinator.ui.components.DevinatorCard
import com.axondragonscale.devinator.ui.components.DevinatorDivider
import com.axondragonscale.devinator.ui.components.DevinatorSnackbarHost
import com.axondragonscale.devinator.ui.components.DevinatorSwitch
import com.axondragonscale.devinator.ui.components.DurationChip
import com.axondragonscale.devinator.ui.components.IconBadge
import com.axondragonscale.devinator.ui.components.SmallDropdown
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import com.axondragonscale.devinator.ui.theme.SectionLabelTextStyle
import org.koin.androidx.compose.koinViewModel

/** Created by Ronak on 09/11/26 */

/** Design source: `12 · Dev options · A grouped cards` in `apps/devinator/devinator.fig`. */
@Composable
fun TweaksScreen(navController: NavController) {
    val viewModel: TweaksVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is TweaksUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    TweaksContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBack = { navController.popBackStack() },
    )
}

@Composable
private fun TweaksContent(
    uiState: TweaksUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TweaksUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { DevinatorSnackbarHost(snackbarHostState) },
        topBar = { BackTopBar(title = "Tweaks", onBack = onBack) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            TweakGroup.entries.forEachIndexed { index, group ->
                if (index > 0) Spacer(Modifier.height(24.dp))
                GroupLabel(group.label)
                Spacer(Modifier.height(8.dp))
                TweakCard(
                    group = group,
                    values = uiState.values,
                    onEvent = onEvent,
                )
            }
            Spacer(Modifier.height(20.dp))
            ResetRow(onReset = { onEvent(TweaksUiEvent.Reset) })
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Reset restores the stock value for every tweak.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A group heading: sentence case beside a hairline rule, so the rule carries the grouping. */
@Composable
private fun GroupLabel(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SectionLabelTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun TweakCard(
    group: TweakGroup,
    values: Map<String, Float>,
    onEvent: (TweaksUiEvent) -> Unit,
) {
    val rows = TweakCatalog.filter { it.group == group }
    DevinatorCard {
        rows.forEachIndexed { index, tweak ->
            if (index > 0) DevinatorDivider()
            TweakRow(
                tweak = tweak,
                value = values[tweak.key] ?: tweak.stock,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun TweakRow(
    tweak: Tweak,
    value: Float,
    onEvent: (TweaksUiEvent) -> Unit,
) {
    val on = value != 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(
                if (tweak is Tweak.Toggle) {
                    Modifier.toggleable(
                        value = on,
                        role = Role.Switch,
                        onValueChange = { onEvent(TweaksUiEvent.SetToggle(tweak, it)) },
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = tweak.icon.vector())
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = tweak.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = tweak.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when (tweak) {
            is Tweak.Toggle -> DevinatorSwitch(
                checked = on,
                onCheckedChange = null,
                modifier = Modifier.clearAndSetSemantics {},
            )

            is Tweak.Scale,
            is Tweak.Limit -> ChoiceControl(tweak = tweak, value = value, onEvent = onEvent)
        }
    }
}

/** The DurationChip Home uses for auto-off, reused as the value control for every choice row. */
@Composable
private fun ChoiceControl(
    tweak: Tweak,
    value: Float,
    onEvent: (TweaksUiEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // The chip is 36 dp; the tap target around it is the design's 48 dp.
        Box(
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.DropdownList) { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            DurationChip(
                label = tweak.label(value),
                off = tweak is Tweak.Scale && value <= 0f,
            )
        }
        SmallDropdown(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = tweak.choices,
            selected = value,
            label = { tweak.label(it) },
            onSelect = { onEvent(TweaksUiEvent.Select(tweak, it)) },
        )
    }
}

@Composable
private fun ResetRow(onReset: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(role = Role.Button, onClickLabel = "Reset to defaults", onClick = onReset),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reset to defaults",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** What the chip and the menu print: "Off" is red, every other value is a set value. */
private fun Tweak.label(value: Float): String = when (this) {
    is Tweak.Scale -> if (value <= 0f) "Off" else "${scale(value)}×"
    is Tweak.Limit -> if (value < 0f) "Standard" else value.toInt().toString()
    is Tweak.Toggle -> if (value != 0f) "On" else "Off"
}

/** 0.5 stays "0.5"; 1.0 becomes "1" so the chip does not read "1.0×". */
private fun scale(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()

private fun TweakIcon.vector(): ImageVector = when (this) {
    TweakIcon.ANIMATION -> Icons.Filled.Animation
    TweakIcon.LIFECYCLE -> Icons.Filled.RestartAlt
    TweakIcon.PROCESSES -> Icons.Filled.Speed
    TweakIcon.ANR -> Icons.Filled.Report
    TweakIcon.AWAKE -> Icons.Filled.Bedtime
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TweaksScreenPreview() {
    DevinatorTheme(dynamicColor = false) {
        TweaksContent(
            uiState = TweaksUiState(TweakCatalog.associate { it.key to it.stock }),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TweaksScreenChangedPreview() {
    val values = TweakCatalog.associate { it.key to it.stock } +
        (TweakCatalog[0].key to 0.5f) +
        (TweakCatalog[1].key to 0f) +
        (TweakCatalog[3].key to 1f) +
        (TweakCatalog[4].key to 1f)
    DevinatorTheme(dynamicColor = false) {
        TweaksContent(
            uiState = TweaksUiState(values),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onBack = {},
        )
    }
}
