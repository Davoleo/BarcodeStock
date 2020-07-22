package io.github.davoleo.barcodestock.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import io.github.davoleo.barcodestock.R
import io.github.davoleo.barcodestock.ui.MainActivity
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerActivity : AppCompatActivity(), View.OnClickListener {

    private var preview: Preview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var promptChip: Chip? = null
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
            setOnClickListener(this@BarcodeScannerActivity)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(graphicOverlay!!))
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
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
                preview?.setSurfaceProvider(cameraPreview.createSurfaceProvider())
            } catch (e: Exception) {
                Log.e(MainActivity.TAG, "There was an issue binding the new use case", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onClick(v: View?) {
//        TODO("Not yet implemented")
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 0
    }
}
