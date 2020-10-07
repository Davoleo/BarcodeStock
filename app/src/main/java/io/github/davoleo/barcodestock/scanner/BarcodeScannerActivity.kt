package io.github.davoleo.barcodestock.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.RectF
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.davoleo.barcodestock.R
import io.github.davoleo.barcodestock.ui.MainActivity
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

class BarcodeScannerActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    private val formats = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()

    private var barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(formats)

    private var preview: Preview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private val targetResolution = Size(480, 640)

    private var delayCount = 0

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        lifecycle.addObserver(barcodeScanner)
        CameraX.configureInstance(Camera2Config.defaultConfig())

        setSupportActionBar(findViewById(R.id.toolbar))

        //Request Camera Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            startCamera()
        }
        else
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Barcode Scanner requires access to the camera", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        CameraX.getOrCreateInstance(applicationContext)

        graphicOverlay = findViewById<GraphicOverlay>(R.id.cameraPreviewOverlay).apply {
            setOnClickListener { //overlay: View? ->
                run {
                    Log.i(MainActivity.TAG, "you tapped the OVERLAY :o")
                }
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, this)
                }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        CameraX.shutdown()
        super.onDestroy()
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
        val futureCameraProvider = ProcessCameraProvider.getInstance(this)

        futureCameraProvider.addListener(Runnable {
            //Used to bind lifecycle of cameras to the lifecycle owner [takes care of opening and closing the camera]
            val cameraProvider: ProcessCameraProvider = futureCameraProvider.get()

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

            if (graphicOverlay != null) {
                barcodeScanner.process(image)
                        .addOnSuccessListener { barcodeList ->
                            onSuccess(barcodeList, graphicOverlay!!)
                        }
                        .addOnFailureListener {
                            onFailure(it)
                        }
                        .addOnCompleteListener { imageProxy.close() }
            }
            else
                Log.w(MainActivity.TAG, "overlay is null :(")
        }
    }

    fun onSuccess(results: List<Barcode>, graphicOverlay: GraphicOverlay) {

        //Debug Logging ---
        //Log.i(MainActivity.TAG, "Barcode Type: " + results.firstOrNull()?.valueType)

        val reticle = BarcodeGraphic.getReticleBox(graphicOverlay)
        val widthScaleFactor: Float = graphicOverlay.width / targetResolution.width.toFloat()
        val heightScaleFactor: Float = graphicOverlay.height / targetResolution.height.toFloat()
        val barcodeInCenter: Barcode?

        barcodeInCenter = results.firstOrNull { barcode ->
            //Take barcode's bounding box, otherwise if boundingBox is null then return null as barcodeInCenter
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = translateRect(boundingBox, widthScaleFactor, heightScaleFactor)
            box.contains(reticle.centerX(), reticle.centerY())
        }

        //Debug Logging ---
        //Log.i(MainActivity.TAG, "onSuccess: $barcodeInCenter")
        //Log.i(MainActivity.TAG, "Raw Value: $barcodeInCenter")
        //val xLeft = results.firstOrNull()?.boundingBox?.left ?: -1
        //val yTop = results.firstOrNull()?.boundingBox?.top ?: -1
        //val xRight = results.firstOrNull()?.boundingBox?.right ?: -1
        //val yBottom = results.firstOrNull()?.boundingBox?.bottom ?: -1
        //Log.i(MainActivity.TAG, "barcode LTRB: $xLeft, $yTop, $xRight, $yBottom")
        //Log.i(MainActivity.TAG, "center XY: ${reticle.centerX()}, ${reticle.centerY()}")
        //Log.i(MainActivity.TAG, "center XY: ${reticle.width() / 2}, ${reticle.height() / 2}")
        graphicOverlay.clear()

        if (barcodeInCenter == null) {
            delayCount = max(0, --delayCount)
            graphicOverlay.add(BarcodeGraphic(graphicOverlay, ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)))
        }
        else {
            graphicOverlay.add(BarcodeGraphic(graphicOverlay, ContextCompat.getColor(applicationContext, R.color.pureRed)))
            delayCount = min(2, ++delayCount)

            if (delayCount == 2) {
                delayCount = 0
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notifySound = RingtoneManager.getRingtone(applicationContext, notification)
                notifySound.play()

                val intent = Intent()
                intent.putExtra("barcode", barcodeInCenter.rawValue)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        graphicOverlay.invalidate()
    }

    fun onFailure(e: Exception) {
        Log.e(MainActivity.TAG, "Barcode detection failed!", e)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 0

        /**
         * It doubles the size of the rectangle keeping the ratio if the multiplier is 1
         */
        fun increaseSize(rect: Rect, multiplier: Float): RectF {
            val right = rect.right + (multiplier * rect.width())
            val bottom = rect.bottom + (multiplier * rect.height())
            return RectF(rect.left.toFloat(), rect.top.toFloat(), right, bottom)
        }

        fun translateRect(rect: Rect, widthFactor: Float, heightFactor: Float): RectF
        {
            val left = rect.left * widthFactor
            val top = rect.top * heightFactor
            val right = rect.right * widthFactor
            val bottom = rect.bottom * heightFactor

            //Debug Prints ---
            //Log.i(MainActivity.TAG, "translateRect: Factors: $widthFactor, $heightFactor")
            Log.i(MainActivity.TAG, "translateRect LTRB: $left, $top, $right, $bottom")

            return RectF(left, top, right, bottom)
        }
    }
}
