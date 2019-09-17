package davoleo.barcodestock;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import davoleo.barcodestock.barcode.Barcode;

public class ActivityAddBarcode extends AppCompatActivity {

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
            int code = Integer.parseInt(txbCode.toString());
            float price = Float.parseFloat(txbPrice.toString());

            //System.out.println(title + " | " + desc + " | " + code + " | " + quantity + " | " + price);
            Barcode barcode = new Barcode(code, title, desc, price);
            MainActivity.getDatabaseDAO().insert(barcode);

            this.finish();
        }catch (NumberFormatException exception) {
            Log.e(TAG, "addBarcode: Price or Code fields are not formatted correctly");
        }

    }
}
