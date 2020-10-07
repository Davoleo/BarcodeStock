/* -----------------------------------
 * Author: Davoleo
 * Class: MainActivity
 * Project: BarcodeStock
 * ----------------------------------- */

package io.github.davoleo.barcodestock.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.BarcodeAdapter;
import io.github.davoleo.barcodestock.barcode.BarcodeComparator;
import io.github.davoleo.barcodestock.scanner.BarcodeScannerActivity;
import io.github.davoleo.barcodestock.ui.dialog.AlertDialogs;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BarcodeStock";
    public static WeakReference<MainActivity> INSTANCE;

    private Menu mainMenu;

    private BarcodeAdapter adapter;
    private AlertDialogs dialogs;
    private List<Barcode> searchResults = new ArrayList<>();

    private SharedPreferences sharedPreferences;

    private int selectedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        INSTANCE = new WeakReference<>(this);

        //Build Alert Dialogs
        dialogs = new AlertDialogs(this);

        //Setup the sorting preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortingOption = sharedPreferences.getString(SettingsFragment.SORTING_OPTION, "TITLE");
        BarcodeComparator comparator = new BarcodeComparator(Barcode.BarcodeFields.valueOf(sortingOption));

        List<Barcode> barcodesFromLocalFile = BarcodeFileUtils.readAll(this);
        Collections.sort(barcodesFromLocalFile, comparator);

        //database = Room.databaseBuilder(getApplicationContext(), BarcodeDatabase.class, "barcode_db").build();
//        try {
//            barcodeList = new BarcodeAsyncTask(this).execute().get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }

        //Link data source to the listview using a custom Adapter
        final ListView listView = findViewById(R.id.barcodeListView);
        adapter = new BarcodeAdapter(this, barcodesFromLocalFile);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d(TAG, "onItemLongClick: POSITION VALUE " + position);
            selectedItemId = position;
            listView.showContextMenu();
            return true;
        });

        //Register for Floating Contextual Menu
        registerForContextMenu(listView);

        Log.d(TAG, "onCreate: HAS STARTED SUCCESSFULLY");

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", false);
            startActivityForResult(intent, 1);

            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //                        .setAction("Action", null).show();
        });

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshClearBarcodeList(null);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    //Get the added barcode back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            //Result of Adding a new barcode
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Bundle barcodeBundle = data.getBundleExtra("newBarcode");
                Barcode barcode = Barcode.fromBundle(barcodeBundle);
                //Apply the changes on the local device storage
                BarcodeFileUtils.writeToFile(this, barcode);

                //Apply changes on the adapterView
                adapter.getData().add(barcode);
                sortBarcodeList(adapter.getData());
                adapter.notifyDataSetChanged();
            }

            //Result of Editing an existing barcode
            if (requestCode == 2 && resultCode == RESULT_OK) {
                Bundle oldBarcode = data.getBundleExtra("oldBarcode");
                Barcode newBarcode = Barcode.fromBundle(data.getBundleExtra("newBarcode"));
                adapter.remove(oldBarcode.getLong("code"), oldBarcode.getString("title"), oldBarcode.getString("desc"), oldBarcode.getFloat("price"));
                BarcodeFileUtils.overwriteListToFile(this, adapter.getData());
                BarcodeFileUtils.writeToFile(this, newBarcode);
                refreshListView(BarcodeFileUtils.readAll(this));
            }

            //Request Code:
            //3: Scanning a barcode from the MainActivity
            //4: Scanning a barcode from the AddEditBarcode Activity
            if ((requestCode == 3 || requestCode == 4) && resultCode == RESULT_OK) {

                String barcode = data.getStringExtra("barcode");
                boolean exists = adapter.getData().stream()
                        .map(listBarcode -> String.valueOf(listBarcode.getCode()))
                        .anyMatch(listBarcode -> listBarcode.equals(barcode));

                if (requestCode == 4)
                    exists = false;

                if (exists) {
                    MenuItem searchItem = mainMenu.findItem(R.id.action_search);
                    searchItem.expandActionView();
                    SearchView searchView = ((SearchView) searchItem.getActionView());
                    searchView.setQuery(barcode, true);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", false).putExtra("barcode", barcode);
                    startActivityForResult(intent, 1);
                }
            }
        }
        else {
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
            BarcodeFileUtils.overwriteListToFile(this, adapter.getData());
            return true;
        } else
            return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mainMenu = menu;

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = ((SearchView) searchItem.getActionView());
        final List<Barcode> cachedBarcodes = new ArrayList<>(adapter.getData());

        Set<Barcode.BarcodeFields> indexedFieldsReference = new HashSet<>();

        searchView.clearFocus();
        searchView.onActionViewCollapsed();

        //Update cached barcode every time the search view is expanded
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                updateQueryCache(cachedBarcodes);

                //Arrays.stream(barcodeFields).map(Enum::name).collect(Collectors.toSet())
                indexedFieldsReference.addAll(
                        sharedPreferences.getStringSet("indexed_fields", Collections.emptySet())
                                .stream()
                                .map(Barcode.BarcodeFields::valueOf)
                                .collect(Collectors.toSet())
                );

                searchView.onActionViewExpanded();
                searchView.setFocusable(true);
                searchView.requestFocusFromTouch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchResults.clear();
                searchView.setFocusable(false);
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                searchResults = cachedBarcodes.stream()
                        .filter(barcode -> compareQueryToFields(barcode, s, indexedFieldsReference))
                        .collect(Collectors.toList());

                //TODO find some way to optimize UI refresh +
                // Fix refresh showing all elements when search results == empty
                refreshListView(searchResults);
                return true;
            }
        });

        return true;
    }

    private void updateQueryCache(List<Barcode> cachedBarcodes) {
        cachedBarcodes.clear();
        cachedBarcodes.addAll(adapter.getData());
    }

    private boolean compareQueryToFields(Barcode barcode, String query, Set<Barcode.BarcodeFields> indexedFields)
    {
        boolean caseSensitive = sharedPreferences.getBoolean("case_sensitiveness", false);

        if (!caseSensitive)
            query = query.toLowerCase();

        boolean found = false;

        for (Barcode.BarcodeFields field : indexedFields) {
            switch (field) {
                case BARCODE:
                    found |= String.valueOf(barcode.getCode()).contains(query);
                    break;
                case TITLE:
                    String barcodeTitle = barcode.getTitle();
                    if (!caseSensitive)
                        barcodeTitle = barcodeTitle.toLowerCase();
                    found |= barcodeTitle.contains(query);
                    break;
                case DESCRIPTION:
                    String barcodeDescription = barcode.getDescription();
                    if (!caseSensitive)
                        barcodeDescription = barcodeDescription.toLowerCase();
                    found |= barcodeDescription.contains(query);
                    break;
                case PRICE:
                    found |= String.valueOf(barcode.getPrice()).contains(query);
                    break;
            }
        }

        return found;
    }

    //UI Refresh and Clear -----------------------------------------
    public void refreshListView (List<Barcode> newList) {
        adapter.getData().clear();

        if (!newList.isEmpty()) {
            sortBarcodeList(newList);
            adapter.getData().addAll(newList);
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "refreshListView: |!|!|!|!|!|  VIEW REFRESHED  |!|!|!|!|!|");
    }

    public void refreshClearBarcodeList(@Nullable MenuItem item) {

        if (item == null || item.getItemId() == R.id.action_refresh) {
            if (searchResults.isEmpty())
                refreshListView(BarcodeFileUtils.readAll(this));
            else
                refreshListView(searchResults);
        }
        else if (item.getItemId() == R.id.action_clear)
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
