package io.github.davoleo.barcodestock.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.davoleo.barcodestock.R
import io.github.davoleo.barcodestock.ui.MainActivity
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    private val formats = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()

    private var preview: Preview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        setSupportActionBar(findViewById(R.id.toolbar))

        //Request Camera Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            startCamera()
        else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Barcode Scanner requires access to the camera", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        graphicOverlay = findViewById<GraphicOverlay>(R.id.cameraPreviewOverlay).apply {
            setOnClickListener { overlay: View? ->
                run {
                    Log.i(MainActivity.TAG, "you tapped the OVERLAY :o")
                }
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, this)
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Barcode Scanner requires access to the camera", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this)

        cameraProvider.addListener(Runnable {
            //Used to bind lifecycle of cameras to the lifecycle owner [takes care of opening and closing the camera]
            val cameraProvider: ProcessCameraProvider = cameraProvider.get()

            imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

            //preview
            preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                //Unbind all previous use cases before rebinding
                cameraProvider.unbindAll()

                //Bind our use cases to camera
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                preview?.setSurfaceProvider(cameraPreview.createSurfaceProvider())
            } catch (e: Exception) {
                Log.e(MainActivity.TAG, "There was an issue binding the new use case", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient(formats)

            if (graphicOverlay != null) {
                val result = scanner.process(image)
                        .addOnSuccessListener { barcodeList ->
                            onSuccess(barcodeList, graphicOverlay!!)
                        }
                        .addOnFailureListener {
                            onFailure(it)
                        }
            }
            else
                Log.w(MainActivity.TAG, "overlay is null :(")

        }

        imageProxy.close()
    }

    @MainThread
    fun onSuccess(results: List<Barcode>, graphicOverlay: GraphicOverlay) {

        Log.i(MainActivity.TAG, "Raw Value: " + results.firstOrNull()?.rawValue)
        val barcodeInCenter = results.firstOrNull()
//        {barcode ->
//            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
//            val box = graphicOverlay.translateRect(boundingBox);
//            box.contains(graphicOverlay.width / 2F, graphicOverlay.height / 2F)
//        }

        graphicOverlay.clear()

        if (barcodeInCenter != null) {
            //graphicOverlay.add(BarcodeGraphic(graphicOverlay, barcodeInCenter))

        }

        graphicOverlay.invalidate()
    }

    fun onFailure(e: Exception) {
        Log.e(MainActivity.TAG, "Barcode detection failed!", e)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 0
    }
}
