package com.axondragonscale.cardy.emv

internal data class Track2EquivalentData(
    val pan: String,
    val expirationDate: String,
    val serviceCode: String,
    val discretionaryData: String,
)

internal fun parseTrack2EquivalentData(value: ByteArray): Track2EquivalentData? {
    val digits = value.toHexString(HexFormat.UpperCase).trimEnd('F')
    val separator = digits.indexOf('D')
    if (separator <= 0 || digits.length < separator + 8) return null

    val pan = digits.substring(0, separator)
    val remainder = digits.substring(separator + 1)
    if (!pan.all(Char::isDigit) || !remainder.all(Char::isDigit)) return null

    return Track2EquivalentData(
        pan = pan,
        expirationDate = remainder.substring(0, 4),
        serviceCode = remainder.substring(4, 7),
        discretionaryData = remainder.drop(7),
    )
}
