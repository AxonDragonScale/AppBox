package com.axondragonscale.limitless.ui

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 30/10/24
 */
object Route {

    @Serializable
    data object Home

    @Serializable
    data object Settings

    @Serializable
    data object Stats

    @Serializable
    data class Editor(val habitId: Long? = null)

}
