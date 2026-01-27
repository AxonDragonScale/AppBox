package com.axondragonscale.wallmatic.ui.folder

import com.axondragonscale.wallmatic.model.FullFolder

/**
 * Created by Ronak Harkhani on 17/09/24
 */
data class FolderUiState(
    val gridSize: Int = 2,
    val folder: FullFolder? = null,
)
