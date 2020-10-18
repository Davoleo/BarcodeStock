/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 25/06/2020 / 12:44
 * Class: SettingsFragment
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.PreferenceFragmentCompat;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;
import kotlin.collections.SetsKt;

import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String SORTING_OPTION = "sorting_field";
    public static final Set<String> DEFAULT_INDEXED_FIELDS = SetsKt.hashSetOf("BARCODE", "TITLE", "DESCRIPTION", "PRICE");

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        preferenceChangeListener = (sharedPreferences, key) -> {
            if (key.equals(SORTING_OPTION)) {
                Log.d(MainActivity.TAG, "onSharedPreferenceChanged: Preferred sorting method was changed");
                MainActivity mainActivity = MainActivity.INSTANCE.get();
                if (mainActivity != null) {
                    mainActivity.refreshListView(BarcodeFileUtils.readAll(mainActivity));
                }
            }
        };
    }

    //Pause and resume changes handler
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
