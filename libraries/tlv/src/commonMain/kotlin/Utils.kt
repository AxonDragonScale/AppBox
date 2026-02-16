/**
 * Converts Byte to Int such that the 8 least significant bits are same as the byte and the other bits are 0.
 * The Byte.toInt() from the standard library makes the other 24 bits same as the sign bit.
 */
fun Byte.asInt() = toInt() and 0xFF

/**
 * Converts Byte to Long such that the 8 least significant bits are same as the byte and the other bits are 0.
 * The Byte.toLong() from the standard library makes the other bits same as the sign bit.
 */
fun Byte.asLong() = toLong() and 0xFF

/**
 * Convert ByteArray of size <= 4 into an Int
 */
fun ByteArray.toInt(): Int {
    check(size <= 4) { "ByteArray too large for an Int" }
    return this.fold(0) { acc, byte -> (acc shl 8) or byte.asInt() }
}

/**
 * Convert ByteArray of size <= 8 into a Long
 */
fun ByteArray.toLong(): Long {
    check(size <= 8) { "ByteArray too large for an Long" }
    return this.fold(0L) { acc, byte -> (acc shl 8) or byte.asLong() }
}

/**
 * Convert Long to a ByteArray of size 8
 */
fun Long.toByteArray(): ByteArray {
    val size = 8
    return ByteArray(size) { i ->
        val bitShift = ((size - 1) - i) * 8
        ((this shr bitShift) and 0xFF).toByte()
    }
}

/**
 * Convert Long to ByteArray of minimum viable size.
 */
fun Long.toMinimalByteArray(): ByteArray {
    val fullBytes = toByteArray()
    var start = 0
    while (start < fullBytes.lastIndex && fullBytes[start] == 0.toByte()) {
        start++
    }
    return fullBytes.sliceArray(start .. fullBytes.lastIndex)
}
