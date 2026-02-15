package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
data class LogoData(
    val uri: String,
    val size: Float = 0.25f,
    val padding: Float = 0.05f,
    val shape: LogoShape = LogoShape.RoundedRect,
)
