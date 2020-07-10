package io.github.davoleo.barcodestock.scanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import io.github.davoleo.barcodestock.R;

import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity {

    private BarcodeScannerOptions formats = new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build();
    private CameraManager manager;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
    }


    private boolean requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 3);
            return true;
        }

        return false;
    }

    private void openCamera(int width, int height) {
        if (requestCameraPermissions()) {
            return;
        }
    }

    private void scanBarcode() {
        BarcodeScanner scanner = BarcodeScanning.getClient(formats);

        Task<List<Barcode>> result = scanner.process(null);
    }

}
