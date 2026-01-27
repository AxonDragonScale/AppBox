package com.axondragonscale.jest.ui.tune

import com.axondragonscale.appdump.jest.model.BlacklistFlag
import com.axondragonscale.appdump.jest.model.Category
import com.axondragonscale.appdump.jest.model.JokeType

/**
 * Created by Ronak Harkhani on 10/04/24
 */
data class PrefsUiState(
    val selectedCategories: List<Category> = emptyList(),
    val selectedJokeTypes: List<JokeType> = emptyList(),
    val selectedBlacklistFlags: List<BlacklistFlag> = emptyList(),
)
