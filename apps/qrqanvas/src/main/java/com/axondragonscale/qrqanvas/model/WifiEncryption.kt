package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
enum class WifiEncryption(val protocol: String) {
    WPA("WPA"),
    WEP("WEP"),
    Open("nopass"),
}
