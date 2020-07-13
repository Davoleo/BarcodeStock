package io.github.davoleo.barcodestock.scanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.ui.MainActivity;
import io.github.davoleo.barcodestock.util.CompareSizeByArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_PERMISSION_RESULT = 0;

    private BarcodeScannerOptions formats = new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build();

    private String cameraId;
    private Size previewSize;
    //Represents a mobile camera device
    private CameraDevice cameraDevice;
    //Return a specific Camera Device
    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        //Clean up memory on disconnect and error
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private CaptureRequest.Builder captureRequestBuilder;

    //Components
    private TextureView textureView;
    //Notifies when the textureView is available
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        textureView = findViewById(R.id.cameraPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        //Checks if the textureview is available
        if (textureView.isAvailable()) {
            setupCamera(textureView.getWidth(), textureView.getHeight());
            connectCamera();
        }
        else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void setupCamera(int width, int height) {
        CameraManager manager = ((CameraManager) getSystemService(Context.CAMERA_SERVICE));

        assert manager != null;
        //Loop through all the available camera IDs (usually front and rear but there could be more on some devices)
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int portraitWidth = height;
                int portraitHeight = width;

                //Swap the values because the app is always in portrait mode (and the width and height are calculated in landscape mode)
                previewSize = chooseOptimalSize(configMap.getOutputSizes(SurfaceTexture.class), width, height);
                cameraId = id;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(MainActivity.TAG, "setupCamera: Something Went wrong while accessing the Camera Devices", e);
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = ((CameraManager) getSystemService(Context.CAMERA_SERVICE));

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, cameraDeviceStateCallback, backgroundHandler);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "Barcode Scanner requires access to camera", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_PERMISSION_RESULT);
            }
        } catch (CameraAccessException e) {
            Log.e(MainActivity.TAG, "connectCamera: There was a problem connecting to the Camera", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Barcode Scanner requires access to camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        Log.e(MainActivity.TAG, "startPreview: There was a problem accessing the camera", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(BarcodeScannerActivity.this, "Unable to setup camera preview correctly", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(MainActivity.TAG, "startPreview: There was a problem accessing the camera", e);
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    //Setup the background thread (to avoid doing things on the UI Thread)
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("BarcodeStockCameraService");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            Log.e(MainActivity.TAG, "stopBackgroundThread: The thread was interrupted while waiting to die :c", e);
            e.printStackTrace();
        } finally {
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();

        for (Size size : choices) {
            //check if the ratio matches and that the supported size is greater than the preview size
            if (/*size.getHeight() == size.getWidth() * height / width &&*/ size.getWidth() >= width && size.getHeight() >= height) {
                bigEnough.add(size);
            }
        }

        if (!bigEnough.isEmpty()) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            Log.w(MainActivity.TAG, "chooseOptimalSize: NO COMPATIBLE SIZE", null);
            return choices[0];
        }
    }

    private void scanBarcode() {
        BarcodeScanner scanner = BarcodeScanning.getClient(formats);

        Task<List<Barcode>> result = scanner.process(null);
    }

}
