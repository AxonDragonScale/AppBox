package com.axondragonscale.cardy.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.nfc.NfcReaderMode
import com.axondragonscale.cardy.ui.detail.DetailScreen
import com.axondragonscale.cardy.ui.home.HomeScreen
import com.axondragonscale.cardy.ui.home.HomeVM
import com.axondragonscale.cardy.ui.settings.SettingsScreen
import com.axondragonscale.cardy.ui.vault.VaultScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/** Created by Ronak on 05/30/26 */
@Composable
fun CardyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val activity = LocalActivity.current as FragmentActivity
    val homeVM: HomeVM = koinViewModel(viewModelStoreOwner = activity)
    val nfcManager: NfcManager = koinInject()
    val backStackEntry by navController.currentBackStackEntryAsState()

    if (backStackEntry != null) {
        NfcReaderEffect(
            activity = activity,
            nfcManager = nfcManager,
            isReadingPage = backStackEntry?.destination?.route == Route.Home::class.qualifiedName,
            onTag = homeVM::readTag,
        )
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Home,
    ) {
        composable<Route.Home> {
            HomeScreen(navController = navController)
        }
        composable<Route.CardDetail> {
            DetailScreen(navController = navController)
        }
        composable<Route.Vault> {
            VaultScreen(navController = navController)
        }
        composable<Route.Settings> {
            SettingsScreen(navController = navController)
        }
    }
}

@Composable
private fun NfcReaderEffect(
    activity: FragmentActivity,
    nfcManager: NfcManager,
    isReadingPage: Boolean,
    onTag: (android.nfc.Tag) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, activity, nfcManager, isReadingPage) {
        val mode = if (isReadingPage) NfcReaderMode.Read(onTag) else NfcReaderMode.Suppress
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> nfcManager.enableReaderMode(activity, mode)
                Lifecycle.Event.ON_PAUSE -> nfcManager.disableReaderMode(activity)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            nfcManager.enableReaderMode(activity, mode)
        }

        onDispose {
            nfcManager.disableReaderMode(activity)
            lifecycle.removeObserver(observer)
        }
    }
}
