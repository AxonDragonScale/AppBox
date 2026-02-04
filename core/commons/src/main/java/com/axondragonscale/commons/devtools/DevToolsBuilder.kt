package com.axondragonscale.commons.devtools

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Builder class for creating a list of DevTools using a DSL-style API.
 *
 * Usage:
 * ```
 * val tools = DevToolsBuilder()
 *     .section("Database")
 *     .action("Clear Cache") { clearCache() }
 *     .action("Reset DB", destructive = true) { resetDb() }
 *     .section("Settings")
 *     .toggle("Debug Mode", isDebug) { setDebug(it) }
 *     .info("Version", "1.0.0")
 *     .build()
 * ```
 */
class DevToolsBuilder {

    private val tools = mutableListOf<DevTool>()

    /**
     * Adds a section header to group related tools.
     */
    fun section(
        title: String,
        icon: ImageVector? = null,
    ) = apply {
        tools.add(DevTool.Section(title, icon))
    }

    /**
     * Adds a clickable action tool.
     */
    fun action(
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        iconTint: Color? = null,
        destructive: Boolean = false,
        onClick: () -> Unit,
    ) = apply {
        tools.add(
            DevTool.Action(
                title = title,
                subtitle = subtitle,
                icon = icon,
                iconTint = iconTint,
                destructive = destructive,
                onClick = onClick,
            )
        )
    }

    /**
     * Adds a toggle/switch tool.
     */
    fun toggle(
        title: String,
        checked: Boolean,
        subtitle: String? = null,
        icon: ImageVector? = null,
        onCheckedChange: (Boolean) -> Unit,
    ) = apply {
        tools.add(
            DevTool.Toggle(
                title = title,
                subtitle = subtitle,
                icon = icon,
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        )
    }

    /**
     * Adds a read-only information display.
     */
    fun info(
        title: String,
        value: String,
        icon: ImageVector? = null,
    ) = apply {
        tools.add(
            DevTool.Info(
                title = title,
                value = value,
                icon = icon,
            )
        )
    }

    /**
     * Adds a custom composable tool for fully custom UI.
     */
    fun custom(
        content: @Composable () -> Unit,
    ) = apply {
        tools.add(DevTool.Custom(content))
    }

    /**
     * Adds a pre-built DevTool directly.
     */
    fun add(tool: DevTool) = apply {
        tools.add(tool)
    }

    /**
     * Adds multiple pre-built DevTools directly.
     */
    fun addAll(tools: List<DevTool>) = apply {
        this.tools.addAll(tools)
    }

    /**
     * Builds and returns the list of DevTools.
     */
    fun build(): List<DevTool> = tools.toList()
}

/**
 * DSL function for creating a list of DevTools.
 *
 * Usage:
 * ```
 * val tools = buildDevTools {
 *     section("Database")
 *     action("Clear Cache") { clearCache() }
 *     toggle("Debug Mode", isDebug) { setDebug(it) }
 * }
 * ```
 */
inline fun buildDevTools(
    builder: DevToolsBuilder.() -> Unit
): List<DevTool> = DevToolsBuilder().apply(builder).build()
