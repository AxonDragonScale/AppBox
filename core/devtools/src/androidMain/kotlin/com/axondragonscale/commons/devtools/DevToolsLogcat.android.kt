package com.axondragonscale.commons.devtools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.axondragonscale.logcatviewer.LogcatViewer

actual val isLogcatAvailable: Boolean = true

/**
 * The viewer as a plain screen, for a host that wants it as a navigation destination instead of
 * the built-in dialog.
 */
@Composable
fun LogcatDevToolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    LogcatViewer(modifier = modifier, onBack = onBack)
}

/**
 * Android has a viewer, so the dialog is the whole presentation: it owns back, input blocking and
 * its own window insets, and the viewer's top bar arrow closes it through [onDismiss].
 */
@Composable
actual fun DevToolsLogcatHost(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
    // The dialog is not a nav destination, so it needs its own store to clear the viewer on close.
    val owner = remember { DialogViewModelStoreOwner() }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            Surface(modifier = Modifier.fillMaxSize()) {
                LogcatViewer(onBack = onDismiss)
            }
        }
    }
}

private class DialogViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
