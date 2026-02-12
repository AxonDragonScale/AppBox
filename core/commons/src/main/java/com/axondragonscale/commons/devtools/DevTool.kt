package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed interface representing different types of developer tools.
 * Each tool type has its own data class with relevant properties.
 */
sealed interface DevTool {

    /**
     * A clickable action tool that performs an operation when clicked.
     *
     * @param title The main title of the tool
     * @param subtitle Optional description or current state
     * @param icon Optional icon to display
     * @param iconTint Optional tint color for the icon
     * @param destructive Whether this action is destructive (will be styled in red)
     * @param onClick Callback when the tool is clicked
     */
    data class Action(
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val iconTint: Color? = null,
        val destructive: Boolean = false,
        val completionAlert: String? = null,
        val onClick: suspend () -> Unit,
    ) : DevTool

    /**
     * A toggle/switch tool for boolean settings.
     *
     * @param title The main title of the tool
     * @param subtitle Optional description
     * @param icon Optional icon to display
     * @param checked Current state of the toggle
     * @param onCheckedChange Callback when the toggle state changes
     */
    data class Toggle(
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : DevTool

    /**
     * A read-only information display tool.
     *
     * @param title The label for the information
     * @param value The value to display
     * @param icon Optional icon to display
     */
    data class Info(
        val title: String,
        val value: String,
        val icon: ImageVector? = null,
    ) : DevTool

    /**
     * A section header to group related tools.
     *
     * @param title The section title
     * @param icon Optional icon for the section
     */
    data class Section(
        val title: String,
        val icon: ImageVector? = null,
    ) : DevTool

    /**
     * A custom composable tool for fully custom UI.
     *
     * @param content The composable content to render
     */
    data class Custom(
        val content: @Composable () -> Unit,
    ) : DevTool
}
