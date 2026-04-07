package com.axondragonscale.jest.ui.library

import com.axondragonscale.jest.model.IJoke

/**
 * Created by Ronak Harkhani on 24/10/24
 */
enum class LibraryFilter { All, Favorites }

data class LibraryUiState(
    val jokes: List<IJoke> = emptyList(),
    val activeFilter: LibraryFilter = LibraryFilter.All,
    val isLoading: Boolean = true,
)
