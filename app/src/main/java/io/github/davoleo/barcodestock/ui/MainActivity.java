package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.BarcodeAdapter;
import io.github.davoleo.barcodestock.barcode.BarcodeComparator;
import io.github.davoleo.barcodestock.scanner.BarcodeScannerActivity;
import io.github.davoleo.barcodestock.ui.dialog.AlertDialogs;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BarcodeStock";
    public static WeakReference<MainActivity> INSTANCE;

    private BarcodeAdapter adapter;
    private List<Barcode> barcodeList;
    private AlertDialogs dialogs;

    private int selectedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        INSTANCE = new WeakReference<>(this);
        barcodeList = BarcodeFileUtils.readAll(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortingOption = sharedPreferences.getString(SettingsFragment.SORTING_OPTION, "TITLE");
        BarcodeComparator comparator = new BarcodeComparator(Barcode.BarcodeFields.valueOf(sortingOption));
        Collections.sort(barcodeList, comparator);

        //database = Room.databaseBuilder(getApplicationContext(), BarcodeDatabase.class, "barcode_db").build();
//        try {
//            barcodeList = new BarcodeAsyncTask(this).execute().get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }

        //Link data source to the listview using a custom Adapter
        final ListView listView = findViewById(R.id.barcodeListView);
        adapter = new BarcodeAdapter(this, barcodeList);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick: POSITION VALUE " + position);
                selectedItemId = position;
                listView.showContextMenu();
                return true;
            }
        });

        //Register for Floating Contextual Menu
        registerForContextMenu(listView);

        Log.d(TAG, "onCreate: HAS STARTED SUCCESSFULLY");

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", false);
                startActivityForResult(intent, 1);

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                        .setAction("Action", null).show();
            }
        });

        //Build Alert Dialogs
        dialogs = new AlertDialogs(this);
    }

    //Get the added barcode back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (requestCode == 1 && resultCode == RESULT_OK) {
                Bundle barcodeBundle = data.getBundleExtra("newBarcode");
                BarcodeFileUtils.writeToFile(this, Barcode.fromBundle(barcodeBundle));
                refreshListView(BarcodeFileUtils.readAll(this));
            }

            if (requestCode == 2 && resultCode == RESULT_OK) {
                Bundle oldBarcode = data.getBundleExtra("oldBarcode");
                Barcode newBarcode = Barcode.fromBundle(data.getBundleExtra("newBarcode"));
                adapter.remove(oldBarcode.getLong("code"), oldBarcode.getString("title"), oldBarcode.getString("desc"), oldBarcode.getFloat("price"));
                BarcodeFileUtils.overwriteListToFile(this, barcodeList);
                BarcodeFileUtils.writeToFile(this, newBarcode);
                refreshListView(BarcodeFileUtils.readAll(this));
            }

            if ((requestCode == 3 || requestCode == 4) && resultCode == RESULT_OK) {
                refreshListView(BarcodeFileUtils.readAll(this));

                String barcode = data.getStringExtra("barcode");
                boolean exists = barcodeList.stream()
                        .map(listBarcode -> String.valueOf(listBarcode.getCode()))
                        .anyMatch(listBarcode -> listBarcode.equals(barcode));

                if (requestCode == 4)
                    exists = false;

                if (exists) {
                    SearchView searchView = findViewById(R.id.action_search);
                    searchView.setIconified(false);
                    searchView.setQuery(barcode, true);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", false).putExtra("barcode", barcode);
                    startActivityForResult(intent, 1);
                }
            }
        } else {
            Log.w(TAG, "The activity result intent is null!");
        }
    }

    //Build List Context Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contextual, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Barcode barcode = ((Barcode) adapter.getItem(selectedItemId));

        if (item.getItemId() == R.id.context_edit_barcode) {
            Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", true);
            Bundle bundle = barcode.toBundle();
            intent.putExtras(bundle);
            startActivityForResult(intent, 2);
            return true;
        }
        else if (item.getItemId() == R.id.context_remove_barcode) {
            adapter.remove(barcode);
            BarcodeFileUtils.overwriteListToFile(this, barcodeList);
            return true;
        } else
            return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = ((SearchView) searchItem.getActionView());
        final List<Barcode> queryResult = new ArrayList<>();
        final Barcode[] cachedBarcodes = barcodeList.toArray(new Barcode[]{});

        searchView.clearFocus();
        searchView.onActionViewCollapsed();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                queryResult.clear();

                for (Barcode barcode : cachedBarcodes) {
                    if (barcode.getTitle().contains(s) || barcode.getDescription().contains(s) || String.valueOf(barcode.getCode()).contains(s)) {
                        if (!queryResult.contains(barcode)) {
                            queryResult.add(barcode);
                        }
                    }
                }

                //TODO find some way to optimize UI refresh
                refreshListView(queryResult);
                return true;
            }
        });

        return true;
    }

    //UI Refresh and Clear -----------------------------------------
    public void refreshListView (List<Barcode> newList) {
        adapter.getData().clear();
        sortBarcodeList(newList);
        adapter.getData().addAll(newList);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "refreshListView: |!|!|!|!|!|  CALLED  |!|!|!|!|!|");
    }

    public void refreshClearBarcodeList(MenuItem item) {
        refreshListView(BarcodeFileUtils.readAll(this));

        if (item.getItemId() == R.id.action_clear)
            dialogs.CLEAR_DIALOG.show();
    }

    //Sorting methods ---------------------------------------------
    // TODO: 25/06/2020 Find a way to refresh the main list automatically when needed
    public void sortBarcodeList(List<Barcode> list) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortingOption = sharedPreferences.getString(SettingsFragment.SORTING_OPTION, "TITLE");
        BarcodeComparator comparator = new BarcodeComparator(Barcode.BarcodeFields.valueOf(sortingOption));

        Collections.sort(list, comparator);
    }

    //Search/Add from Scanner
    public void startBarcodeScanner(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), BarcodeScannerActivity.class);
        startActivityForResult(intent, 3);
    }

    public void startSettingsActivity(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
