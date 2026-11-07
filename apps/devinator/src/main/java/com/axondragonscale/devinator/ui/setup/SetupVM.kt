package com.axondragonscale.devinator.ui.setup

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Created by Ronak on 11/06/26 */

/**
 * The grant arrives from outside the app (`pm grant`, or the user returning from Settings), so the
 * state is a fresh read on every resume instead of anything stored.
 */
class SetupVM(
    context: Context,
) : ViewModel() {

    // Application context, not an activity: holding it cannot leak a screen.
    @SuppressLint("StaticFieldLeak")
    private val context: Context = context.applicationContext

    private val _uiState = MutableStateFlow(
        SetupUiState(grantCommand = SecureSettingsPermission.grantCommand(context.packageName)),
    )
    val uiState = _uiState.asStateFlow()

    init { check(explicit = false) }

    fun onEvent(event: SetupUiEvent): Any = when (event) {
        SetupUiEvent.CheckAgain -> check(explicit = true)
        SetupUiEvent.OnResume -> check(explicit = false)
    }

    /**
     * The only state rule: the flag follows the live grant, and [SetupUiState.checkFailed] is set only
     * by an explicit check — an automatic one has to stay silent.
     */
    private fun check(explicit: Boolean) {
        val granted = SecureSettingsPermission.isGranted(context)
        _uiState.update {
            it.copy(permissionGranted = granted, checkFailed = explicit && !granted)
        }
    }
}

data class SetupUiState(
    val grantCommand: String = "",
    val permissionGranted: Boolean = false,
    val checkFailed: Boolean = false,
)

sealed interface SetupUiEvent {
    data object CheckAgain : SetupUiEvent
    data object OnResume : SetupUiEvent
}
