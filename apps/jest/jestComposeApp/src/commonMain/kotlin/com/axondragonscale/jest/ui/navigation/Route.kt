package com.axondragonscale.jest.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 24/10/24
 */
object Route {

    @Serializable
    data class Dashboard(val initialTab: Int = 0)

    @Serializable
    data object DevTools

}
