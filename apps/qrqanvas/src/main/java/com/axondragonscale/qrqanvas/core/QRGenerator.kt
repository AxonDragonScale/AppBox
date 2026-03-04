package com.axondragonscale.qrqanvas.core

import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRErrorCorrection
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * ZXing wrapper that encodes [QRContent] into a [BitMatrix].
 *
 * The returned BitMatrix has exactly 1 cell per QR module (no scaling, no margin).
 * The renderer is responsible for mapping modules to pixels at the desired output size.
 */
object QRGenerator {

    private val writer = QRCodeWriter()

    /**
     * Generates a [BitMatrix] from the given [content].
     *
     * @param content The QR content to encode.
     * @param errorCorrection The error correction level. Higher levels produce denser codes
     *   but allow more damage/obstruction (e.g. logo embedding requires [QRErrorCorrection.H]).
     * @return A square [BitMatrix] where `true` = dark module, `false` = light module.
     *   Each cell corresponds to exactly one QR module.
     * @throws com.google.zxing.WriterException if the content cannot be encoded.
     */
    fun generate(
        content: QRContent,
        errorCorrection: QRErrorCorrection = QRErrorCorrection.M,
    ): BitMatrix {
        val encoded = QRContentCodec.encode(content)
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to errorCorrection.toZxing(),
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        // Requesting size=1 with margin=0 causes ZXing to return the minimum BitMatrix
        // where each QR module occupies exactly 1 cell:
        //   outputWidth = max(1, moduleCount) = moduleCount
        //   multiple = moduleCount / moduleCount = 1
        return writer.encode(encoded, BarcodeFormat.QR_CODE, 1, 1, hints)
    }
}
