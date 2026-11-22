package com.axondragonscale.commons.devtools.builtin

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

/** Renders: Kill App only. */
@Composable
actual fun DevToolsBuiltInContent() = KillAppOnlyContent { exitProcess(0) }
