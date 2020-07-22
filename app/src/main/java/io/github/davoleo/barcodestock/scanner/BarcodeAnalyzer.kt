package io.github.davoleo.barcodestock.scanner

import android.util.Log
import androidx.annotation.MainThread
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.davoleo.barcodestock.ui.MainActivity

class BarcodeAnalyzer(val graphicOverlay: GraphicOverlay) : ImageAnalysis.Analyzer {


    private val formats = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()


    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val scanner = BarcodeScanning.getClient(formats)

            scanner.process(image)
                    .addOnSuccessListener { barcodeList ->
                        onSuccess(barcodeList, graphicOverlay)
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }

        }

        imageProxy.close()
    }


    @MainThread
    fun onSuccess(results: List<Barcode>, graphicOverlay: GraphicOverlay) {

        val barcodeInCenter = results.firstOrNull() {barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox);
            box.contains(graphicOverlay.width / 2F, graphicOverlay.height / 2F)
        }

        graphicOverlay.clear()

        if (barcodeInCenter == null) {
            graphicOverlay.add(BarcodeGraphic(graphicOverlay, barcodeInCenter))
        }

        graphicOverlay.invalidate()
    }

    fun onFailure(e: Exception) {
        Log.e(MainActivity.TAG, "Barcode detection failed!", e)
    }
}