package davoleo.barcodestock.ui;

import android.view.View;
import android.widget.AdapterView;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 18/11/2019 / 22:57
 * Class: ListViewClickHandler
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

// TODO: 18/11/2019 Implement this
public class ListViewClickHandler implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("ITEM CLICKED");
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("ITEM LONG CLICKED");
        return true;
    }
}
