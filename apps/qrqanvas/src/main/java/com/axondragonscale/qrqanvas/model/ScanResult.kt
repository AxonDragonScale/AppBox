package com.axondragonscale.qrqanvas.model

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class ScanResult(
    val rawValue: String,
    val content: QRContent,
    val timestamp: Long = System.currentTimeMillis(),
)
