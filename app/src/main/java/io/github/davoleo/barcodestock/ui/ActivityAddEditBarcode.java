package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.scanner.BarcodeScannerActivity;

public class ActivityAddEditBarcode extends AppCompatActivity {

    private static final String TAG = "ActivityAddBarcode";

    private Barcode selectedBarcode = null;
    private boolean editMode;

    private EditText barcodeTxb;
    private EditText titleTxb;
    private EditText descriptionTxb;
    private EditText priceTxb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        barcodeTxb = findViewById(R.id.txbCode);
        titleTxb = findViewById(R.id.txbTitle);
        descriptionTxb = findViewById(R.id.txbDesc);
        priceTxb = findViewById(R.id.txbPrice);

        editMode = getIntent().getBooleanExtra("edit", false);

        if (getIntent().hasExtra("barcode")) {
            barcodeTxb.setText(getIntent().getStringExtra("barcode"));
        }

        if (editMode) {
            selectedBarcode = Barcode.fromBundle(getIntent().getExtras());

            setTitle(R.string.title_activity_edit_barcode);
            barcodeTxb.setText(R.string.btn_save);

            barcodeTxb.setText(String.valueOf(selectedBarcode.getCode()));
            priceTxb.setText(String.valueOf(selectedBarcode.getPrice()));

            titleTxb.setText(selectedBarcode.getTitle());
            descriptionTxb.setText(selectedBarcode.getDescription());


        }
    }

    public void addBarcode(View view)
    {
        Editable txbCode = barcodeTxb.getText();
        Editable txbPrice = priceTxb.getText();

        String title = titleTxb.getText().toString();
        String desc = descriptionTxb.getText().toString();

        try {
            long code = Long.parseLong(txbCode.toString());
            float price = Float.parseFloat(txbPrice.toString());

            if (!title.isEmpty() && !desc.isEmpty()) {

                Barcode barcode = new Barcode(code, title, desc, price);
                Intent intent = new Intent();

                //Bundle the information about the old barcode
                if (editMode) {
                    Bundle barcodeBundle = selectedBarcode.toBundle();
                    intent.putExtra("oldBarcode", barcodeBundle);
                }

                //Bundle information about the new Barcode
                intent.putExtra("newBarcode", barcode.toBundle());
                setResult(RESULT_OK, intent);
                this.finish();
            }

        }catch (NumberFormatException exception) {
            exception.printStackTrace();
            Snackbar.make(view, "Barcode/Price might not be valid! Please check them before continuing.", Snackbar.LENGTH_LONG);
            Log.w(TAG, "addBarcode: Price or Code fields are not formatted correctly");
        }
    }

    /**
     * Starts the scanner Activity to add a scanned barcode to the object
     * @param view the button (view) that this method was called from
     */
    public void startScanner(View view) {
        Intent intent = new Intent(getApplicationContext(), BarcodeScannerActivity.class);
        startActivityForResult(intent, 4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 4 && resultCode == RESULT_OK && data != null) {
            String barcode = data.getStringExtra("barcode");
            if (barcode != null)
                barcodeTxb.setText(barcode);
        }
    }
}
