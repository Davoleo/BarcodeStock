package davoleo.barcodestock;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TabHost;
import android.widget.Toast;
import davoleo.barcodestock.barcode.Barcode;
import davoleo.barcodestock.barcode.BarcodeDAO;

import java.lang.ref.WeakReference;
import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 20/09/2019 / 19:31
 * Class: BarcodeAsyncTask
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public final class BarcodeAsyncTask extends AsyncTask<Void, Void, List<Barcode>> {

    private WeakReference<Activity> weakActivity;

    public BarcodeAsyncTask(Activity activity) {
        this.weakActivity = new WeakReference<>(activity);
    }

    @Override
    protected List<Barcode> doInBackground(Void... voids) {
        BarcodeDAO dao = MainActivity.database.barcodeDAO();
        return dao.getAll();
    }

    @Override
    protected void onPostExecute(List<Barcode> barcodes) {
        Activity activity = weakActivity.get();
        if (activity == null)
            return;

        if (!barcodes.isEmpty())
        {
            Toast.makeText(activity, "Barcode list is empty!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(activity, "Barcode List is empty!", Toast.LENGTH_LONG).show();
            activity.onBackPressed();
        }
    }
}
