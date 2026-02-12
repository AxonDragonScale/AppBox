package com.axondragonscale.qrqanvas.model

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class Template(
    val id: String,
    val name: String,
    val style: QRStyle,
    val isPredefined: Boolean,
)
