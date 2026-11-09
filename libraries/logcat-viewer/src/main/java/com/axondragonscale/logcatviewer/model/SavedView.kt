package com.axondragonscale.logcatviewer.model

import kotlinx.serialization.Serializable

/** Created by Ronak on 09/11/26 */

@Serializable
data class SavedView(
    val id: Long,
    val name: String,
    val filter: LogFilter,
)
