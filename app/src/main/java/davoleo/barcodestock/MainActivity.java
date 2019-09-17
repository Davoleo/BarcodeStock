package davoleo.barcodestock;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.room.Room;
import davoleo.barcodestock.barcode.Barcode;
import davoleo.barcodestock.barcode.BarcodeAdapter;
import davoleo.barcodestock.barcode.BarcodeDAO;
import davoleo.barcodestock.barcode.BarcodeDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static BarcodeDatabase database;

    private BarcodeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: 17/09/2019 Create Async Task to do this instead of doing it in the main thread
        database = Room.databaseBuilder(getApplicationContext(), BarcodeDatabase.class, "barcode-db").allowMainThreadQueries().build();

        //Link database data to the listview using a custom Adapter
        ListView listView = findViewById(R.id.barcodeListView);
        adapter = new BarcodeAdapter(this, getDatabaseDAO().getAll());
        listView.setAdapter(adapter);

        Log.d(TAG, "onCreate: HAS STARTED SUCCESSFULLY");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), ActivityAddBarcode.class);
                startActivity(intent);

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = ((SearchView) searchItem.getActionView());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //TODO Filter Barcode List
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    public static BarcodeDAO getDatabaseDAO() {
        return database.barcodeDAO();
    }
}
