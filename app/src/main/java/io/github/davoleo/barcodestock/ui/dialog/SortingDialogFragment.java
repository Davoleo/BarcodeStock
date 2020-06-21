/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 18/12/2019 / 19:00
 * Class: SortingDialog
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 * ----------------------------------- */

package io.github.davoleo.barcodestock.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.ui.MainActivity;

import java.util.concurrent.atomic.AtomicInteger;

// TODO: 21/06/2020 move this to an activity containing all settings
public class SortingDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MainActivity activity = ((MainActivity) getActivity());
        final AtomicInteger selectedChoice = new AtomicInteger(activity.comparator.getComparableField().ordinal());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_sort_title);

        builder.setSingleChoiceItems(R.array.barcode_fields, selectedChoice.get(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedChoice.set(which);
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogClick(selectedChoice.get());
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (SortingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Class must implement NoticeDialogListener");
        }
    }

    SortingDialogListener listener;

    public interface SortingDialogListener {

        void onDialogClick(int sortingChoice);

    }
}
