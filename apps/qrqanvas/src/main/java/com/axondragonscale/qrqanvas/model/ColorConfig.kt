package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 13/02/26
 */
@Serializable
sealed interface ColorConfig {

    @Serializable
    data class Solid(val color: Long) : ColorConfig

    @Serializable
    data class LinearGradient(
        val colors: List<Long>,
        val angleDegrees: Float = 0f,
    ) : ColorConfig

    @Serializable
    data class RadialGradient(
        val colors: List<Long>,
        val centerX: Float = 0.5f,
        val centerY: Float = 0.5f,
    ) : ColorConfig
}
