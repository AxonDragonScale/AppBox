package com.axondragonscale.devinator.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.commons.UIMode
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.repository.AppPrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */

/**
 * Appearance is the only state this screen owns, and it lives in prefs. The grant is not state at
 * all: it is re-read on every resume, because it can change outside the app.
 */
class SettingsVM(
    private val prefs: AppPrefsRepository,
    context: Context,
) : ViewModel() {

    // Application context, not an activity: holding it cannot leak a screen.
    @SuppressLint("StaticFieldLeak")
    private val context: Context = context.applicationContext

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            permissionGranted = SecureSettingsPermission.isGranted(context),
            versionName = AppInfo.versionName,
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.uiMode.flow, prefs.dynamicColor.flow) { mode, dynamic -> mode to dynamic }
                .collect { (mode, dynamic) ->
                    _uiState.update { it.copy(uiMode = mode, dynamicColor = dynamic) }
                }
        }
    }

    fun onEvent(event: SettingsUiEvent): Any = when (event) {
        is SettingsUiEvent.SetUiMode -> viewModelScope.launch { prefs.uiMode.set(event.mode) }
        is SettingsUiEvent.SetDynamicColor -> viewModelScope.launch {
            prefs.dynamicColor.set(event.enabled)
        }

        SettingsUiEvent.OnResume -> refreshGrant()
    }

    private fun refreshGrant() {
        val granted = SecureSettingsPermission.isGranted(context)
        _uiState.update { it.copy(permissionGranted = granted) }
    }
}

data class SettingsUiState(
    val uiMode: UIMode = UIMode.DEFAULT,
    val dynamicColor: Boolean = false,
    val permissionGranted: Boolean = true,
    val versionName: String = "",
)

sealed interface SettingsUiEvent {
    data class SetUiMode(val mode: UIMode) : SettingsUiEvent
    data class SetDynamicColor(val enabled: Boolean) : SettingsUiEvent
    data object OnResume : SettingsUiEvent
}
