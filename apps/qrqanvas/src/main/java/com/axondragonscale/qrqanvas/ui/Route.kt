package com.axondragonscale.qrqanvas.ui

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 13/02/26
 */
object Route {

    @Serializable
    data object Home

    @Serializable
    data class Creator(val qrCodeId: Long? = null)

    @Serializable
    data class Detail(val qrCodeId: Long)

    @Serializable
    data object Scanner

    @Serializable
    data object History

    @Serializable
    data object Templates

    @Serializable
    data object Settings

    @Serializable
    data object DevTools
}
