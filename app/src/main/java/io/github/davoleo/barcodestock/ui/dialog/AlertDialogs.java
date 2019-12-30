package io.github.davoleo.barcodestock.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import io.github.davoleo.barcodestock.R;
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

    // TODO: 18/11/2019 Finish Up this AlertDialog
    private AlertDialog buildDeleteBarcodesDialog() {
        builder.setMessage(R.string.message_clear_barcodes).setCancelable(true);
        return builder.create();
    }

}
