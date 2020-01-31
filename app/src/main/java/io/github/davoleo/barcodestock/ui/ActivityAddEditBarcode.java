package io.github.davoleo.barcodestock.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

public class ActivityAddEditBarcode extends AppCompatActivity {

    private static final String TAG = "ActivityAddBarcode";

    private Barcode selectedBarcode = null;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editMode = getIntent().getBooleanExtra("edit", false);

        if (editMode) {
            Bundle barcodeBundle = getIntent().getBundleExtra("barcode");
            selectedBarcode = new Barcode(
                    barcodeBundle.getLong("code"),
                    barcodeBundle.getString("title"),
                    barcodeBundle.getString("desc"),
                    barcodeBundle.getFloat("price"));

            setTitle(R.string.title_activity_edit_barcode);
            ((Button) findViewById(R.id.button)).setText(R.string.btn_save);

            ((EditText) findViewById(R.id.txbCode)).setText(String.valueOf(selectedBarcode.getCode()));
            ((EditText) findViewById(R.id.txbPrice)).setText(String.valueOf(selectedBarcode.getPrice()));

            ((EditText)findViewById(R.id.txbTitle)).setText(selectedBarcode.getTitle());
            ((EditText) findViewById(R.id.txbDesc)).setText(selectedBarcode.getDescription());
        }
    }

    public void addBarcode(View view)
    {
        Editable txbCode = ((EditText) findViewById(R.id.txbCode)).getText();
        Editable txbPrice = ((EditText) findViewById(R.id.txbPrice)).getText();

        String title = ((EditText)findViewById(R.id.txbTitle)).getText().toString();
        String desc = ((EditText) findViewById(R.id.txbDesc)).getText().toString();

        try {
            long code = Long.parseLong(txbCode.toString());
            float price = Float.parseFloat(txbPrice.toString());

            if (!title.isEmpty() && !desc.isEmpty()) {
                Barcode barcode = new Barcode(code, title, desc, price);
                BarcodeFileUtils.writeToFile(this, barcode);

                setResult(RESULT_OK);
                this.finish();
            }

        }catch (NumberFormatException exception) {
            exception.printStackTrace();
            Log.e(TAG, "addBarcode: Price or Code fields are not formatted correctly");
        }

    }
}
