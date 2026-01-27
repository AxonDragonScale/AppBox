package com.axondragonscale.jest.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.JestTheme

@Composable
fun JestIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 56.dp,
) {
    // Use Button, IconButton ripple doesn't scale with size
    Button(
        modifier = modifier.size(size),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JestIconPreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            JestIconButton(
                icon = Icons.Default.Tune,
                onClick = { /*TODO*/ }
            )
        }
    }
}
