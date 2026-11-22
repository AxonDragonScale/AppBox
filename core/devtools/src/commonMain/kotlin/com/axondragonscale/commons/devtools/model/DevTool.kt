package com.axondragonscale.commons.devtools.model

import androidx.compose.ui.graphics.vector.ImageVector

/** One entry in a developer tools list. */
sealed interface DevTool {

    /** A clickable action; [destructive] styles it as an error. */
    data class Action(
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val destructive: Boolean = false,
        val onClick: suspend () -> Unit,
    ) : DevTool

    data class Toggle(
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : DevTool

    data class Info(
        val title: String,
        val value: String,
    ) : DevTool

    data class Section(
        val title: String,
        val icon: ImageVector? = null,
    ) : DevTool
}
