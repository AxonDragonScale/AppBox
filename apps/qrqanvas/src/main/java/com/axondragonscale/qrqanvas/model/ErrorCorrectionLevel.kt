package com.axondragonscale.qrqanvas.model

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on13/02/26
 */
@Serializable
enum class ErrorCorrectionLevel {
    L,
    M,
    Q,
    H;

    fun toZxing(): ErrorCorrectionLevel = when (this) {
        L -> ErrorCorrectionLevel.L
        M -> ErrorCorrectionLevel.M
        Q -> ErrorCorrectionLevel.Q
        H -> ErrorCorrectionLevel.H
    }
}
