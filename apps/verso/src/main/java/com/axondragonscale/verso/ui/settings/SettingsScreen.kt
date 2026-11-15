package com.axondragonscale.verso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.verso.ui.theme.Dimens
import com.axondragonscale.verso.ui.theme.Spacing
import com.axondragonscale.verso.ui.theme.VersoTextStyles
import com.axondragonscale.verso.ui.theme.verso

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.verso

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paper)
            .statusBarsPadding()
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(Modifier.height(Spacing.lg))
        Text(text = "Settings", style = VersoTextStyles.display, color = colors.ink)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Nothing to configure yet.",
            style = VersoTextStyles.ui,
            color = colors.inkMuted,
        )
    }
}
