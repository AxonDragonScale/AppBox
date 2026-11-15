package com.axondragonscale.commons.devtools

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.axondragonscale.storageviewer.StorageViewer

internal actual val isStorageViewerAvailable: Boolean = true

/**
 * The viewer as a plain screen, for a host that wants it as a navigation destination instead of
 * the built-in dialog.
 */
@Composable
internal fun StorageDevToolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    DevToolsTheme {
        DevToolsSystemBars()
        StorageViewer(modifier = modifier, onBack = onBack)
    }
}

/**
 * Android has a viewer, so the dialog is the whole presentation: it owns back, input blocking and
 * its own window insets, and the viewer's top bar arrow closes it through [onDismiss].
 */
@Composable
internal actual fun DevToolsStorageHost(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            DevToolsTheme {
                DevToolsSystemBars()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    StorageViewer(onBack = onDismiss)
                }
            }
        }
    }
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
