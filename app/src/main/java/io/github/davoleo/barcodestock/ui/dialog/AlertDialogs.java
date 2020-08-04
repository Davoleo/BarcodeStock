package io.github.davoleo.barcodestock.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.SearchView;
import androidx.appcompat.app.AlertDialog;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.ui.ActivityAddEditBarcode;
import io.github.davoleo.barcodestock.ui.MainActivity;
import io.github.davoleo.barcodestock.util.BarcodeFileUtils;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 18/11/2019 / 22:59
 * Class: AlertDialogs
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class AlertDialogs {

    private final Activity activity;
    private AlertDialog.Builder builder;

    public final AlertDialog CLEAR_DIALOG;

    public AlertDialogs(Activity activity) {
        this.activity = activity;
        this.builder = new AlertDialog.Builder(activity);
        this.CLEAR_DIALOG = buildClearBarcodesDialog();
    }

    private AlertDialog buildClearBarcodesDialog() {
        builder.setMessage(R.string.message_clear_barcodes).setCancelable(true);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                BarcodeFileUtils.clearBarcodes(activity);
                ((MainActivity) activity).refreshListView(BarcodeFileUtils.readAll(activity));
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    //Old Barcode Scanning Dialog
    public AlertDialog buildAddBarcodeFromScanDialog(final Activity activity, final String barcode) {
        builder.setMessage(activity.getString(R.string.message_scaned_barcode) + barcode + activity.getString(R.string.message_select_action));
        builder.setPositiveButton(R.string.btn_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(activity.getApplicationContext(), ActivityAddEditBarcode.class).putExtra("edit", false).putExtra("barcode", barcode);
                activity.startActivityForResult(intent, 1);
            }
        });
        builder.setNegativeButton(R.string.action_search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SearchView searchView = activity.findViewById(R.id.action_search);
                searchView.setIconified(false);
                searchView.setQuery(barcode, true);
            }
        });
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
