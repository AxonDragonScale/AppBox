package com.axondragonscale.jest.ui.tune

/**
 * Created by Ronak Harkhani on 10/04/24
 */
sealed interface PrefsUiEvent {
    data class CategorySelectionChanged(val selectedCategories: List<Int>): PrefsUiEvent
    data class JokeTypeSelectionChanged(val selectedJokeTypes: List<Int>): PrefsUiEvent
    data class BlacklistFlagSelectionChanged(val selectedBlacklistFlags: List<Int>): PrefsUiEvent
}
