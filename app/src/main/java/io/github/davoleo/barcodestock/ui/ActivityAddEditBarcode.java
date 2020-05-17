package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
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

        if (getIntent().hasExtra("barcode")) {
            ((EditText) findViewById(R.id.txbCode)).setText(getIntent().getStringExtra("barcode"));
        }

        if (editMode) {
            selectedBarcode = Barcode.fromBundle(getIntent().getExtras());

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
            Log.e(TAG, "addBarcode: Price or Code fields are not formatted correctly");
        }

    }
}
