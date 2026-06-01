package com.axondragonscale.cardy.util

/** Created by Ronak on 05/30/26 */

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex(): String = toHexString(HexFormat.UpperCase)

@OptIn(ExperimentalStdlibApi::class)
fun String.hexToBytes(): ByteArray = hexToByteArray(HexFormat { })

fun ByteArray.toHexFormatted(separator: String = " "): String =
    joinToString(separator) { it.toInt().and(0xFF).toString(16).padStart(2, '0').uppercase() }
