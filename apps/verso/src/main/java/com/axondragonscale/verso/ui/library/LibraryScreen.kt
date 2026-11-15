package com.axondragonscale.verso.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.verso.ui.component.ActionButton
import com.axondragonscale.verso.ui.component.ActionStyle
import com.axondragonscale.verso.ui.theme.Dimens
import com.axondragonscale.verso.ui.theme.Spacing
import com.axondragonscale.verso.ui.theme.VersoTextStyles
import com.axondragonscale.verso.ui.theme.verso

/**
 * The empty library: the app's own onboarding. No carousel, no sample book, no tour.
 */
@Composable
fun LibraryScreen(
    onImportBooks: () -> Unit,
    onImportFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.verso

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paper)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenPadding),
    ) {
        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = "Verso",
            style = VersoTextStyles.display,
            color = colors.ink,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "EPUB, FB2, TXT, Markdown, HTML.",
            style = VersoTextStyles.ui,
            color = colors.inkMuted,
        )

        Spacer(Modifier.height(Spacing.xxxl))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ActionButton(
                label = "Import books",
                onClick = onImportBooks,
                modifier = Modifier.fillMaxWidth(),
            )
            ActionButton(
                label = "Import a folder",
                onClick = onImportFolder,
                modifier = Modifier.fillMaxWidth(),
                style = ActionStyle.Outline,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "Books stay on this device.",
            style = VersoTextStyles.micro,
            color = colors.inkMuted,
        )

        Spacer(Modifier.height(Spacing.xxl))
    }
}
