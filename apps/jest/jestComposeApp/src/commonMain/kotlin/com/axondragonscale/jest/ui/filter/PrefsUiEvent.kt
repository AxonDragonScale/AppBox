package com.axondragonscale.jest.ui.filter

/**
 * Created by Ronak Harkhani on 10/04/24
 */
sealed interface PrefsUiEvent {
    data class CategorySelectionChanged(val selectedCategories: List<Int>) : PrefsUiEvent
    data class JokeTypeSelectionChanged(val selectedJokeTypes: List<Int>) : PrefsUiEvent
    data class BlacklistFlagSelectionChanged(val selectedBlacklistFlags: List<Int>) : PrefsUiEvent
    data object CategoryCleared : PrefsUiEvent
    data object FlagsCleared : PrefsUiEvent
}
