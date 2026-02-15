package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
data class QRStyle(
    val dotStyle: DotStyle = DotStyle.Square,
    val dotColor: ColorData = ColorData.Solid(0xFF000000),
    val eyeStyle: EyeStyle = EyeStyle.Square,
    val eyeFrameColor: ColorData = ColorData.Solid(0xFF000000),
    val eyeDotColor: ColorData = ColorData.Solid(0xFF000000),
    val backgroundColor: ColorData = ColorData.Solid(0xFFFFFFFF),
    val backgroundImageUri: String? = null,
    val logoData: LogoData? = null,
    val errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    val quietZone: Int = 1,
    val size: Int = 1024,
) {

    /**
     * Returns a copy with error correction forced to [ErrorCorrectionLevel.H]
     * if a logo is embedded, since the logo obscures modules and high error
     * correction is needed to keep the code scannable.
     */
    fun withEnforcedLogoErrorCorrection(): QRStyle =
        if (logoData != null && errorCorrection != ErrorCorrectionLevel.H) {
            copy(errorCorrection = ErrorCorrectionLevel.H)
        } else {
            this
        }
}
