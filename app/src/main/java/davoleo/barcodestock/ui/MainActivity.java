package davoleo.barcodestock.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import davoleo.barcodestock.R;
import davoleo.barcodestock.barcode.Barcode;
import davoleo.barcodestock.barcode.BarcodeAdapter;
import davoleo.barcodestock.ui.dialog.AlertDialogs;
import davoleo.barcodestock.util.BarcodeFileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BarcodeAdapter adapter;
    private List<Barcode> barcodeList;
    private AlertDialogs dialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeList = BarcodeFileUtils.readAll(this);

        //database = Room.databaseBuilder(getApplicationContext(), BarcodeDatabase.class, "barcode_db").build();
//        try {
//            barcodeList = new BarcodeAsyncTask(this).execute().get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }

        //Link data source to the listview using a custom Adapter
        ListView listView = findViewById(R.id.barcodeListView);
        adapter = new BarcodeAdapter(this, barcodeList);
        listView.setAdapter(adapter);
        ListViewClickHandler handler = new ListViewClickHandler();
        listView.setOnItemClickListener(handler);

        Log.d(TAG, "onCreate: HAS STARTED SUCCESSFULLY");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class);
                startActivity(intent);

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                        .setAction("Action", null).show();
            }
        });

        //Build Alert Dialogs
        dialogs = new AlertDialogs(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus)
        {
            refreshListView(BarcodeFileUtils.readAll(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = ((SearchView) searchItem.getActionView());
        final List<Barcode> queryResult = new ArrayList<>();
        final Activity activity = this;

        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                refreshListView(BarcodeFileUtils.readAll(activity));
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                queryResult.clear();

                for (Barcode barcode : barcodeList) {
                    if (barcode.getTitle().contains(s))
                        queryResult.add(barcode);
                    if (barcode.getDescription().contains(s))
                        queryResult.add(barcode);
                    if (String.valueOf(barcode.getCode()).contains(s))
                        queryResult.add(barcode);
                }

                refreshListView(queryResult);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    public void refreshListView (List<Barcode> newList){
        adapter.getData().clear();
        adapter.getData().addAll(newList);
        adapter.notifyDataSetChanged();
    }

    public void clearBarcodeList(MenuItem item) {
        refreshListView(BarcodeFileUtils.readAll(this));
        dialogs.CLEAR_DIALOG.show();
    }



    public void sortBarcodeList(String field) {
        Barcode[] arr;
        arr = barcodeList.toArray(new Barcode[0]);
        Arrays.sort(arr);
        refreshListView(Arrays.asList(arr));
        Toast.makeText(getApplicationContext(), "Barcodes have been sorted!", Toast.LENGTH_LONG).show();
    }
}
