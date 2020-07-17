package io.github.davoleo.barcodestock.scanner

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.davoleo.barcodestock.ui.MainActivity

class BarcodeAnalyzer : ImageAnalysis.Analyzer {

    private val formats = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val scanner = BarcodeScanning.getClient(formats)
            val result = scanner.process(image)
                    .addOnSuccessListener { barcodeList ->
                        for (barcode in barcodeList) {
                            println(barcode.displayValue)
                        }
                    }
                    .addOnFailureListener {
                        // Scanning Failure:
                        // Called whenever the scanner couldn't find a valid barcode
                        // This is not an exceptional application state
                    }

        }

        imageProxy.close()
    }

    companion object {
        fun onSuccess(barcodes: List<Barcode>, graphicOverlay: GraphicOverlay) {
            if (barcodes.isEmpty()) {
                Log.v(MainActivity.TAG, "No barcodes have been detected")
            }

            barcodes.forEach { barcode ->
                graphicOverlay.add(BarcodeGraphic(graphicOverlay, barcode))
            }
        }
    }
}