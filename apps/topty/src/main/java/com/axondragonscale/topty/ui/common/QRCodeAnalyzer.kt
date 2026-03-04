package com.axondragonscale.topty.ui.common

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.axondragonscale.topty.core.QRScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Created by Ronak Harkhani on 16/10/24
 */
class QRCodeAnalyzer(val onSuccess: (List<Barcode>) -> Unit) : ImageAnalysis.Analyzer {

    var isPaused = false

    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image == null || isPaused) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )

        QRScanner.scan(inputImage) { barcodes ->
            if (!barcodes.isNullOrEmpty()) onSuccess(barcodes)
            imageProxy.close()
        }
    }

}
