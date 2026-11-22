package com.axondragonscale.commons.devtools.host

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.axondragonscale.commons.devtools.logs.data.DevLogStore
import com.axondragonscale.commons.devtools.logs.ui.StoredLogsScreen
import com.axondragonscale.commons.devtools.ui.DevToolsSurface
import com.axondragonscale.logviewer.logcat.LogcatScreen
import com.axondragonscale.storageviewer.StorageViewer

internal actual val devToolsHosts: List<DevToolsHost> = listOf(
    DevToolsHost(
        id = "logs",
        title = "Open Logs",
        subtitle = "Read the logs kept on this device",
        icon = Icons.Rounded.History,
        available = { DevLogStore.isInstalled },
        content = { onBack ->
            val context = LocalContext.current
            StoredLogsScreen(
                onBack = onBack,
                onShare = { shareText(context, it) },
                onCopy = { copyText(context, it) },
            )
        },
    ),
    DevToolsHost(
        id = "logcat",
        title = "Open Logcat",
        subtitle = "Read this app's log records",
        icon = Icons.Rounded.Terminal,
        available = { true },
        content = { onBack ->
            val context = LocalContext.current
            LogcatScreen(
                onBack = onBack,
                onShare = { shareText(context, it) },
                onCopy = { copyText(context, it) },
            )
        },
    ),
    DevToolsHost(
        id = "storage",
        title = "Open Storage",
        subtitle = "Inspect databases, preferences and files",
        icon = Icons.Rounded.Storage,
        available = { true },
        content = { onBack -> StorageHost(onBack) },
    ),
)

/**
 * Android has a window per tool: it owns back, input blocking and its own window insets, and the
 * screen's back arrow closes it through [onClose].
 */
@Composable
internal actual fun DevToolsHostWindow(host: DevToolsHost?, onClose: () -> Unit) {
    if (host == null) return
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        DevToolsSurface {
            host.content(onClose)
        }
    }
}

@Composable
private fun StorageHost(onBack: () -> Unit) {
    // The dialog is not a nav destination, so it needs its own store to clear the viewer on close;
    // routing that store's state through the host registry keeps the walk across a recreation.
    val registryOwner = LocalSavedStateRegistryOwner.current
    val owner = remember(registryOwner) { StorageDialogViewModelStoreOwner(registryOwner) }
    DisposableEffect(owner) {
        onDispose {
            owner.viewModelStore.clear()
            owner.forget()
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        StorageViewer(onBack = onBack)
    }
}

private fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share logs"))
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Logs", text))
}

/** A dialog-owned store with its own registry, saved into [host] so the viewer survives recreation. */
private class StorageDialogViewModelStoreOwner(
    private val host: SavedStateRegistryOwner,
) : ViewModelStoreOwner, SavedStateRegistryOwner, HasDefaultViewModelProviderFactory {

    override val viewModelStore = ViewModelStore()

    /** The dialog lives exactly as long as the host, so it borrows its lifecycle. */
    override val lifecycle: Lifecycle get() = host.lifecycle

    private val controller = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras().apply {
            set(SAVED_STATE_REGISTRY_OWNER_KEY, this@StorageDialogViewModelStoreOwner)
            set(VIEW_MODEL_STORE_OWNER_KEY, this@StorageDialogViewModelStoreOwner)
        }

    /** The viewer always passes its own factory; this only satisfies the interface. */
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory = viewModelFactory { }

    init {
        controller.performAttach()
        enableSavedStateHandles()
        controller.performRestore(host.savedStateRegistry.consumeRestoredStateForKey(KEY))
        host.savedStateRegistry.registerSavedStateProvider(KEY) {
            Bundle().apply(controller::performSave)
        }
    }

    /** Drops the saved walk too, so a reopened dialog starts at the store list. */
    fun forget() {
        host.savedStateRegistry.unregisterSavedStateProvider(KEY)
    }

    private companion object {
        const val KEY = "storageviewer:dialog"
    }
}
