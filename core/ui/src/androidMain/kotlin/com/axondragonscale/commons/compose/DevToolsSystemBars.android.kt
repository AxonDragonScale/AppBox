package com.axondragonscale.commons.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
public actual fun DevToolsSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val controller = view.systemBarWindow()?.insetsController
        val appearance = controller?.systemBarsAppearance
        val lightFlags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // Already-light bars need no change, which also keeps nested tool screens from stacking.
        val changed = appearance != null && appearance and lightFlags != 0
        if (changed) controller?.setSystemBarsAppearance(0, lightFlags)
        onDispose {
            if (changed && appearance != null) controller?.setSystemBarsAppearance(appearance and lightFlags, lightFlags)
        }
    }
}

/** A dialog's window, or the activity's, whichever owns [this] view; null before attachment. */
private fun View.systemBarWindow(): Window? {
    dialogWindow()?.let { return it }
    val activity = context.findActivity() ?: return null
    return activity.window
}

private fun View.dialogWindow(): Window? {
    var owner: View? = this
    while (owner != null) {
        (owner.parent as? DialogWindowProvider)?.let { return it.window }
        owner = owner.parent as? View
    }
    return null
}

private fun Context.findActivity(): Activity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
