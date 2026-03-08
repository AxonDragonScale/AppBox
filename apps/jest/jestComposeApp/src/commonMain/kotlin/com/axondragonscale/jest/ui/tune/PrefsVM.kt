package com.axondragonscale.jest.ui.tune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.repository.PrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 10/04/24
 */
class PrefsVM(
    private val prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<PrefsUiState> =
        prefsUiStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PrefsUiState()
            )

    fun onEvent(event: PrefsUiEvent) = viewModelScope.launch(Dispatchers.Default) {
        when (event) {
            is PrefsUiEvent.CategorySelectionChanged -> {
                prefsRepository.jokeCategories.set(
                    event.selectedCategories.map { Category.fromOrdinal(it) }
                )
            }

            is PrefsUiEvent.JokeTypeSelectionChanged -> {
                prefsRepository.jokeTypes.set(
                    event.selectedJokeTypes.map { JokeType.fromOrdinal(it) }
                )
            }

            is PrefsUiEvent.BlacklistFlagSelectionChanged -> {
                prefsRepository.blacklistFlags.set(
                    event.selectedBlacklistFlags.map { BlacklistFlag.fromOrdinal(it) }
                )
            }
        }
    }

    private fun prefsUiStateFlow(): Flow<PrefsUiState> =
        combine(
            prefsRepository.jokeCategories.flow,
            prefsRepository.jokeTypes.flow,
            prefsRepository.blacklistFlags.flow
        ) { jokeCategories, jokeTypes, blacklistFlags ->
            PrefsUiState(
                selectedCategories = jokeCategories,
                selectedJokeTypes = jokeTypes,
                selectedBlacklistFlags = blacklistFlags,
            )
        }

}
