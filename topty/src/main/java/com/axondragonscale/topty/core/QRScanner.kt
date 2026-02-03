package com.axondragonscale.topty.core

import com.axondragonscale.commons.logD
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Created by Ronak Harkhani on 30/10/24
 */
object QRScanner {

    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        BarcodeScanning.getClient(options)
    }

    fun scan(image: InputImage, onComplete: (List<Barcode>?) -> Unit) {
        barcodeScanner
            .process(image)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { barcodes ->
                        this.logD { "QRScanner Successful." }
                        barcodes.forEach { this.logD { "QRCode: ${it.rawValue}" } }
                        onComplete(barcodes)
                    }
                } else {
                    val stacktrace = task.exception?.stackTraceToString()
                    this.logD { "QRScanner Failed. \n $stacktrace" }
                    onComplete(null)
                }
            }
    }

}
