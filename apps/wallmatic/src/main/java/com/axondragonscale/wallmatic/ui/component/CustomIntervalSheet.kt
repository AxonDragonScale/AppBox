package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun CustomIntervalSheet(
    modifier: Modifier = Modifier,
    initial: Duration,
    onConfirm: (Duration) -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticBottomSheet(
        height = 430.dp,
        onDismiss = onDismiss,
    ) {
        CustomIntervalContent(
            modifier = modifier,
            initial = initial,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CustomIntervalContent(
    modifier: Modifier = Modifier,
    initial: Duration,
    onConfirm: (Duration) -> Unit,
    onDismiss: () -> Unit,
) {
    var hours by rememberSaveable { mutableIntStateOf(initial.inWholeHours.toInt()) }
    var minutes by rememberSaveable { mutableIntStateOf((initial.inWholeMinutes % 60).toInt()) }

    val maxHours = Interval.maximum.inWholeHours.toInt()

    fun setHours(value: Int) {
        hours = value.coerceIn(0, maxHours)
        if (hours == maxHours) minutes = 0
    }

    fun setMinutes(value: Int) {
        minutes = if (hours == maxHours) 0 else value.coerceIn(0, 55)
    }

    val total = hours.hours + minutes.minutes

    Column(modifier = modifier) {
        SheetDragHandle()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Custom interval",
            style = WallmaticTextStyles.dialogTitle,
            color = MaterialTheme.wallmatic.ink,
        )

        Spacer(modifier = Modifier.height(16.dp))

        StepperRow(
            label = "HOURS",
            value = hours,
            onDecrease = { setHours(hours - 1) },
            onIncrease = { setHours(hours + 1) },
        )
        Spacer(modifier = Modifier.height(16.dp))

        StepperRow(
            label = "MINUTES",
            value = minutes,
            onDecrease = { setMinutes(minutes - 5) },
            onIncrease = { setMinutes(minutes + 5) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "EVERY",
                style = WallmaticTextStyles.micro,
                color = MaterialTheme.wallmatic.inkMuted,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = Interval.label(total),
                style = WallmaticTextStyles.titleSmall.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.wallmatic.ink,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextAction(
                text = "Cancel",
                onClick = onDismiss,
                color = MaterialTheme.wallmatic.inkMuted,
                emphasized = false,
            )
            TextAction(
                text = "Set",
                onClick = { onConfirm(Interval.coerce(total)) },
                enabled = total in Interval.minimum..Interval.maximum,
            )
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WallmaticTextStyles.microStrong,
            color = MaterialTheme.wallmatic.inkMuted,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.width(160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(
                icon = Icons.Filled.Remove,
                contentDescription = "Decrease $label",
                onClick = onDecrease,
            )

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.toString(),
                    style = WallmaticTextStyles.number,
                    color = MaterialTheme.wallmatic.ink,
                )
            }

            StepperButton(
                icon = Icons.Filled.Add,
                contentDescription = "Increase $label",
                onClick = onIncrease,
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, MaterialTheme.wallmatic.ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.wallmatic.ink,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 430)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 430)
@Composable
private fun CustomIntervalSheetPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .height(430.dp)
                .background(MaterialTheme.wallmatic.paper)
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        ) {
            CustomIntervalContent(
                initial = 1.hours + 30.minutes,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
