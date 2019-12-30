package io.github.davoleo.barcodestock.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

public class ActivityAddEditBarcode extends AppCompatActivity {

    private static final String TAG = "ActivityAddBarcode";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

            //System.out.println(title + " | " + desc + " | " + code + " | " + quantity + " | " + price);

            if (!title.isEmpty() && !desc.isEmpty())
            {
                Barcode barcode = new Barcode(code, title, desc, price);
                BarcodeFileUtils.writeToFile(this, barcode);
                //MainActivity.database.barcodeDAO().insert(barcode);
                this.finish();
            }

        }catch (NumberFormatException exception) {
            exception.printStackTrace();
            Log.e(TAG, "addBarcode: Price or Code fields are not formatted correctly");
        }

    }
}
