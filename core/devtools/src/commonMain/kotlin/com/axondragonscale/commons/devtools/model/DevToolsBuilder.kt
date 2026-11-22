package com.axondragonscale.commons.devtools.model

import androidx.compose.ui.graphics.vector.ImageVector

/** The DSL behind a developer tools screen; each call appends one [DevTool]. */
class DevToolsBuilder {

    private val tools = mutableListOf<DevTool>()

    fun section(
        title: String,
        icon: ImageVector? = null,
    ) = apply {
        tools.add(DevTool.Section(title, icon))
    }

    fun action(
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        destructive: Boolean = false,
        onClick: suspend () -> Unit,
    ) = apply {
        tools.add(
            DevTool.Action(
                title = title,
                subtitle = subtitle,
                icon = icon,
                destructive = destructive,
                onClick = onClick,
            )
        )
    }

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

    fun info(
        title: String,
        value: String,
    ) = apply {
        tools.add(DevTool.Info(title = title, value = value))
    }

    fun build(): List<DevTool> = tools.toList()
}

inline fun buildDevTools(
    builder: DevToolsBuilder.() -> Unit
): List<DevTool> = DevToolsBuilder().apply(builder).build()
