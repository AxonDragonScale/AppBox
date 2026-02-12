package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
data class QRStyle(
    val dotStyle: DotStyle = DotStyle.Square,
    val dotColor: ColorConfig = ColorConfig.Solid(0xFF000000),
    val eyeStyle: EyeStyle = EyeStyle.Square,
    val eyeFrameColor: ColorConfig = ColorConfig.Solid(0xFF000000),
    val eyeDotColor: ColorConfig = ColorConfig.Solid(0xFF000000),
    val backgroundColor: ColorConfig = ColorConfig.Solid(0xFFFFFFFF),
    val backgroundImageUri: String? = null,
    val logoConfig: LogoConfig? = null,
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
        if (logoConfig != null && errorCorrection != ErrorCorrectionLevel.H) {
            copy(errorCorrection = ErrorCorrectionLevel.H)
        } else {
            this
        }
}
