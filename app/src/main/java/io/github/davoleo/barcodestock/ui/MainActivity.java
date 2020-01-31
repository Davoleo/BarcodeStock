package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.BarcodeAdapter;
import io.github.davoleo.barcodestock.ui.dialog.AlertDialogs;
import io.github.davoleo.barcodestock.ui.dialog.SortingDialogFragment;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SortingDialogFragment.SortingDialogListener {

    private static final String TAG = "MainActivity";

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

        barcodeList = BarcodeFileUtils.readAll(this);

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

        FloatingActionButton fab = findViewById(R.id.fab);
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

        if (requestCode == 1 && resultCode == RESULT_OK) {
            refreshListView(BarcodeFileUtils.readAll(this));
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
            Bundle bundle = new Bundle();
            bundle.putFloat("price", barcode.getPrice());
            bundle.putString("title", barcode.getTitle());
            bundle.putString("desc", barcode.getDescription());
            bundle.putLong("code", barcode.getCode());
            intent.putExtra("barcode", bundle);
            adapter.remove(barcode);
            BarcodeFileUtils.overwriteListToFile(this, barcodeList);
            startActivityForResult(intent, 1);
            return true;
        } else if (item.getItemId() == R.id.context_remove_barcode) {
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
        final List<Barcode> cachedList = barcodeList;
        final List<Barcode> queryResult = new ArrayList<>();

        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                refreshListView(cachedList);
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

    //UI Refresh and Clear -----------------------------------------
    public void refreshListView (List<Barcode> newList){
        adapter.getData().clear();
        adapter.getData().addAll(newList);
        adapter.notifyDataSetChanged();
        System.out.println("CALLED  |||||||||");
    }

    public void refreshClearBarcodeList(MenuItem item) {
        refreshListView(BarcodeFileUtils.readAll(this));

        if (item.getItemId() == R.id.action_clear)
            dialogs.CLEAR_DIALOG.show();
    }

    //Sorting methods ----------------------------------------------
    public void showSortingDialog(MenuItem item) {
        SortingDialogFragment dialog = new SortingDialogFragment();
        dialog.show(getSupportFragmentManager(), "sorting");
    }

    @Override
    public void onDialogClick(int sortingChoice) {
        Barcode.sortingMethod = Barcode.BarcodeFields.values()[sortingChoice];
        Barcode[] arr;
        arr = barcodeList.toArray(new Barcode[0]);
        Arrays.sort(arr);
        refreshListView(Arrays.asList(arr));

        Toast.makeText(getApplicationContext(), "Barcodes have been sorted!", Toast.LENGTH_SHORT).show();
    }
}
