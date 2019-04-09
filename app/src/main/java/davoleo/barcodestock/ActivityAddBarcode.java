package davoleo.barcodestock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ActivityAddBarcode extends AppCompatActivity {

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
        String title = ((TextView)findViewById(R.id.txbTitle)).getText().toString();
        String desc = ((TextView) findViewById(R.id.txbDesc)).getText().toString();
        int code = Integer.parseInt(((TextView) findViewById(R.id.txbCode)).getText().toString());
        int quantity = Integer.parseInt(((TextView) findViewById(R.id.txbQuantity)).getText().toString());
        float price = Float.parseFloat(((TextView) findViewById(R.id.txbPrice)).getText().toString());

        //System.out.println(title + " | " + desc + " | " + code + " | " + quantity + " | " + price);



    }

    private boolean validateData(String title, String desc, int code, int quantity, float price)
    {
        return true;
    }

}
