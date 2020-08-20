package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.VAT;
import io.github.davoleo.barcodestock.scanner.BarcodeScannerActivity;

public class ActivityAddEditBarcode extends AppCompatActivity {

    private static final String TAG = "ActivityAddBarcode";

    private Barcode selectedBarcode = null;
    private boolean editMode;

    private Button confirmButton;

    private EditText barcodeTxb;
    private EditText titleTxb;
    private EditText descriptionTxb;
    private EditText priceTxb;

    private String[] vatStrings;
    private ArrayAdapter<String> vatAdapter;
    private AutoCompleteTextView vatTxb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        //Initialize the adapter
        vatAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.dropdown_vat_item);
        for (VAT value : VAT.values()) {
            vatAdapter.add(value.toString());
        }

        confirmButton = findViewById(R.id.button);

        barcodeTxb = findViewById(R.id.txbCode);
        titleTxb = findViewById(R.id.txbTitle);
        descriptionTxb = findViewById(R.id.txbDesc);
        priceTxb = findViewById(R.id.txbPrice);
        vatTxb = findViewById(R.id.spinnerVat);
        vatTxb.setAdapter(vatAdapter);

        editMode = getIntent().getBooleanExtra("edit", false);

        if (getIntent().hasExtra("barcode")) {
            barcodeTxb.setText(getIntent().getStringExtra("barcode"));
        }

        if (editMode) {
            selectedBarcode = Barcode.fromBundle(getIntent().getExtras());

            setTitle(R.string.title_activity_edit_barcode);
            confirmButton.setText(R.string.btn_save);

            barcodeTxb.setText(String.valueOf(selectedBarcode.getCode()));
            priceTxb.setText(String.valueOf(selectedBarcode.getPrice()));

            titleTxb.setText(selectedBarcode.getTitle());
            descriptionTxb.setText(selectedBarcode.getDescription());

            vatTxb.setText(selectedBarcode.getVat().toString());
        }

    }

    public void addBarcode(View view)
    {
        Editable txbCode = barcodeTxb.getText();
        Editable txbPrice = priceTxb.getText();

        String title = titleTxb.getText().toString();
        String desc = descriptionTxb.getText().toString();

        try {
            int vatValue = Integer.parseInt(vatTxb.getText().toString().replace("%", ""));
            VAT vat = VAT.byValue(vatValue);

            long code = Long.parseLong(txbCode.toString());
            float price = Float.parseFloat(txbPrice.toString());

            if (!title.isEmpty() && !desc.isEmpty() && !title.contains("§") && !desc.contains("§")) {

                Barcode barcode = new Barcode(code, title, desc, price, vat);
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
        }
        catch (NumberFormatException exception) {
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
