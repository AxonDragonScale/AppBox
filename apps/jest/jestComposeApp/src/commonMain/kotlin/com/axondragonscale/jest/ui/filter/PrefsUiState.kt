package com.axondragonscale.jest.ui.filter

import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType

/**
 * Created by Ronak Harkhani on 10/04/24
 */
data class PrefsUiState(
    val selectedCategories: List<Category> = emptyList(),
    val selectedJokeTypes: List<JokeType> = emptyList(),
    val selectedBlacklistFlags: List<BlacklistFlag> = emptyList(),
)
