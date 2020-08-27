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
import com.google.android.material.textfield.TextInputLayout;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.VAT;
import io.github.davoleo.barcodestock.scanner.BarcodeScannerActivity;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

public class ActivityAddEditBarcode extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final String TAG = "ActivityAddBarcode";

    private Barcode selectedBarcode = null;
    private boolean editMode;

    private Button confirmButton;

    // Inputs
    private EditText barcodeTxb;
    private EditText titleTxb;
    private EditText descriptionTxb;
    private EditText priceTxb;
    private AutoCompleteTextView vatTxb;

    private ArrayAdapter<String> vatAdapter;

    //Input Layouts (Material Outlines)
    TextInputLayout titleLayout;
    TextInputLayout descLayout;
    TextInputLayout priceLayout;
    TextInputLayout barcodeLayout;

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

        //When the button to clear the VAT box is clicked
        findViewById(R.id.buttonClearVat).setOnClickListener(view -> {
            vatTxb.setText("");
            vatTxb.clearFocus();
        });

        confirmButton = findViewById(R.id.button);

        barcodeTxb = findViewById(R.id.txbCode);
        titleTxb = findViewById(R.id.txbTitle);
        descriptionTxb = findViewById(R.id.txbDesc);
        priceTxb = findViewById(R.id.txbPrice);
        vatTxb = findViewById(R.id.spinnerVat);

        titleLayout = findViewById(R.id.title_layout);
        descLayout = findViewById(R.id.desc_layout);
        priceLayout = findViewById(R.id.price_layout);
        barcodeLayout = findViewById(R.id.barcode_layout);

        barcodeTxb.setOnFocusChangeListener(this);
        titleTxb.setOnFocusChangeListener(this);
        descriptionTxb.setOnFocusChangeListener(this);
        priceTxb.setOnFocusChangeListener(this);

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

            if (selectedBarcode.getVat() != null)
                vatTxb.setText(selectedBarcode.getVat().toString());
        }

    }

    private void validateInputs() {
        onFocusChange(barcodeTxb, false);
        onFocusChange(titleTxb, false);
        onFocusChange(descriptionTxb, false);
        onFocusChange(priceTxb, false);
    }

    /**
     * Called when any of the inputs change focus state<br>
     * Validates user input
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        if (hasFocus)
            return;

        switch (view.getId()) {
            case R.id.txbTitle:
                if (titleTxb.getText().toString().isEmpty())
                    titleLayout.setError(getString(R.string.empty_input_error));
                else if (titleTxb.getText().toString().contains(String.valueOf(BarcodeFileUtils.SEPARATOR_CHAR)))
                    titleLayout.setError(getString(R.string.invalid_char_error));
                else
                    titleLayout.setError(null);
                break;

            case R.id.txbDesc:
                if (titleTxb.getText().toString().contains(String.valueOf(BarcodeFileUtils.SEPARATOR_CHAR)))
                    descLayout.setError(getString(R.string.invalid_char_error));
                else
                    descLayout.setError(null);

            case R.id.txbPrice:
                if (priceTxb.getText().toString().isEmpty())
                    priceLayout.setError(getString(R.string.empty_input_error));
                else
                    priceLayout.setError(null);

            case R.id.txbCode:
                if (barcodeTxb.getText().toString().isEmpty())
                    barcodeLayout.setError(getString(R.string.empty_input_error));
                else
                    barcodeLayout.setError(null);

        }
    }

    public void addBarcode(View view)
    {
        validateInputs();

        if (titleLayout.getError() != null || descLayout.getError() != null || priceLayout.getError() != null || barcodeLayout.getError() != null)
            return;

        Editable txbCode = barcodeTxb.getText();
        Editable txbPrice = priceTxb.getText();

        String title = titleTxb.getText().toString();
        String desc = descriptionTxb.getText().toString();

        try {
            long code = Long.parseLong(txbCode.toString());
            float price = Float.parseFloat(txbPrice.toString());

            //If the description is empty set it to a special encoded empty description
            if (desc.isEmpty()) {
                desc = BarcodeFileUtils.EMPTY_DESC;
            }

            //If the VAT is empty set it to null
            VAT vat = null;
            if (!vatTxb.getText().toString().isEmpty()) {
                int vatValue = Integer.parseInt(vatTxb.getText().toString().replace("%", ""));
                vat = VAT.byValue(vatValue);
            }

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
        catch (NumberFormatException exception) {
            exception.printStackTrace();
            Snackbar.make(view,
                    "Invalid Number Format! This error should have been caught earlier, please report it on github", Snackbar.LENGTH_LONG).show();
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
